package com.ule.merchant.demo.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    // 重量级组件，尽量重用
    private static final ObjectMapper SIMPLE_MAPPER = createSimpleMapper(null);
    private static final ObjectMapper NON_NULL_MAPPER = createSimpleMapper(JsonInclude.Include.NON_NULL);


    public static final String BLANK_MAP = "{}";
    public static final String BLANK_COLLECTION = "[]";

    private static ObjectMapper createSimpleMapper(JsonInclude.Include include) {
        ObjectMapper mapper = new ObjectMapper();

        // Include.ALWAYS 默认
        // Include.NON_DEFAULT 属性为默认值不序列化
        // Include.NON_EMPTY 属性为 "" 或者为 NULL 都不序列化
        // Include.NON_NULL 属性为NULL 不序列化
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }
        //
        mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        mapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        mapper.disable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        // 日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return mapper;
    }

    /**
     * 对象转Json，抛异常
     *
     * @param obj 对象
     * @return
     * @throws IOException
     */
    public static String toJson(Object obj) throws IOException {
        return SIMPLE_MAPPER.writeValueAsString(obj);
    }

    /**
     * 对象转Json，抛异常
     *
     * @param obj 对象
     * @return
     * @throws IOException
     */
    public static String toJsonNonNull(Object obj) throws IOException {
        return NON_NULL_MAPPER.writeValueAsString(obj);
    }

    /**
     * json转对象，抛异常
     *
     * @param json
     * @param cls
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json, Class<T> cls) throws IOException {
        if (json == null || json.isEmpty()) {
            return null;
        }

        return SIMPLE_MAPPER.readValue(json, cls);
    }

    /**
     * json转自定义对象，抛异常
     *
     * @param json
     * @param objClass       Object真实的类
     * @param genericClasses Object内部泛型列表
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json, Class<? extends T> objClass, Class<?>... genericClasses)
            throws IOException {
        if (json == null || json.isEmpty()) {
            return null;
        }

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructParametricType(objClass, genericClasses);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转Map，抛异常
     *
     * @param json
     * @param keyCls
     * @param valueCls
     * @return
     * @throws IOException
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyCls, Class<V> valueCls)
            throws IOException {
        if (json == null || json.isEmpty()) {
            return null;
        }

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyCls, valueCls);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转List，抛异常
     *
     * @param json
     * @param eleCls
     * @return
     * @throws IOException
     */
    public static <T> List<T> toList(String json, Class<T> eleCls) throws IOException {
        if (json == null || json.isEmpty()) {
            return null;
        }

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleCls);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转MapMap
     *
     * @param json
     * @param keyCls
     * @param key1Cls
     * @param val1Cls
     * @return
     * @throws IOException
     */
    public static <K, K1, V1> Map<K, Map<K1, V1>> toMapMap(String json, Class<K> keyCls, Class<K1> key1Cls,
            Class<V1> val1Cls) throws IOException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        JavaType javaTypeKey = SIMPLE_MAPPER.getTypeFactory().constructType(keyCls);
        JavaType javaTypeValue = SIMPLE_MAPPER.getTypeFactory().constructParametricType(HashMap.class, key1Cls, val1Cls);
        JavaType javaTypeResult = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, javaTypeKey,
                javaTypeValue);
        return SIMPLE_MAPPER.readValue(json, javaTypeResult);
    }

}
