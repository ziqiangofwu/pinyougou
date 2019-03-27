package entity;

import java.io.Serializable;
import java.util.List;
/*
 * 分页的结果集列表
 */
public class PageResult implements Serializable {
	
	//代表总记录数
	private Long totle;
	//代表当页的结果数
	private List rows;
	
	
	public PageResult(Long totle, List rows) {
		super();
		this.totle = totle;
		this.rows = rows;
	}

	public PageResult() {
		super();
	}

	public Long getTotle() {
		return totle;
	}

	public void setTotle(Long totle) {
		this.totle = totle;
	}

	public List getRows() {
		return rows;
	}

	public void setRows(List rows) {
		this.rows = rows;
	}
	
	
	
	
}
