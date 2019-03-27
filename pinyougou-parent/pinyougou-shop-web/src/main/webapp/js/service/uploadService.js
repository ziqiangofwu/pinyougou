//文件上传服务层
app.service("uploadService",function($http){
	this.uploadFile = function (){
		var formData = new FormData();
		//file.files[0]:   file::文件上传框的name  files[0]:取第一个name
		formData.append("file",file.files[0]);
		return $http({
			method:"post",
			url:"../upload.do",
			data:formData,
			headers:{"Content-Type":undefined},
			transformRequest:angular.identity
		});
	}
}); 