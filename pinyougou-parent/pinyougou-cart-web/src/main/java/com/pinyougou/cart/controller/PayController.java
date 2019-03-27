package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference(timeout=50000)
	private WeixinPayService weixinPayService;
	
	@Reference
	private OrderService orderService;
	
	/**
	 * 生成二维码
	 * @return
	 */
	@RequestMapping("/creatNative")
	public Map creatNative(){
		//获取当前用户
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		//到Redis中查询日志
		TbPayLog payLog = orderService.searchPayLogFormRedis(userName);
		if(payLog != null){
			return weixinPayService.creatNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
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
				 orderService.updateOrderStatus(out_trade_no,  payStatus.get("transaction_id"));//修改订单状态
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
				 break; 
			 }
			 
		 }
		 return result;
	}
	
}
