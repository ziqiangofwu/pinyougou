package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${appid}")
	private String appid;

	@Value("${partner}")
	private String partner;

	@Value("${partnerkey}")
	private String partnerkey;

	/**
	 * 生成微信支付二维码
	 */
	@Override
	public Map creatNative(String out_trade_no, String total_fee) {
		// 参数的封装
		Map param = new HashMap();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		param.put("body", "品优购");// 商品描述
		param.put("out_trade_no", out_trade_no);// 商户订单号
		param.put("total_fee", total_fee);// 标价金额(分)
		param.put("spbill_create_ip", "127.0.0.1");// 终端IP
		param.put("notify_url", "www.itcast.cn");// 通知地址(回调地址)随便写
		param.put("trade_type", "NATIVE");// 交易类型

		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("请求的参数:" + xmlParam);

			// 发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			// 获取结果
			String xmlResult = httpClient.getContent();
			// 转成Map
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
			System.out.println("微信返回的结果:" + mapResult);
			Map map = new HashMap<>();
			map.put("code_url", mapResult.get("code_url"));// 支付二维码的连接地址
			map.put("out_trade_no", out_trade_no);// 订单号
			map.put("total_fee", total_fee);// 总金额
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}

	}

	/**
	 * 查询支付状态
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		// 封装参数
		Map param = new HashMap<>();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		try {
			String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);// 签名
			// 发送请求
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(paramXml);
			httpClient.post();
			// 返回结果
			String result = httpClient.getContent();
			// 转成Map
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println("支付的状态为:" + map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map closePay(String out_trade_no) {
		Map param=new HashMap();
		param.put("appid", appid);//公众账号ID
		param.put("mch_id", partner);//商户号
		param.put("out_trade_no", out_trade_no);//订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		String url="https://api.mch.weixin.qq.com/pay/closeorder";
		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client=new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
			
		} catch (Exception e) {			
			e.printStackTrace();
			return null;
		}
		
	}
}