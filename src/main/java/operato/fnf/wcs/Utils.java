package operato.fnf.wcs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.util.ValueUtil;

public class Utils {
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
}
