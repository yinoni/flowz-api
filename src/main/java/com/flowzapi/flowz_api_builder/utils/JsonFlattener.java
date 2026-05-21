package com.flowzapi.flowz_api_builder.utils;

import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonFlattener {

    @Autowired
    private ObjectMapper objectMapper;

    public JsonFlattener() {
    }

    public Map<String, Object> flatten(Map<String, Object> map, Map<String, String> extractorMap) {
        Map<String, Object> flattenedMap = new HashMap<>();
        flattenRec(map, flattenedMap, "", extractorMap);
        return flattenedMap;
    }

    public void flattenRec(Map<String, Object> map, Map<String, Object> flattenedMap, String currentPrefix, Map<String, String> extractorMap) {
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            String key = currentPrefix.isEmpty() ? entry.getKey() : currentPrefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                if(extractorMap.containsValue(key)){
                    try{
                        String jsonString = objectMapper.writeValueAsString(value);
                        flattenedMap.put(key, jsonString);
                    }
                    catch (Exception e){
                        flattenedMap.put(key, value);
                    }
                }
                else
                    flattenRec((Map<String, Object>) value, flattenedMap, key, extractorMap);
            }
            else{
                flattenedMap.put(key, value);
            }
        }
    }
}
