package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private HttpServletRequest request;

	@Reference
	private CartService cartService;

	@Autowired
	private HttpServletResponse response;

	@RequestMapping("/findCartList")
	// 提取cookie购物车信息
	public List<Cart> findCartList() {
		// 得到登陆人账号,判断当前是否有人登陆
		// 测试：当用户未登陆时，username的值为anonymousUser
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "utf-8");
		if (cartListString == null || cartListString.equals("")) {
			cartListString = "[]";			
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		if (username.equals("anonymousUser")) {// 如果未登陆
			System.out.println("从cookie中提取购物车");
			
			return cartList_cookie;
		} else {// 如果登陆
			// 从Redis中提取
			List<Cart> cartListRedis = cartService.findCartListFromRedis(username);
			System.out.println("从Redis中提取购物车!");
			if(cartList_cookie.size()>0){//判断当cookie中的购物车有没有商品
				//得到合并后的购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartListRedis);
				//将合并后的购物车存入到Redis
				cartService.saveCartListToRedis(username, cartList);
				//清空cookie中的购物车
				util.CookieUtil.deleteCookie(request, response, "cartList");
				System.out.println("执行合并购物车!");
				return cartList;
			}
			return cartListRedis;
		}
	}

	@RequestMapping("/addGoodsToCartList")
	//此注解可以替代下面两行代码,SpringMVC版本要在4.2以上
//	@CrossOrigin(origins="http://localhost:9105")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId, Integer num) {
		
		//可以访问的域(此操作不需要操作cookie)
		//response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		//如果要操作cookie的话,必须要加上这句
		//response.setHeader("Access-Control-Allow-Credentials", "true");
		
		
		// 得到登陆人账号,判断当前是否有人登陆
		// 测试：当用户未登陆时，username的值为anonymousUser
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人:"+username);
		try {
			// 提取cookie信息
			List<Cart> cartList = findCartList();
			// 调用服务方法操作购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if (username.equals("anonymousUser")) {// 如果未登陆
				// 将新的购物车放回cookie
				String cartListString = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "utf-8");
				System.out.println("向cookie中存入购物车!");
			} else {
				cartService.saveCartListToRedis(username, cartList);
				System.out.println("向Redis中存入购物车!");
			}
			return new Result(true, "存入购物车成功!");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "存入购物车失败!");
		}
	}
}
