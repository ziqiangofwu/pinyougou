package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference(timeout=50000)
	private WeixinPayService weixinPayService;
	
	@Reference
	private SeckillOrderService seckillOrderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/creatNative")
	public Map creatNative(){
		//获取当前用户
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		//到Redis中查询日志
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userName);
		if(seckillOrder != null){
			return weixinPayService.creatNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
		}else{
			return new HashMap<>();
		}
		
	}
	
	/**
	 * 查询支付状态
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		Result result = null;
		int x =0;
		 while(true){
			 Map<String,String> payStatus = weixinPayService.queryPayStatus(out_trade_no);
			 if(payStatus == null){
				 result = new Result(false, "再来再来!");
				 break;
			 }
			 if(payStatus.get("trade_state").equals("SUCCESS")){
				 result = new Result(true, "可以可以!");
				 seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), payStatus.get("transactionId"));
				 break;
			 }
			 try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			 x++;
			 if(x>100){
				 result = new Result(false, "二维码超时!");
				 
				//关闭支付
				 Map<String,String> closePay = weixinPayService.closePay(out_trade_no);
				 if(closePay != null && "FAIL".equals(closePay.get("return_code"))){
					 if("ORDERPAID".equals(closePay.get("err_code"))){
						 result = new Result(true, "可以可以!");
						 seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), payStatus.get("transactionId"));
					 }
				 }
				 
				 
				 if(result.isSuccess() == false){
					 //删除订单
					 seckillOrderService.deleteOrder(userId, Long.valueOf(out_trade_no));					 
				 }				 
				 break; 
			 }			 
		 }
		 return result;
	}
	
}
