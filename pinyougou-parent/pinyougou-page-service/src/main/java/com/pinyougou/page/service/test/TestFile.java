package com.pinyougou.page.service.test;

import java.io.File;

public class TestFile {

	public static void main(String[] args) {
		File file = new File("a.txt");
		file.delete();
	}
}
