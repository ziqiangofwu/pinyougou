package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/*
 * 品牌的接口
 */
public interface BrandService {
	//查询所有的商品
	public List<TbBrand> findAll();
	//查询分页  pageNum:当前总数   pageSize:当前页数
	public PageResult findPage(int pageNum, int pageSize);
	
	//增加数据
	public void add(TbBrand brand);
	
	//根据ID查找数据
	public TbBrand findOne(Long id);
	
	//更新数据
	public void update(TbBrand brand);
	
	//删除数据
	public void delete(Long[] ids);
	
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize);
	//返回下拉列表数据
	public List<Map> selectOptionList();
	
	
	
}
