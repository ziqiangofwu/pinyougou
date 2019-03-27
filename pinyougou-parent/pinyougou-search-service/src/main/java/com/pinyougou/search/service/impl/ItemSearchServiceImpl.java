package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 5000) // timeout=5000:设置超时时间 ,主要是为了第一次走逻辑时候启动过慢造成超时
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap();
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格
		// 1.查询列表
		map.putAll(searchList(searchMap));
		// 2.分组查询 商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		// 3.查询品牌和规格列表
		String category = (String) searchMap.get("category");
		if (!category.equals("")) {//假如 category 不为空 ,既是用户点击了.那么就走用户点击的程序
			map.putAll(searchBrandAndSpecList(category));
		} else {//假如为空,就走默认
			if (categoryList.size() > 0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		// 
		return map;
	}

	// 查询列表
	private Map searchList(Map searchMap) {
		Map map = new HashMap();
		// 高亮选项初始化
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 高亮域
		highlightOptions.setSimplePrefix("<em style='color:red' >");// 设置前缀
		highlightOptions.setSimplePostfix("</em>");// 设置高亮后缀
		query.setHighlightOptions(highlightOptions);// 为查询对象设置高亮选项
		// 1.1>关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);

		// 1.2>按商品分类进行过滤查询
		if (!"".equals(searchMap.get("category"))) {// 如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.3>按品牌进行过滤查询
		if (!"".equals(searchMap.get("brand"))) {// 如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.4>按规格过滤
		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		// 1.5> 按价格过滤查询
		if(!"".equals(searchMap.get("price"))){
			String[] price = ((String)searchMap.get("price")).split("-");
			if(!price[0].equals("0")){//如果最低价格不等于零
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if(!price[1].equals("*")){//如果最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			
		}
		// 1.6> 按分页查询
		Integer pageNo =  (Integer) searchMap.get("pageNo");//获得页码
		if(pageNo == null){
			pageNo = 1;
		}
		Integer pageSize =  (Integer) searchMap.get("pageSize");//获得每页的数量
		if(pageSize == null){
			pageSize = 20;
		}
		query.setOffset((pageNo-1)*pageSize);//设置起始页
		query.setRows(pageSize);//设置每页记录数
		
		
		//排序
		String sortValue = (String) searchMap.get("sort");//升序 或者降序
		String sortField = (String) searchMap.get("sortField");//排序字段
		if(sortValue != null && !sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
			
		}
		
		
		
		// ************************获取高亮结果集(不变的)****************************************
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		// 高亮入口集合
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			// 获取高亮列表(高亮域的个数)
			List<Highlight> highlightList = entry.getHighlights();
			if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		return map;
	}

	private List<String> searchCategoryList(Map searchMap) {

		List<String> list = new ArrayList<String>();
		Query query = new SimpleQuery();
		// 关键字查询 相当于查询语句中的 where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		// 设置分组选项 相当于查询语句中的 group by
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		// 获取分组页
		GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 获取分组结果的对象
		GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
		// 获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		// 获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for (GroupEntry<TbItem> groupEntry : entryList) {
			list.add(groupEntry.getGroupValue());
		}
		return list;
	}

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 根据商品分类名称获取品牌和规格列表
	 * 
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {

		Map map = new HashMap<>();
		// 根据商品分类名称得到模板id
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId != null) {
			// 根据模板id得到品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			// 根据模板id得到归根列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}
	/**
	 * 根据id删除索引库
	 */
	@Override
	public void deleteByGoodsIds(List goodsIds) {		
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
	}

}
