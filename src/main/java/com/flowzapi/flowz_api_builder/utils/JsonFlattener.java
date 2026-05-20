package com.flowzapi.flowz_api_builder.utils;

import java.util.HashMap;
import java.util.Map;

public class JsonFlattener {

    public JsonFlattener() {
    }

    public Map<String, Object> flatten(Map<String, Object> map) {
        Map<String, Object> flattenedMap = new HashMap<>();
        flattenRec(map, flattenedMap, "");
        return flattenedMap;
    }

    public void flattenRec(Map<String, Object> map, Map<String, Object> flattenedMap, String currentPrefix) {
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            String key = currentPrefix.isEmpty() ? entry.getKey() : currentPrefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenRec((Map<String, Object>) value, flattenedMap, key);
            }
            else{
                flattenedMap.put(key, value);
            }
        }
    }
}
