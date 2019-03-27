app.service("contentService",function ($http){
	//根据ID查询呢广告分类列表
	this.findCategoryId = function (categoryId){
		return $http.get("content/findCategoryId.do?categoryId="+categoryId);
	}
})