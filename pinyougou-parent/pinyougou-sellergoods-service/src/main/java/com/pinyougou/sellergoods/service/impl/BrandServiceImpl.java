package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
@Service
@Transactional
public class BrandServiceImpl implements BrandService {
	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {
		
		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//分页
		//  brandMapper.selectByExample(null):查询所有数据
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		Long total = page.getTotal();
		/*List<TbBrand> result = page.getResult();
		PageResult pageResult = new PageResult();
		return ();*/
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(TbBrand brand) {
		
		brandMapper.insert(brand);
	}

	@Override
	public TbBrand findOne(Long id) {
		
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
		
	}

	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
		
	}

	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//分页
		
		TbBrandExample example= new TbBrandExample();
		//构建条件
		Criteria criteria = example.createCriteria();
		if(brand != null){
			if(brand.getName() != null&&brand.getName().length()>0){
				//构建条件中封装的方法
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if(brand.getFirstChar() != null&&brand.getFirstChar().length()>0){
				//构建条件中封装的方法
				criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
			}
		}
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		// TODO Auto-generated method stub
		return brandMapper.selectOptionList();
	}
	
	
	
	
}
