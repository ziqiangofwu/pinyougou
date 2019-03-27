package com.pinyougou.page.service.impl;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
@Component
public class pageDeleteListener implements MessageListener {
	
	
	@Autowired
	private ItemPageService itemPageService;
	
	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Long[] goodsIds = (Long[]) objectMessage.getObject();
			System.out.println("监听接收到的消息为:"+goodsIds);
			boolean b = itemPageService.deleteItemHtml(goodsIds);
			System.out.println("网页删除的结果为:"+b);
		} catch (JMSException e) {
			
			e.printStackTrace();
		}
	}

}
