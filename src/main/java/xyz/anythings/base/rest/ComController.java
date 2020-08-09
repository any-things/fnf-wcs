package xyz.anythings.base.rest;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
import xyz.anythings.base.model.ResponseObj;
import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;
//import xyz.xmes.cust.model.WsResponseObj;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/wcs")
@ServiceDesc(description = "Common Controller")
public class ComController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return null;
	}
	
	@RequestMapping(value = "/{serviceUrl}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ResponseObj dispatchService(HttpServletRequest request, @PathVariable("serviceUrl") String serviceUrl)
			throws Exception {
		String queryString = request.getQueryString();
		Map<String, Object> params = this.parseParams(queryString);
		
		String serviceName = ValueUtil.toCamelCase(serviceUrl, '_');
		Object service = BeanUtil.get(serviceName);

		Method method = service.getClass().getMethod(serviceName, Map.class);
		ResponseObj response = null;
		try {
			response = (ResponseObj) method.invoke(service, params);
		} catch (InvocationTargetException e) {
			throw (Exception) e.getTargetException();
		} catch (Exception e) {
			throw e;
		}

		return response;
	}

	@RequestMapping(value = "/{serviceUrl}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Common Service Port")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ResponseObj dispatchService(@RequestBody Map<String, Object> params,
			@PathVariable("serviceUrl") String serviceUrl) throws Exception {
		if (ValueUtil.isEmpty(params)) {
			throw new ElidomInputException("need params"); // FIXME
		}

		String serviceName = ValueUtil.toCamelCase(serviceUrl, '_');
		Object service = BeanUtil.get(serviceName);

		Method method = service.getClass().getMethod(serviceName, Map.class);
		ResponseObj response = null;
		try {
			response = (ResponseObj) method.invoke(service, params);
		} catch (InvocationTargetException e) {
			throw (Exception) e.getTargetException();
		} catch (Exception e) {
			throw e;
		}
		return response;
	}

	private Map<String, Object> parseParams(String paramString) throws UnsupportedEncodingException {
		if (ValueUtil.isEmpty(paramString)) {
			return new HashMap<>();
		}

		String charset = "utf-8";
		String[] params = paramString.split("&");

		Map<String, Object> paramsMap = new HashMap<String, Object>();
		for (String p : params) {
			String[] unit = p.split("=");
			if (unit.length == 2) {
				paramsMap.put(unit[0], URLDecoder.decode(unit[1], charset));
			}
		}

		return paramsMap;
	}
	
	@RequestMapping(value = "/update_multiple/{serviceUrl}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public ResponseObj multipleUpdate(@RequestBody List<Object> list, @PathVariable("serviceUrl") String serviceUrl) throws Exception {
		String serviceName = ValueUtil.toCamelCase(serviceUrl, '_');
		Object service = BeanUtil.get(serviceName);

		Method method = service.getClass().getMethod(serviceName, Map.class);
		ResponseObj response = null;
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("list", list);
			response = (ResponseObj) method.invoke(service, params);
		} catch (InvocationTargetException e) {
			throw (Exception) e.getTargetException();
		} catch (Exception e) {
			throw e;
		}
		return response;
	}
}
