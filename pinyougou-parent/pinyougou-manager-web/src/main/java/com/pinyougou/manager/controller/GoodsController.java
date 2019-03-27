package com.pinyougou.manager.controller;

import java.util.Arrays;
import java.util.List;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;

/**
 * controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	private String status;

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll() {
		return goodsService.findAll();
	}

	/**
	 * 返回全部列表
	 * 
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows) {
		return goodsService.findPage(page, rows);
	}

	/**
	 * 修改
	 * 
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods) {
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}

	/**
	 * 获取实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id) {
		return goodsService.findOne(id);
	}
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	
	@Autowired
	private Destination topicPageDeleteDestination;
	/**
	 * 批量删除
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long[] ids) {
		try {
			goodsService.delete(ids);
			//从索引库中删除
			//itemSearchServcie.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {				
				@Override
				public Message createMessage(Session session) throws JMSException {					
					return session.createObjectMessage(ids);
				}
			});
			//删除详情页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}

	/**
	 * 查询+分页
	 * 
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
		return goodsService.findPage(goods, page, rows);
	}

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private Destination queueSolrDestination;
	
	@Autowired
	private Destination topicPageDestination;

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {

		try {
			goodsService.updateStatus(ids, status);
			if ("1".equals(status)) {// 如果是审核通过
				//*****导入到索引库
				// 得到需要导入的SKU列表
				List<TbItem> itemList = goodsService.findItemListByGoodsIdListAndStatus(ids, status);
				final String jsonString = JSON.toJSONString(itemList);
				// 导入到solr
					//if(itemList.size()>0){
						 //itemSearchServcie.importList(itemList);
					//}
		       
				jmsTemplate.send(queueSolrDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						
						return session.createTextMessage(jsonString);
					}
				});
				//******生成商品详细页
				for(final Long goodsId: ids){
					//itemPageService.genItemHtml(goodsId);
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							
							return session.createTextMessage(goodsId+"");
						}
					});
				}
				
			}
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}
//	@Reference(timeout=50000)
//	private ItemPageService itemPageService;
	//生成静态页面(测试)
	@RequestMapping("/genHtml")
	public void genHtml (Long goodsId){
		//itemPageService.genItemHtml(goodsId);
	}

}
