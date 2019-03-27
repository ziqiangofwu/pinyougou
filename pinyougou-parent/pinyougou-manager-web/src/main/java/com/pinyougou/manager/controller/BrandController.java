package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
	
	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		
		return brandService.findAll();
	}
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int size){
		
		return brandService.findPage(page, size);
	}
	
	//增加
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand brand){
		try {
			brandService.add(brand);
			return new Result(true,"保存成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"保存失败!");
		}
	}
	
	//根据ID查找数据
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		
		return brandService.findOne(id);
	}
	
	//更新数据
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
			return new Result(true,"更新成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"更新失败!");
		}
	}
	
	//删除数据
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			brandService.delete(ids);
			return new Result(true,"删除成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"删除失败!");
		}
		
	}
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand,int page, int size){
		
		return brandService.findPage(brand, page, size);	
	}
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList() {
		 
		return brandService.selectOptionList();
	}
	
	
}
