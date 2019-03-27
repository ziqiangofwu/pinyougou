app.controller("seckillGoodsController",function ($scope,$location,$interval,seckillGoodsService){
	/**
	 * 读取列表数据到表单中
	 */
	$scope.findList = function(){
		seckillGoodsService.findList().success(function (response){
			$scope.list = response;
		});
	}
	
	/**
	 * 根据ID从Redis中查询商品
	 */
	$scope.findOne = function (){
		seckillGoodsService.findOne($location.search()['id']).success(function (response){
			$scope.entity = response;
			//总秒数(取整)
			allsecond = Math.floor((new Date($scope.entity.endTime).getTime()-(new Date().getTime())))/1000;
			time = $interval(function (){
				allsecond = allsecond - 1;
				$scope.timeString = convertTimeString(allsecond);//转换时间字符串
				if(allsecond <= 0){
					$interval.cancel(time);
					alert("本次秒杀结束!");
				}
			},1000);
		});
	}
	
	convertTimeString = function (allsecond){
		var days =  Math.floor(allsecond/(60*60*24));//天数
		var hours = Math.floor((allsecond-days*60*60*24)/(60*60));	//小时数
		var minutes = Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分钟
		var seconds = Math.floor(allsecond-days*60*60*24-hours*60*60-minutes*60);//秒数
		var timeString = "";
		if(days > 0){
			timeString = days + "天";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}
	
	//秒杀订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
			function(response){
				if(response.success){
					alert("下单成功，请在1分钟内完成支付");
					location.href="pay.html";
				}else{
					alert(response.message);
				}
			}
		);		
	}
	
});