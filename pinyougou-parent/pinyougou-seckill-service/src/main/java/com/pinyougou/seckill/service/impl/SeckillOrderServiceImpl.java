package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}	
		
		@Autowired
		private RedisTemplate redisTemplate;
		
		@Autowired
		private TbSeckillGoodsMapper seckillGoodsMapper;
		
		@Autowired
		private IdWorker idWorker;
		
		
		@Override
		public void submitOrder(Long seckillId, String userId) {
			
			//从缓存中查询秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			if(seckillGoods==null){
				throw new RuntimeException("商品不存在!");
			}
			if(seckillGoods.getStockCount()<=0){
				throw new RuntimeException("商品卖完啦!");
			}
			//扣减库存
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
			//放入缓存
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
			if(seckillGoods.getStockCount() == 0){
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//更新数据库
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
				System.out.println("商品同步到数据库!");
			}
			//存储秒杀的订单(存入的是Redis中)
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(idWorker.nextId());//
			seckillOrder.setSeckillId(seckillId);
			seckillOrder.setStatus("0");
			seckillOrder.setMoney(seckillGoods.getCostPrice());
			seckillOrder.setUserId(userId);
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			seckillOrder.setCreateTime(new Date());				
			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
			System.out.println("保存订单成功(Redis)!");
		}

		@Override
		public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
			
			return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		}
		
		
		/**
		 * 保存支付成功订单
		 */
		@Override
		public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
			// 从缓存中查询订单
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			if(seckillOrder == null){
				throw new RuntimeException("订单不存在!");
			}
			if(seckillOrder.getId().longValue() != orderId.longValue()){
				throw new RuntimeException("订单不相符!");
			}
			seckillOrder.setTransactionId(transactionId);//交易流水号
			seckillOrder.setStatus("1");//状态
			seckillOrder.setUserId(userId);
			
			seckillOrder.setPayTime(new Date());//支付时间
			seckillOrderMapper.insert(seckillOrder);//保存数据库
			//System.out.println("数据库:"+seckillOrder);
			redisTemplate.boundHashOps("seckillOrder").delete(userId);//清楚Redis缓存
		}
		
		/**
		 * 从缓存中 删除 订单
		 */
		@Override
		public void deleteOrder(String userId, Long orderId) {
			
			//从缓存中取出日志
			TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			if(seckillOrder != null && seckillOrder.getId().longValue()== orderId.longValue()){
				redisTemplate.boundHashOps("seckillOrder").delete(userId);//从缓存中删除订单
				//恢复商品
				//从缓存中提取商品
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
				if(seckillGoods != null){
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
				}else{
					seckillGoods = new TbSeckillGoods();
					seckillGoods.setId(seckillOrder.getSeckillId());
					seckillGoods.setStockCount(1);
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
		
				}
				System.out.println("订单取消:"+orderId);
			}
		}
	
}
