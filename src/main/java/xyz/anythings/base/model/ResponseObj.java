package xyz.anythings.base.model;

import java.util.List;
import java.util.Map;

public class ResponseObj {
	public final static String STATUS_NG = "NG";
	private String msg = "success";
	private String status = "OK";	// FIXME to 200
	private Boolean success = true;
	private List<?> items;
	private Map<String, ?> values;
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public List<?> getItems() {
		return items;
	}
	public void setItems(List<?> items) {
		this.items = items;
	}
	public Map<String, ?> getValues() {
		return values;
	}
	public void setValues(Map<String, ?> values) {
		this.values = values;
	}
}
