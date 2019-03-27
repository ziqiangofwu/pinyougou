 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.totle;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//$location.search():location里面自带方法 search 查询页面上所有的参数封装成数组
		var id = $location.search()['id'];
		if(id==null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;	
				editor.html($scope.entity.goodsDesc.introduction);//商品介绍
				//回显商品图片
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//回显规格选择
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				//转换sku列表中的规格对象
				for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){	
		$scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("保存成功!");
					location.href = "goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	/*//增加
	$scope.add=function(){	
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert("新增成功");
					$scope.entity={};
					editor.html("");//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}*/
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.totle;//更新总记录数
			}			
		);
	}
	
	//图片上传
	//$scope.image_entity={url:{}};
	$scope.uploadFile = function (){
		uploadService.uploadFile().success(function (response){
			if(response.success){//如果上传成功，取出url
				$scope.image_entity.url = response.message; 
			}else{
				alert(response.message);
			}
		});
	}
	$scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
	//将当前上传的图片实体增加到图片列表中
	$scope.add_image_entity = function (){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
    
	//删除图片
	$scope.remove_image_entity = function (index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	//读取一级分类
	$scope.selectItemCat1List = function (){
		itemCatService.findByParentId(0).success(function (response){
			$scope.itemCat1List = response;
		});	
	}
	//读取二级分类
	$scope.$watch("entity.goods.category1Id",function (newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function (response){
			$scope.itemCat2List = response;
		});	
	});
	//读取三级分类
	$scope.$watch("entity.goods.category2Id",function (newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function (response){
			$scope.itemCat3List = response;
		});	
	});
	//读取模板ID
	$scope.$watch("entity.goods.category3Id",function (newValue,oldValue){
		itemCatService.findOne(newValue).success(function (response){
			$scope.entity.goods.typeTemplateId = response.typeId;
		});
	})
	//根据模板ID 读取品牌列表
	$scope.$watch("entity.goods.typeTemplateId",function (newValue,oldValue){
		typeTemplateService.findOne(newValue).success(function (response){
			$scope.typeTemplate = response;//模板对象
			$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表的类型转换 
			//扩展属性
			if($location.search()['id'] == null){
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
			}
			
		});
		//查询规格列表
		typeTemplateService.findSpecList(newValue).success(function (response){
			$scope.specList = response;
		});
	})
	$scope.updateSpecAttribute = function ($event,name,value){
		var Object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
		if(Object != null){
			if($event.target.checked){
				//如果 得到的object 不为空  那么 就往 其里面的attributeValue属性添加 值
				Object.attributeValue.push(value);
			}else{
				//取消勾选
				Object.attributeValue.splice(Object.attributeValue.indexOf(value),1);
				if(Object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice(
							$scope.entity.goodsDesc.specificationItems.indexOf(Object),1);
				}
			}
			
		}else{
			//那么直接添加一个空数组
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	//创建sku列表
	$scope.createItemList = function (){
		$scope.entity.itemList = [{spec:{},price:0,num:999,status:'0',isDefault:'0'}];//列表初始化
		var item = $scope.entity.goodsDesc.specificationItems;
		/*[{"attributeName":"机身内存","attributeValue":["16G","32G","64G","128G"]},
		 {"attributeName":"网络","attributeValue":["移动4G","移动3G"]}]*/
		for(var i=0;i<item.length;i++){
			$scope.entity.itemList = addColumn( $scope.entity.itemList,item[i].attributeName,item[i].attributeValue ); 
		}
	}
	
	addColumn = function (list,columnName,columnValues){
		var newList = [];
		for(var i=0;i<list.length;i++){
			var oldRow = list[i];
			for(var j=0;j<columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
				/*[{spec:{'网络':'3G','内存':'2G'},price:0,num:999,status:'0',isDefault:'0'},
				 {spec:{'网络':'3G','内存':'4G'},price:0,num:999,status:'0',isDefault:'0'},
				 {spec:{'网络':'3G','内存':'8G'},price:0,num:999,status:'0',isDefault:'0'}]*/
//				spec:{内存:32G,网络:3G}
				newRow.spec[columnName] = columnValues[j];
				
				newList.push(newRow);
			}
		}
		
		return  newList;
	}
	
	$scope.status=["未审核","已审核","审核未通过","已关闭"];
	//商品上下架状态
	$scope.isMarkeStatus=["上架","下架"];
	//定义一个集合(商品分类列表)
	$scope.itemCatList = [];
	//查询商品的分类列表
	$scope.findItemCatList = function (){	
		itemCatService.findAll().success(function (response){
			for(var i =0;i<response.length;i++){
				$scope.itemCatList[response[i].id] = response[i].name;		
				
			}
		});
	}
	//判断规格与规格选项是否被勾选
	$scope.checkAttributeValue = function (specName,optionName){
		var item = $scope.entity.goodsDesc.specificationItems;
		var Object = $scope.searchObjectByKey(item,'attributeName',specName);
		if(Object != null){
			//如果能查询到规格选项
			if(Object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	//更新上架状态
	$scope.updateIsMarkerTableStatus = function (status){
		goodsService.updateIsMarkerTableStatus($scope.selectIds,status).success(function (response){
			if(response.success){
				$scope.reloadList();//刷新列表
				$scope.selectIds=[];
			}else{
				alert(response.message);
			}
		});
	}
	
	
	
});	
