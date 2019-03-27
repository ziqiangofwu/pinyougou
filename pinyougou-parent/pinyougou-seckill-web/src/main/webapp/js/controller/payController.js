app.controller("payController", function($scope,$location, payService) {
	/**
	 * 本地支付
	 */
	$scope.creatNative = function() {
		payService.creatNative().success(function(response) {
			// 显示订单号和金额
			$scope.money = (response.total_fee / 100).toFixed(2);// 金额
			$scope.out_trade_no = response.out_trade_no;// 订单号
			// 生成二维码
			var qr = new QRious({
				element : document.getElementById('qrious'),
				size : 250,
				value : response.code_url,
				level : 'H'
			});
			//alert(345);
			queryPayStatus();//查询支付状态
		});
	}
	/**
	 * 查询支付状态
	 */
	
	queryPayStatus = function (){
		//alert(123);
		payService.queryPayStatus($scope.out_trade_no).success(function (response){
			if(response.success){
				location.href="paysuccess.html#?money="+$scope.money;
			}else{
				if(response.message == "二维码超时!"){
					//$scope.creatNative();//重新生成二维码
					alert("手贱呐!");
				}else{
					location.href="payfail.html";
				}
				
			}
		});
	}
	//获取金额
	$scope.getMoney = function (){
		return $location.search()['money'];
	}
});