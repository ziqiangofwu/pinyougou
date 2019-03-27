//购物车控制层
app.controller("cartController",function ($scope,cartService){
	
	//查询购物车列表
	$scope.findCartList = function (){
		cartService.findCartList().success(function (response){
			$scope.cartList= response;
			$scope.totalValue = cartService.sum($scope.cartList);//	求和计数
		});
	}
	
	//添加商品到购物车
	$scope.addGoodsToCartList = function (itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(function (response){
			if(response.success){
				$scope.findCartList();//刷新列表
			}else{
				alert(response.message);//发出错误信息
			}
		});
	}
	
	//获取地址列表
	$scope.findAddressList = function (){
		
		cartService.findAddressList().success(function (response){
			$scope.addressList = response;
			for(var i=0;i<$scope.addressList.length;i++){
				if($scope.addressList[i].isDefault == '1'){
					$scope.address = $scope.addressList[i];
					break;
				}
			}
		});
	}
	//选择地址
	$scope.selectAddress = function (address){
		$scope.address = address;
	}
	
	//判断某地址对象是否是当前地址
	$scope.isSelectedAddress = function (address){
		if(address == $scope.address){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.order = {paymentType:'1'};//订单对象
	//选择支付类型
	$scope.selectPayType = function (type){
		$scope.order.paymentType = type;
	}
	
	//提交订单
	$scope.submitOrder = function (){
		$scope.order.receiverAreaName = $scope.address.address;//收货地址
		$scope.order.receiverMobile = $scope.address.mobile;//手机号
		$scope.order.receiver = $scope.address.contact;//收货人
		
		cartService.submitOrder($scope.order).success(function (response){
			if (response.success){//如果成功				
				if($scope.order.paymentType == '1'){//如果是微信支付.跳转到微信支付
					location.href="pay.html";
				}else{//如果是货到付款.跳转到提示页面
					location.href = "paysuccess.html";
				}
			}else{
				alert(response.meaasge);//也可以跳转到提示页面
			}
			
		});
	}
	
})