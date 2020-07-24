package operato.fnf.wcs;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dev.entity.DiyService;
import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

public class FnfUtils {
	public static final String DPS_RECEIVE_MUTEX_LOCK = "dps.batch.receive.mutex";
	public static final String MUTEX_LOCK_ON = "ON";
	public static final String MUTEX_LOCK_OFF = "OFF";
	public static final String BIZ_TYPE_PKG = "PKG";
	
	public static String today() {
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		String today = dateFormat.format(date);
		return today;
	}
	
	protected static void checkValueEmpty(Map<String, Object> values) {
		for (String key : values.keySet()) {
			if (ValueUtil.isEmpty(values.get(key))) {
				throw new ElidomInputException("VALUE_IS_EMPTY", "Value of '{0}' is empty.", Arrays.asList(key));
			}
		}
	}
	
	/**
	 * label, value
	 * @param values
	 */
	public static void checkValueEmpty(Object ...values) {
		for (int i = 0; i < values.length; i++) {
			if (i % 2 == 0) {
				continue;
			}
			
			Object value = values[i];
			
			if (ValueUtil.isEmpty(value)) {
				String fieldName = String.valueOf(values[i - 1]);
				//throw new ElidomInputException("VALUE_IS_EMPTY", "Value of '{0}' is empty.", Arrays.asList(fieldName));
				throw new ElidomInputException("값[" + fieldName + "]이(가) 빈 값입니다.");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> objectToMap(Object obj) {
		ObjectMapper oMapper = new ObjectMapper();

        // object -> Map
        Map<String, Object> map = oMapper.convertValue(obj, Map.class);
        return map;
	}
	
	public static String queryCustService(String serviceName) throws Exception {
		Query conds = new Query();
		conds.addFilter("name", serviceName);
		conds.addFilter("category", "SERVICE");
		conds.addFilter("scriptType", "SQL");
		DiyService service = BeanUtil.get(IQueryManager.class).selectByCondition(DiyService.class, conds);
		if (ValueUtil.isEmpty(service)) {
			return null;
		}
		
		return service.getServiceLogic();
	}
	
	public static String queryCustServiceWithError(String serviceName) throws Exception {
		Query conds = new Query();
		conds.addFilter("name", serviceName);
		conds.addFilter("category", "SERVICE");
		conds.addFilter("scriptType", "SQL");
		DiyService service = BeanUtil.get(IQueryManager.class).selectByCondition(DiyService.class, conds);
		if (ValueUtil.isEmpty(service)) {
			throw new ValidationException("커스텀 서비스 [" + serviceName + "]가 존재하지 않습니다.");
		}
		
		if (ValueUtil.isEmpty(service.getServiceLogic())) {
			throw new ValidationException("커스텀 서비스 [" + serviceName + "]는 서비스로직이 없습니다.");
		}
		
		return service.getServiceLogic();
	}
	
	public static <T extends Object> T populate(Object from, T to, boolean replaceFlag)
			throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> fromClass = from.getClass();
		Field[] fromFields = fromClass.getDeclaredFields();

		Class<? extends Object> toClass = to.getClass();
		Field[] toFields = toClass.getDeclaredFields();

		for (Field fromField : fromFields) {
			String fieldName = fromField.getName();
			Class<?> type = fromField.getType();
			if (type.getName().contains("xyz.")) {
				continue;
			}
			if (fieldName.equals("serialVersionUID")) {
				continue;
			}
			fromField.setAccessible(true);

			for (Field toField : toFields) {
				String toFieldName = toField.getName();
				if (fieldName.equals("serialVersionUID")) {
					continue;
				}
				toField.setAccessible(true);
				if (replaceFlag) {
					if (toFieldName.equals(fieldName)) {
						toField.set(to, fromField.get(from));
					}
				} else {
					if (ValueUtil.isEmpty(toField.get(to))) {					
						if (toFieldName.equals(fieldName)) {
							toField.set(to, fromField.get(from));
						}
					}
				}
			}
		}

		return to;
	}
	
	public static String bizTypeProcess(String bizType) {
		String workType = BIZ_TYPE_PKG.equalsIgnoreCase(bizType) ? "SHIPBYDAS" : bizType;
		return workType;
	}
	
	public static String bizTypeTitleProcess(String bizType, String title) {
		String desc = BIZ_TYPE_PKG.equalsIgnoreCase(bizType) ? title + " :" + bizType : title;
		return desc;
	}
}
