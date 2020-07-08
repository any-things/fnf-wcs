package operato.fnf.wcs.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import operato.fnf.wcs.entity.WcsMhePasChuteStatus;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.DateUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/mhe_pas_chute_status")
@ServiceDesc(description="WcsMhePasChuteStatus Service API")
public class WcsMhePasChuteStatusController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return WcsMhePasChuteStatus.class;
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
	public WcsMhePasChuteStatus findOne(@PathVariable("id") String id) {
//		return this.getOne(this.entityClass(), id);
		
		List<WcsMhePasChuteStatus> chuteList = new ArrayList<>();
		for(int i = 1 ; i < 201 ; i++) {
			WcsMhePasChuteStatus chute = new WcsMhePasChuteStatus();
			chute.setId(UUID.randomUUID().toString());
			chute.setChuteNo(String.format("%03d", i));
			chute.setStatus("0");
			chute.setInsDatetime(DateUtil.getDate());
			
			chuteList.add(chute);
		}
		AnyOrmUtil.insertBatch(chuteList, 100);
		
		return null;
	}

	@RequestMapping(value="/{id}/exist", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create")
	public WcsMhePasChuteStatus create(@RequestBody WcsMhePasChuteStatus input) {
		return this.createOne(input);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update")
	public WcsMhePasChuteStatus update(@PathVariable("id") String id, @RequestBody WcsMhePasChuteStatus input) {
		return this.updateOne(input);
	}
  
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}  
  
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<WcsMhePasChuteStatus> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	  
	}