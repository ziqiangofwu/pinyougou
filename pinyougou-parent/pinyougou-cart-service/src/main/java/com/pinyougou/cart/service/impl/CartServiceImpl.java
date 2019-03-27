package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

/**
 * 购物车服务实现类
 * cartList:购物车列表   cart:购物车   orderItem:购物车明细对象     
 * @author user
 *
 */
@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		// 1> 根据商品的SKU ID 查询商品SKU对象
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("该商品不存在!");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("该商品状态非法!");
		}
		// 2> 根据SKU对象获取商家ID
		String sellerId = item.getSellerId();
		// 3> 根据商家ID 在购物车列表中查询商家对象
		Cart cart = searchCartBySellerId(cartList, sellerId);
		// 4> 如果购物车列表中不存在该商家的购物车
		if (cart == null) {
			// 4.1> 创建一个新的购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);// 商家ID
			cart.setSellerName(item.getSeller());// 商家名称
			List<TbOrderItem> orderItemList = new ArrayList<>();// 构建购物车明细列表
			// 创建购物车明细对象
			TbOrderItem orderItem = creatOrderItem(num, item);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			// 4.2> 将新的购物车对象添加到购物车列表中
			cartList.add(cart);
		} else {
			// 5> 如果购物车列表中存在该商家
			// 判断该商品是否在该商品的购物车列表中
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			if(orderItem == null){
				// 5.1> 如果不存在 , 创建新的购物车明细对象,并添加到该购物车列表中
				orderItem = creatOrderItem(num, item);
				cart.getOrderItemList().add(orderItem);
			}else{
				// 5.2> 如果存在, 在原有的数量添加数量,并更新金额
				orderItem.setNum(num+orderItem.getNum());
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
				//如果购物车明细数量<= 0 时,移除此购物车
				if(orderItem.getNum() <= 0){
					cart.getOrderItemList().remove(orderItem);
				}
				//当购物车明细数量为0时,在购物车列表中移除此购物车
				if(cart.getOrderItemList().size() == 0 ){
					cartList.remove(cart);
				}
			}
		}

		return cartList;
	}
	/**
	 * 根据SKU ID在购物车列表中查询购物车明细对象
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	public TbOrderItem searchOrderItemByItemId (List<TbOrderItem> orderItemList,Long itemId){
		for (TbOrderItem orderItem : orderItemList) {
			if(orderItem.getItemId().longValue() == itemId.longValue()){
				return orderItem;
			}
		}
		return null;
	}

	/**
	 * 创建订单明细
	 * 
	 * @param num
	 * @param item
	 */
	private TbOrderItem creatOrderItem(Integer num, TbItem item) {
		// 创建购物车明细对象
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
		return orderItem;
	}

	/**
	 * 根据商家ID 查询购物车对象
	 * 
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 从Redis中提取购物车列表
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从Redis中提取购物车"+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList == null){
			cartList = new ArrayList<>();
		}
		return cartList;
	}
	/**
	 * 将购物车列表存到Redis中
	 */
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向Redis中存入购物车"+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		
	}
	/**
	 * 合并购物车
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		for(Cart cart:cartList2){
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				 cartList1 = addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
			}
		}
		return cartList1;
	}

}
