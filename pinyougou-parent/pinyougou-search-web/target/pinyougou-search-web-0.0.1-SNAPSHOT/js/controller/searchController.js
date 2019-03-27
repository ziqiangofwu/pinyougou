app.controller("searchController",function ($scope,$location,searchService){
	
	//定义搜索对象的结构
	$scope.searchMap = {'keywords':'','category':'','brand':'','spec':{},'price':'',
			'pageNo':1,'pageSize':40,'sort':'','sortField':''};
	
	
	//搜索
	$scope.search = function (){
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;//把字符串转为数字
		searchService.search($scope.searchMap).success(function (response){			
			$scope.resultMap = response;//返回搜索结果
			//$scope.searchMap.pageNo = 1;//查询后显示第一页
			buildPageLable();//构建分页栏
		});
	}
	//构建分页栏
	buildPageLable = function (){		
		$scope.pageLable = [];
		var firstPage = 1;//开始页码
		var lastPage = $scope.resultMap.totalPages;//截止页码
		 $scope.firstDot = true;//前面有点
		 $scope.lastDot = true;//后面有点
		//alert(lastPage+" "+$scope.resultMap.pageNo);
		if(lastPage>5){//如果页码数量大于5
			if($scope.searchMap.pageNo<=3){//如果当前页码<=3,显示前5页;
				lastPage = 5;
				 $scope.firstDot = false;
			}else if($scope.searchMap.pageNo >=lastPage-2  ){//显示后5页
				firstPage = lastPage-4;
				 $scope.lastDot = false;
			}else{//显示以当前页码为中心的5页
				firstPage = $scope.searchMap.pageNo-2;
				lastPage = $scope.searchMap.pageNo+2;
			}
		}else{
			 $scope.firstDot = false;
			 $scope.lastDot = false;
		}
		
		
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLable.push(i);
		}
	}
	//添加搜索项 就是改变searchMap的值
	$scope.addSearchItem = function (key, value){
		if(key == 'category' || key == 'brand' || key == 'price'){//如果 用户点击的是品牌或者 分类
			$scope.searchMap[key] = value;
		}else{//那么用户点击的就是 规格
			$scope.searchMap.spec[key] = value;
		}
		$scope.search();//查询
	}	
	//撤销搜索项
	$scope.removeSearchItem = function (key){
		if(key == 'category' || key == 'brand' || key == 'price'){//如果 用户点击的是品牌或者 分类
			$scope.searchMap[key] = "";
		}else{//那么用户点击的就是 规格
			 delete $scope.searchMap.spec[key];
		}
		$scope.search();//查询
	}	
	//分页查询
	$scope.queryByPage = function (pageNo){
		// $scope.resultMap.totalPages:截止页码
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo = pageNo;
		$scope.search();//查询
	}
	//判断当前页是否是第一页
	$scope.isTopPage = function (){
		if($scope.searchMap.pageNo == 1){
			return true;
			
		}else{
			return false;
		}
	}
	
	//判断当前页是否是最后一页
	$scope.isEndPage = function (){
		if($scope.searchMap.pageNo == $scope.resultMap.totalPages){
			return true;
			
		}else{
			return false;
		}
	}
		
	//价格排序
	$scope.querySort = function (sortField,sort){
		$scope.searchMap.sortField = sortField;
		$scope.searchMap.sort = sort;
		$scope.search();//查询
	}
	//判断关键字是否是品牌
	$scope.keywordsIsBrand = function (){
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			//indexOf():判断 括号里面的字符串是否被外面的字符串包含
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	
	//加关键字
	$scope.loadKeywords = function (){
		//既是把 广告页(首页)的 search 方法 中带的参数传递给 搜索页
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();//查询
	}
	
	
});