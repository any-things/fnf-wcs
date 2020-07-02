package operato.logis.dps.model;

import java.util.List;

import operato.fnf.wcs.FnFConstants;
import xyz.anythings.base.entity.JobInput;
import xyz.anythings.base.model.BatchProgressRate;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.ValueUtil;

/**
 * B2C 배치 서머리 정보 리턴 모델
 *  
 * @author yang
 */
public class DpsBatchSummary {
	
	/**
	 * 배치 진행율 
	 */
	private BatchProgressRate rate;
	
	/**
	 * 배치 작업 투입 리스트 
	 */
	private Page<JobInput> inputList;
	
	/**
	 * 투입 가능 박스 수량 
	 */
	private Integer inputableBuckets;
	
	/**
	 * 생성자 
	 * 
	 * @param rate
	 * @param inputList
	 * @param inputableBuckets
	 */
	public DpsBatchSummary(BatchProgressRate rate, Page<JobInput> inputList, Integer inputableBuckets) {
		this.rate = rate;
		this.inputableBuckets = inputableBuckets;
		this.setInputList(inputList);
	}

	public BatchProgressRate getRate() {
		return rate;
	}

	public void setRate(BatchProgressRate rate) {
		this.rate = rate;
	}

	public Page<JobInput> getInputList() {
		return inputList;
	}

	public void setInputList(Page<JobInput> inputList) {
		this.inputList = inputList;
		
		if(ValueUtil.isNotEmpty(inputList)) {
			int splitBoxPcs = ValueUtil.toInteger(SettingUtil.getValue(FnFConstants.BOX_INPUT_SPLIT_BOX_PCS, "20"));
			
			List<JobInput> inputItems = inputList.getList();
			for(JobInput input : inputItems) {
				input.setIsSelectedItem(input.getPlanQty() > splitBoxPcs);
			}
		}
	}

	public Integer getInputableBuckets() {
		return inputableBuckets;
	}

	public void setInputableBuckets(Integer inputableBuckets) {
		this.inputableBuckets = inputableBuckets;
	}

}