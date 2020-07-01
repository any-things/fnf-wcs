package operato.fnf.wcs.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

/**
 * WMS 데이터베이스 assort_item 뷰에 대한 링크 테이블의 엔티티 
 * 
 * @author shortstop
 */
@Table(name = "assort_item", ignoreDdl = true, idStrategy = GenerationRule.NONE)
public class WmsAssortItem extends xyz.elidom.orm.entity.basic.AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -3601219852210491698L;

	@Ignore
	private String id;

	@PrimaryKey
	@Column (name = "strr_id", nullable = false, length = 20)
	private String strrId;

	@PrimaryKey
	@Column (name = "item_cd", nullable = false, length = 30)
	private String itemCd;

	@Column (name = "child_item_cd", nullable = true, length = 30)
	private String childItemCd;
	
	@Column (name = "item_style", nullable = true, length = 30)
	private String itemStyle;

	@Column (name = "item_color", nullable = true, length = 10)
	private String itemColor;

	@Column (name = "item_size", nullable = true, length = 10)
	private String itemSize;
	
	@Column (name = "qty", nullable = true, length = 10)
	private Integer qty;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStrrId() {
		return strrId;
	}

	public void setStrrId(String strrId) {
		this.strrId = strrId;
	}

	public String getItemCd() {
		return itemCd;
	}

	public void setItemCd(String itemCd) {
		this.itemCd = itemCd;
	}

	public String getChildItemCd() {
		return childItemCd;
	}

	public void setChildItemCd(String childItemCd) {
		this.childItemCd = childItemCd;
	}

	public String getItemStyle() {
		return itemStyle;
	}

	public void setItemStyle(String itemStyle) {
		this.itemStyle = itemStyle;
	}

	public String getItemColor() {
		return itemColor;
	}

	public void setItemColor(String itemColor) {
		this.itemColor = itemColor;
	}

	public String getItemSize() {
		return itemSize;
	}

	public void setItemSize(String itemSize) {
		this.itemSize = itemSize;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

}
