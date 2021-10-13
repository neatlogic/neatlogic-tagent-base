package codedriver.framework.tagent.util;

import com.alibaba.fastjson.JSONObject;
import org.json.simple.JSONArray;

import java.lang.reflect.Field;
import java.util.*;

public class TagentObjectUtil {
    public static Map<String, String> parseJSON2Map(JSONObject jsonObj) {
        Map<String, String> map = new HashMap<String, String>();
        for (Object k : jsonObj.keySet()) {
            Object v = jsonObj.get(k);
            // 如果内层还是数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                Iterator<JSONObject> it = ((JSONArray) v).iterator();
                while (it.hasNext()) {
                    JSONObject json2 = it.next();
                    list.add(parseJSON2Map(json2));
                }
                map.put(k.toString(), JSONArray.toJSONString(list).toString());
            } else {
                map.put(k.toString(), v.toString());
            }
        }
        return map;
    }

    public static Map<String, String> ConvertObjToMap(Object obj) {
        Map<String, String> reMap = new HashMap<>();
        if (obj == null)
            return null;
        Field[] fields = obj.getClass().getDeclaredFields();
        try {
            for (int i = 0; i < fields.length; i++) {
                try {
                    Field f = obj.getClass().getDeclaredField(fields[i].getName());
                    f.setAccessible(true);
                    Object o = f.get(obj);
                    reMap.put(fields[i].getName(), o == null ? "" : o.toString());
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return reMap;
    }
}
