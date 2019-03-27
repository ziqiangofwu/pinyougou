package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsDescExample;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */

	@Autowired // 引入扩展表
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbBrandMapper brandMapper;

	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");// 设置商品的状态,刚插入的商品没经过审核,状态为0
		goodsMapper.insert(goods.getGoods());// 插入商品的基本信息

		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());// 需要根据商品基本信息得到
		goodsDescMapper.insert(goods.getGoodsDesc());// 插入商品扩展信息
		// 插入sku列表数据
		saveItemList(goods);

	}

	private void saveItemList(Goods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem item : goods.getItemList()) {
				String title = goods.getGoods().getGoodsName();// 得到spu
				Map<String, Object> map = JSON.parseObject(item.getSpec());
				for (String key : map.keySet()) {
					title += " " + map.get(key);
				}
				item.setTitle(title);
				setItemValus(goods, item);
				itemMapper.insert(item);
			}
		} else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());// 商品KPU+规格描述串作为SKU名称
			item.setPrice(goods.getGoods().getPrice());// 价格
			item.setStatus("1");// 状态
			item.setIsDefault("1");// 是否默认
			item.setNum(99999);// 库存数量
			item.setSpec("{}");
			setItemValus(goods, item);
			itemMapper.insert(item);
		}
	}

	private void setItemValus(Goods goods, TbItem item) {
		// 商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());// 三级分类
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		item.setGoodsId(goods.getGoods().getId());// 商品ID
		item.setSellerId(goods.getGoods().getSellerId());// 商家ID
		// 品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		System.out.println(goods.getGoods().getBrandId());
		System.out.println("-------------" + brand);
		item.setBrand(brand.getName());
		// 分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());

		// 商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		// 图片信息
		List<Map> parseArray = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (parseArray.size() > 0) {
			item.setImage((String) parseArray.get(0).get("url"));
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods) {
		// 更新基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		// 更新扩展表数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		// 删除原有的sku数据
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		// 插入新的sku列表数据
		saveItemList(goods);

	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id) {
		Goods goods = new Goods();
		// 商品基本表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		// 商品的扩展表
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);

		// 读取sku列表
		// selectByExample;条件查询
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> list = itemMapper.selectByExample(example);
		goods.setItemList(list);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");// 表示逻辑删除
			goodsMapper.updateByPrimaryKey(goods);// 更新goods
		}
	}

	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();// 过滤 isDelete 字段为空的数据
		if (goods != null) {
			if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
				// criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				// 模糊查询不精确 .这里查询商家的列表需要用的是精确查询
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}
			if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}
			if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}
			if (goods.getCaption() != null && goods.getCaption().length() > 0) {
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}
			if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}
			if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}
			if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}

		}

		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}

	}
	/**
	 * 
	 * 根据SPU的ID集合查询SKU列表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	public List<TbItem> findItemListByGoodsIdListAndStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);// 状态
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));// 指定条件:SPUID集合
		return itemMapper.selectByExample(example);
	}
	
	/**
	 * 更新状态
	 */
	@Override
	public void updateIsMarkerTableStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsMarketable(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
		
		
	}

}
