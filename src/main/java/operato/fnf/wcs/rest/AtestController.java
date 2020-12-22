package operato.fnf.wcs.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import operato.fnf.wcs.query.store.FnFDasQueryStore;
import operato.fnf.wcs.query.store.FnFDpsQueryStore;
import operato.fnf.wcs.query.store.FnFSmsQueryStore;
import operato.logis.das.query.store.DasQueryStore;
import operato.logis.dps.query.store.DpsBatchQueryStore;
import operato.logis.dps.query.store.DpsBoxQueryStore;
import operato.logis.dps.query.store.DpsInspectionQueryStore;
import operato.logis.dps.query.store.DpsPickQueryStore;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceManager;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/a_test")
@ServiceDesc(description="Automation Test")
public class AtestController {
	
	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(AtestController.class);

	@Autowired
	private FnFDasQueryStore fnfDasQueryStore;
	
	@Autowired
	private FnFDpsQueryStore fnfDpsQueryStore;
	
	@Autowired
	private FnFSmsQueryStore fnfSmsQueryStore;
	
	@Autowired
	private DasQueryStore dasQueryStore;
	
	@Autowired
	private DpsBatchQueryStore dpsBatchQueryStore;
	
	@Autowired
	private DpsBoxQueryStore dpsBoxQueryStore;
	
	@Autowired
	private DpsInspectionQueryStore dpsInspectionQueryStore;
	
	@Autowired
	private DpsPickQueryStore dpsPickQueryStore;
	
	@RequestMapping(value = "/query/store", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Filtering Address")
	public Object testQueryStore() {
		this.printQuery(this.fnfDasQueryStore.getDasBatchTotalResultSummary());
		this.printQuery(this.fnfDpsQueryStore.getDpsCalc1HourResultSummary());
		this.printQuery(this.fnfSmsQueryStore.getSdasCalc10MinuteResultSummary());
		this.printQuery(this.dasQueryStore.getOrderSummaryToReceive());
		this.printQuery(this.dpsBatchQueryStore.getBatchMapBoxIdAndSeqQuery());
		this.printQuery(this.dpsBoxQueryStore.getBoxIdUniqueCheckQuery());
		this.printQuery(this.dpsInspectionQueryStore.getSearchInspectionItemsQuery());
		this.printQuery(this.dpsPickQueryStore.getFindNextMappingJobQuery());
		
		return "OK";
	}
	
	@RequestMapping(value = "/test/datasource", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Datasource test")
	public Object testDatasource() {
		DataSourceManager dsMgr = BeanUtil.get(DataSourceManager.class);
		IQueryManager queryMgr = dsMgr.getQueryManager("WMS");
		
		return queryMgr.getDbType();
	}
	
	private void printQuery(String query) {
		this.logger.info(query);
	}
}
