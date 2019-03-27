package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleOrderByItem;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	
	@Autowired
	private RedisTemplate redisTemplte;
	
	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		
		
		
		//从Redis中提取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplte.boundHashOps("cartList").get(order.getUserId());
		List<String> orderIdList = new ArrayList<>();//订单id集合
		double total_money = 0;//总金额
		//循环购物车列表添加订单
		for (Cart cart : cartList) {
			TbOrder tborder = new TbOrder();//
			long orderId = idWorker.nextId();//获取ID
			tborder.setOrderId(orderId);
			tborder.setPaymentType(order.getPaymentType());//支付类型
			tborder.setStatus("1");//未付款
			tborder.setCreateTime(new Date());//下单时间
			tborder.setUpdateTime(new Date());//更新时间
			tborder.setUserId(order.getUserId());//当前用户
			tborder.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
			tborder.setReceiverMobile(order.getReceiverMobile());//收货人电话
			tborder.setReceiver(order.getReceiver());//收货人
			tborder.setSourceType(order.getSourceType());//订单来源
			//tborder.setSellerId(order.getSellerId());//商家的ID
			
			double money = 0;//合计
			//循环购物车每条的明细
			for(TbOrderItem orderItem: cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId());//主键
				orderItem.setOrderId(orderId);//订单编号
				orderItem.setSellerId(cart.getSellerId());//商家ID				
				orderItemMapper.insert(orderItem);
				money += orderItem.getTotalFee().doubleValue();
				//Long goodsId = orderItem.getGoodsId();
				
			}
			tborder.setSellerId(cart.getSellerId());//商家的ID
			tborder.setPayment(new BigDecimal(money));//合计
			orderMapper.insert(tborder);
			orderIdList.add(orderId+"");
			total_money += money;//累加到总金额
		
		}
		//添加支付日志
		if("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			payLog.setCreateTime(new Date());
			payLog.setUserId(order.getUserId());//用户ID
			payLog.setOrderList(orderIdList.toString().replace("[","").replace("]","").replace(" ",""));//订单ID
			payLog.setPayType("1");//支付类型
			payLog.setTotalFee((long)(total_money*100));//总金额()
			payLog.setTradeState("0");//支付状态
			
			payLogMapper.insert(payLog );//插入到日志表
			redisTemplte.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
		}
		//清除购物车
		redisTemplte.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		
		/**
		 * 查询PayLog
		 * 
		 */
		@Override
		public TbPayLog searchPayLogFormRedis(String userId) {
			
			return (TbPayLog) redisTemplte.boundHashOps("payLog").get(userId);
		}
		
		/**
		 * 修改日志的支付状态
		 */
		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			
			//修改日志的状态及相关字段
			TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			payLog.setPayTime(new Date());//支付时间
			payLog.setTradeState("1");//支付状态(交易成功!)
			payLog.setTransactionId(transaction_id);//微信支付流水号
			payLogMapper.updateByPrimaryKey(payLog);
			//修改订单表的状态
			String orderList = payLog.getOrderList();//订单ID 串
			String[] orderIds = orderList.split(",");
			for (String orderId : orderIds) {
				TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
				if(order != null){
					order.setStatus("2");//修改到已付款状态
					order.setPaymentTime(new Date());
					orderMapper.updateByPrimaryKey(order);
				}				
			}
			//清楚缓存中的PayLog
			redisTemplte.boundHashOps("payLog").delete(payLog.getUserId());
		}
	
}
