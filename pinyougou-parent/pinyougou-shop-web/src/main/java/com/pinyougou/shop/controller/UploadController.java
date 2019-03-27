package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

/*
 * 文件上传
 */
@RestController
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")//注值给变量
	private String file_server_url;//文件服务器地址
	
	
	@RequestMapping("/upload")
	public Result upload(MultipartFile file){
		String filename = file.getOriginalFilename();//获取文件全名
		String extName = filename.substring(filename.lastIndexOf(".")+1);//得到扩展名
		try {
			util.FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
			String uploadFile = client.uploadFile(file.getBytes(), extName);
			String url = file_server_url + uploadFile;//图片的完整地址
			return new Result(true,url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"上传失败!");
		}
		
		
		
	}
	
	
	
	
}
