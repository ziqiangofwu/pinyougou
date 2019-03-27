app.controller("contentController",function ($scope,contentService){
	
	
	$scope.contentList = [];//定义一个广告列表对象
	$scope.findCategoryId = function (categoryId){
		contentService.findCategoryId(categoryId).success(function (response){
			$scope.contentList[categoryId] = response;
		});
	}
	
	//搜索 传递参数
	$scope.search = function (){
		location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
	
});