package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 刷新秒杀商品
	 */
	@Scheduled(cron="0 * * * * ?")
	public void refreshSeckillGoods(){
		System.out.println("执行了秒杀商品增量更新 任务调度:"+new Date());
		//查询所有的秒杀商品的键集合
		List keys = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
		System.out.println(keys);
		//查询正在秒杀的商品列表
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核通过的商品
		criteria.andStartTimeLessThanOrEqualTo(new Date());//开始日期小于等于现在的时间
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());//结束日期大于等于现在时间
		criteria.andStockCountGreaterThan(0);//商品库存大于0的
		if(keys.size()>0){
			criteria.andIdNotIn(keys);//排除缓存中已有的商品
		}		
		List<TbSeckillGoods>seckillGoodsList =  seckillGoodsMapper.selectByExample(example );
	    //将商品放入缓存
		//System.out.println("将秒杀商品装入缓存!");
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("执行了秒杀商品ID:"+seckillGoods.getId());
		}		
		System.out.println("将"+seckillGoodsList.size()+"条商品放入缓存!");
		System.out.println("========END==========");
	}
	
	
	@Scheduled(cron="* * * * * ?")
	public void removeSeckillGoods(){
		System.out.println("移除秒杀商品任务在执行!"+new Date());
		//扫描缓存中秒杀商品列表,发信过期的移除
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			//如果结束日期小于当前日期,说明过期
			if(seckillGoods.getEndTime().getTime()<new Date().getTime()){
				//向数据库保记录
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
				//移除缓存
				redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
				System.out.println("移除秒杀商品ID:"+seckillGoods.getId());
			}			
		}
		System.out.println("移除秒杀商品任务结束!=====END========");
	}
	
	
}
