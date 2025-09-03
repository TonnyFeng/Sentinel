package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MachineInfoSetDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        List<MachineInfo> array = parser.parseArray(MachineInfo.class);
        // 用 ConcurrentHashMap.newKeySet() 包装
        Set<MachineInfo> set = ConcurrentHashMap.newKeySet();
        set.addAll(array);
        return (T) set;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LBRACKET; // 表示期望是个数组
    }
}