package com.pinyougou.pojogroup;

import java.io.Serializable;
import java.util.List;
import com.pinyougou.pojo.TbOrderItem;
/**
 * 购物车对象
 * @author user
 *
 */
public class Cart implements Serializable{
	
	private String sellerId;//商家的ID
	
	private String SellerName;//商家的名称
	
	private List<TbOrderItem> orderItemList;//购物车明细集合

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getSellerName() {
		return SellerName;
	}

	public void setSellerName(String sellerName) {
		SellerName = sellerName;
	}

	public List<TbOrderItem> getOrderItemList() {
		return orderItemList;
	}

	public void setOrderItemList(List<TbOrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}
	
	
	
	
	
}
