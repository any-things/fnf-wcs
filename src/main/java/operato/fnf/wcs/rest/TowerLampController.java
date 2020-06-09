package operato.fnf.wcs.rest;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import operato.fnf.wcs.entity.TowerLamp;
import xyz.anythings.sys.AnyConstants;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/tower_lamp")
@ServiceDesc(description="TowerLamp Service API")
public class TowerLampController extends AbstractRestService {
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * LAMP Agent 주소
	 */
	@Value("${lamp.agent.rest.url:NULL}")
	private String lampAgentUrl;

	
	@Override
	protected Class<?> entityClass() {
		return TowerLamp.class;
	}
  
	@RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search (Pagination) By Search Conditions")
	public Page<?> index(
		@RequestParam(name="page", required=false) Integer page,
		@RequestParam(name="limit", required=false) Integer limit,
		@RequestParam(name="select", required=false) String select,
		@RequestParam(name="sort", required=false) String sort,
		@RequestParam(name="query", required=false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find one by ID")
	public TowerLamp findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value="/{id}/exist", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create")
	public TowerLamp create(@RequestBody TowerLamp input) {
		return this.createOne(input);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update")
	public TowerLamp update(@PathVariable("id") String id, @RequestBody TowerLamp input) {
		return this.updateOne(input);
	}
  
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}  
  
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<TowerLamp> list) {
		
		boolean result = this.cudMultipleData(this.entityClass(), list);
		
		if(ValueUtil.isEqualIgnoreCase(this.lampAgentUrl, AnyConstants.NULL_CAP_STRING) == false) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
				public void afterCommit(){
					BeanUtil.get(TowerLampController.class).requestAsyncAgent(lampAgentUrl, list);
				}
			});
		}
		
		return result;
	}
	
	@Async
	private void requestAsyncAgent(String restUrl, List<TowerLamp> list) {

		RestTemplate rest = this.getRestTemplate();
		String url = restUrl;
		
		// 1. 플래그에 따른 처리 
		for(TowerLamp towerLamp : list) {
			if(ValueUtil.isEqualIgnoreCase(towerLamp.getCudFlag_(), OrmConstants.CUD_FLAG_CREATE)) {
				// 1.1 create : 연결 시도 
				url = restUrl + "/connect";
				
			} else if(ValueUtil.isEqualIgnoreCase(towerLamp.getCudFlag_(), OrmConstants.CUD_FLAG_UPDATE)) {
				// 1.2 update : 데이터 전송 
				url = restUrl + "/send/true";
				
			} else if(ValueUtil.isEqualIgnoreCase(towerLamp.getCudFlag_(), OrmConstants.CUD_FLAG_DELETE)) {
				// 1.3 delete : 연결 종료 
				url = restUrl + "/disconnect";
				
			} else {
				continue;
			}
			
			try {
				rest.put(url, towerLamp);
			} catch(Exception e) {
				throw new ElidomRuntimeException("TowerLamp Request Error", e);
			}
		}
	}
	
	private RestTemplate getRestTemplate() {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(3000);
		factory.setReadTimeout(3000);
		return new RestTemplate(factory);
	}
	
	/********************************/
	/* AGENT 에서 호출 되는 API
	/********************************/
	
	@RequestMapping(value="/agent/list", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Call Request From Agent : All Tower Lamp List")
	public List<TowerLamp> getAllList() {
		return this.queryManager.selectList(TowerLamp.class, new Query());
	}
	
	@RequestMapping(value="/agent/update/status", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Call Request From Agent : Tower Lamp Status Update")
	public void updateStatusFromAgent(@RequestBody TowerLamp input) {
		input.setUpdatedAt(new Date());
		this.queryManager.update(input, "status", "updatedAt");
	}
	
	@RequestMapping(value="/agent/update/lamp", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Call Request From Agent : Tower Lamp lamp Update")
	public void updateLampFromAgent(@RequestBody TowerLamp input) {
		input.setUpdatedAt(new Date());
		this.queryManager.update(input, "lampR", "lampG", "lampA", "updatedAt");
	}

}