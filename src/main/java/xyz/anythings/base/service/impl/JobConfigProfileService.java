package xyz.anythings.base.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfig;
import xyz.anythings.base.entity.JobConfigSet;
import xyz.anythings.base.service.api.IJobConfigProfileService;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.util.ValueUtil;

/**
 * IConfigSetService 구현
 * 
 * @author shortstop
 */
@Component
public class JobConfigProfileService extends AbstractExecutionService implements IJobConfigProfileService {
	
	/**
	 * 작업 프로파일 셋 Copy Fields
	 */
	private static final String[] JOB_CONFIG_SET_COPY_FIELDS = new String[] { "stageCd", "jobType", "equipType", "equipCd", "comCd", "confSetCd", "confSetNm", "remark" };
	/**
	 * ConfigSet 
	 */
	private static final String[] CONFIG_COPY_FIELDS = new String[] { "category", "name", "description", "value", "remark", "config" };
	
	/**
	 * 배치 ID - 작업 설정 셋
	 */
	private Map<String, JobConfigSet> configProfiles = new HashMap<String, JobConfigSet>();

	/**
	 * 스테이지 키를 생성
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public String makeStageKey(Long domainId, String stageCd) {
		return domainId + SysConstants.DASH + stageCd;
	}
	
	@Override
	public int buildStageConfigSet(Long domainId) {
		String sql = "select id,domain_id,conf_set_cd,conf_set_nm,stage_cd from job_config_set where domain_id = :domainId and default_flag = :defaultFlag and stage_cd is not null and equip_type is null and equip_cd is null and job_type is null and com_cd is null";
		List<JobConfigSet> confSetList = AnyEntityUtil.searchItems(domainId, false, JobConfigSet.class, sql, "domainId,defaultFlag", domainId, true);
		
		if(ValueUtil.isNotEmpty(confSetList)) {
			for(JobConfigSet confSet : confSetList) {
				this.addStageConfigSet(confSet);
			}
		}
		
		return confSetList.size();
	}

	@Override
	public JobConfigSet addStageConfigSet(JobConfigSet configSet) {
		List<JobConfig> items = AnyEntityUtil.searchDetails(configSet.getDomainId(), JobConfig.class, "jobConfigSetId", configSet.getId());
		configSet.setItems(items);
		String stageKey = this.makeStageKey(configSet.getDomainId(), configSet.getStageCd());
		this.configProfiles.put(stageKey, configSet);
		return configSet;
	}
	
	@Override
	public JobConfigSet getStageConfigSet(Long domainId, String stageCd) {
		String stageKey = this.makeStageKey(domainId, stageCd);
		return this.configProfiles.get(stageKey);
	}
	
	@Override
	public String getStageConfigValue(Long domainId, String stageCd, String key) {
		String stageKey = this.makeStageKey(domainId, stageCd);
		JobConfigSet configSet = this.configProfiles.get(stageKey);
		return configSet == null ? null : configSet.findValue(key);
	}

	@Override
	public String getStageConfigValue(Long domainId, String stageCd, String key, String defaultValue) {
		String value = this.getStageConfigValue(domainId, stageCd, key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

	@Override
	public void clearStageConfigSet(Long domainId, String stageCd) {
		String stageKey = this.makeStageKey(domainId, stageCd);
		this.configProfiles.remove(stageKey);
	}
	
	@Override
	public JobConfigSet getConfigSet(String batchId) {
		return this.configProfiles.get(batchId);
	}

	@Override
	public JobConfigSet addConfigSet(JobBatch batch) {		
		if(ValueUtil.isNotEmpty(batch.getJobConfigSetId())) {
			JobConfigSet configSet = AnyEntityUtil.findEntityById(true, JobConfigSet.class, batch.getJobConfigSetId());
			if(configSet != null) {
				List<JobConfig> sourceItems = AnyEntityUtil.searchDetails(configSet.getDomainId(), JobConfig.class, "jobConfigSetId", configSet.getId());
				configSet.setItems(sourceItems);
				this.configProfiles.put(batch.getId(), configSet);
				return configSet;
			} else {
				return null;
			}
		} else {
			//throw new ElidomRuntimeException("작업 배치 [" + batch.getId() + "]에 작업 설정 프로파일이 설정되지 않았습니다.");
			return null;
		}
	}

	@Override
	public String getConfigValue(JobBatch batch, String key) {
		JobConfigSet configSet = this.configProfiles.get(batch.getId());

		if(configSet == null) {
			configSet = AnyEntityUtil.findEntityById(true, JobConfigSet.class, batch.getJobConfigSetId());
			configSet.setItems(AnyEntityUtil.searchDetails(batch.getDomainId(), JobConfig.class, "jobConfigSetId", batch.getJobConfigSetId()));
			this.configProfiles.put(batch.getId(), configSet);
		}
		
		return configSet != null ? configSet.findValue(key) : null;
	}
	
	@Override
	public String getConfigValue(String batchId, String key) {
		JobConfigSet configSet = this.configProfiles.get(batchId);
		
		if(configSet == null) {
			JobBatch batch = AnyEntityUtil.findEntityBy(Domain.currentDomainId(), true, false, JobBatch.class, "id,job_config_set_id", "id", batchId);
			configSet = AnyEntityUtil.findEntityById(true, JobConfigSet.class, batch.getJobConfigSetId());
			configSet.setItems(AnyEntityUtil.searchDetails(batch.getDomainId(), JobConfig.class, "jobConfigSetId", batch.getJobConfigSetId()));
			this.configProfiles.put(batchId, configSet);
		}
		
		return configSet != null ? configSet.findValue(key) : null;
	}

	@Override
	public String getConfigValue(JobBatch batch, String key, String defaultValue) {
		String value = this.getConfigValue(batch, key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

	@Override
	public String getConfigValue(String batchId, String key, String defaultValue) {
		String value = this.getConfigValue(batchId, key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

	@Override
	public void clearConfigSet(String batchId) {
		this.configProfiles.remove(batchId);
	}

	@Override
	public JobConfigSet copyConfigSet(Long domainId, String templateConfigSetId, String targetSetCd, String targetSetNm) {
		// 1. templateConfigSetId로 템플릿 설정을 조회 
		JobConfigSet sourceSet = AnyEntityUtil.findEntityById(true, JobConfigSet.class, templateConfigSetId);
		JobConfigSet targetSet = AnyValueUtil.populate(sourceSet, new JobConfigSet(), JOB_CONFIG_SET_COPY_FIELDS);
		targetSet.setConfSetCd(targetSetCd);
		targetSet.setConfSetNm(targetSetNm);
		this.queryManager.insert(targetSet);
		// 2. 템플릿 설정 생성
		this.cloneSourceJobConfigItems(sourceSet, targetSet);
		// 3. 복사한 JobConfigSet 리턴
		return targetSet;
	}
	
	/**
	 * sourceSet의 설정 항목을 targetSet의 설정 항목으로 복사
	 * 
	 * @param sourceSet
	 * @param targetSet
	 */
	protected void cloneSourceJobConfigItems(JobConfigSet sourceSet, JobConfigSet targetSet) {
		List<JobConfig> sourceItems = AnyEntityUtil.searchDetails(sourceSet.getDomainId(), JobConfig.class, "jobConfigSetId", sourceSet.getId());
		
		if(ValueUtil.isNotEmpty(sourceItems)) {
			List<JobConfig> targetItems = new ArrayList<JobConfig>(sourceItems.size());
						
			for(JobConfig sourceItem : sourceItems) {
				JobConfig targetItem = AnyValueUtil.populate(sourceItem, new JobConfig(), CONFIG_COPY_FIELDS);
				targetItem.setJobConfigSetId(targetSet.getId());
				targetItems.add(targetItem);
			}
			
			this.queryManager.insertBatch(targetItems);
			targetSet.setItems(targetItems);
		}		
	}

}
