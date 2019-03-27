app.controller("baseController", function($scope) {

	// 刷新列表
	$scope.reloadList = function() {
		$scope.search($scope.paginationConf.currentPage,
				$scope.paginationConf.itemsPerPage);
	};

	// 分页控件配置currentPage:当前页 totalItems :总记录数 itemsPerPage:每页记录数 perPageOptions
	// :分页选项 onChange:当页码变更后自动触发的方法
	$scope.paginationConf = {
		currentPage : 1,
		totalItems : 10,
		itemsPerPage : 10,
		perPageOptions : [ 10, 20, 30, 40, 50 ],
		onChange : function() {
			$scope.reloadList();
		}
	};

	// 用户勾选复选框
	$scope.selectIds = [];// 定义一个用户勾选ID的集合
	$scope.deleteSelection = function($event, id) {

		if ($event.target.checked) {
			$scope.selectIds.push(id);// 用户勾选的ID
		} else {
			var index = $scope.selectIds.indexOf(id);// 查找被勾选的ID当前的位置
			$scope.selectIds.splice(index, 1);// 参数1:移除的位置,参数2:移除的长度
		}

	}
	
	$scope.jsonToString = function (jsonString,key){
		//返回的是一个json字符串数组
		var json = JSON.parse(jsonString);
		var value = "";
		for(var i=0;i<json.length;i++){
			if(i>0){
				value += ",";
			}
			value += json[i][key];
		}
		return value;
	}
	

});