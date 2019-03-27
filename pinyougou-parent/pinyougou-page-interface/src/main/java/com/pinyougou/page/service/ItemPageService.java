package com.pinyougou.page.service;

/**
 * 商品详情页
 * @author user
 *
 */
public interface ItemPageService {
	
	/**
	 * 生成商品详细页
	 * @param goodsId
	 * @return
	 */
	public boolean genItemHtml(Long goodsId);
	
	/**
	 * 删除商品详情页
	 * @param goodsIds
	 * @return
	 */
	public boolean deleteItemHtml(Long[] goodsIds);
	
	
}
