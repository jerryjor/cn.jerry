package cn.jerry.blockchain.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

public class JsonUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);

    // 重量级组件，尽量重用
    private static final ObjectMapper SIMPLE_MAPPER = createSimpleMapper(null);
    private static final ObjectMapper NON_NULL_MAPPER = createSimpleMapper(JsonInclude.Include.NON_NULL);

    public static final String BLANK_MAP = "{}";
    public static final String BLANK_COLLECTION = "[]";

    private JsonUtil() {
        super();
    }

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
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));

        return mapper;
    }

    /**
     * 对象转Json，抛异常
     *
     * @param obj 对象
     * @return json
     * @throws IOException 解析异常
     */
    public static String toJson(Object obj) throws IOException {
        return SIMPLE_MAPPER.writeValueAsString(obj);
    }

    /**
     * 对象转Json，不抛异常，调用方提供失败解决方案
     *
     * @param obj      对象
     * @param failover 失败解决方案
     * @return json
     */
    public static String toJson(Object obj, String failover) {
        try {
            return toJson(obj);
        } catch (IOException e) {
            LOG.error("Failed to read {} to json, failover: {}", obj, failover, e);
            return failover;
        }
    }

    /**
     * 对象转Json，抛异常
     *
     * @param obj 对象
     * @return json，过滤null属性
     * @throws IOException 解析异常
     */
    public static String toJsonNonNull(Object obj) throws IOException {
        return NON_NULL_MAPPER.writeValueAsString(obj);
    }

    /**
     * 对象转Json，不抛异常，调用方提供失败解决方案
     *
     * @param obj      对象
     * @param failover 失败解决方案
     * @return json，过滤null属性
     */
    public static String toJsonNonNull(Object obj, String failover) {
        try {
            return toJsonNonNull(obj);
        } catch (IOException e) {
            LOG.error("Failed to read {} to json, failover: {}", obj, failover, e);
            return failover;
        }
    }

    /**
     * json转对象，抛异常
     *
     * @param json     json字符串
     * @param classOfT 对象
     * @return T
     * @throws IOException 解析异常
     */
    public static <T> T toObject(String json, Class<T> classOfT) throws IOException {
        if (json == null || json.isEmpty()) return null;
        if (classOfT == null) return null;

        return SIMPLE_MAPPER.readValue(json, classOfT);
    }

    /**
     * json转对象，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param classOfT 对象
     * @param failover 失败解决方案
     * @return T
     */
    public static <T> T toObject(String json, Class<T> classOfT, T failover) {
        try {
            return toObject(json, classOfT);
        } catch (IOException e) {
            LOG.error("Failed to read {} to {}, failover: {}", json, classOfT.getName(), failover, e);
            return failover;
        }
    }

    /**
     * json转对象，抛异常
     *
     * @param json    json字符串
     * @param typeOfT 对象
     * @return T
     * @throws IOException 解析异常
     */
    public static <T> T toObject(String json, JavaType typeOfT) throws IOException {
        if (json == null || json.isEmpty()) return null;
        if (typeOfT == null) return null;

        return SIMPLE_MAPPER.readValue(json, typeOfT);
    }

    /**
     * json转对象，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param typeOfT  对象
     * @param failover 失败解决方案
     * @return T
     */
    public static <T> T toObject(String json, JavaType typeOfT, T failover) {
        try {
            return toObject(json, typeOfT);
        } catch (IOException e) {
            LOG.error("Failed to read {} to {}, failover: {}", json, typeOfT, failover, e);
            return failover;
        }
    }

    /**
     * 构建无内部范型的JavaType
     */
    public static JavaType constructSimpleType(Class<?> clazz) {
        return SIMPLE_MAPPER.getTypeFactory().constructType(clazz);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     */
    public static JavaType constructArrayListType(Class<?> eleClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleClass);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     */
    public static JavaType constructArrayListType(JavaType eleType) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleType);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     */
    public static JavaType constructHashSetType(Class<?> eleClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleClass);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     */
    public static JavaType constructHashSetType(JavaType eleType) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleType);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     */
    public static JavaType constructHashMapType(Class<?> keyClass, Class<?> valueClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     */
    public static JavaType constructHashMapType(JavaType keyType, JavaType valueType) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valueType);
    }

    /**
     * 构建JavaType，用于自定义范型
     */
    public static JavaType constructParametricType(Class<?> objClass, Class<?>... genericClasses) {
        return SIMPLE_MAPPER.getTypeFactory().constructParametricType(objClass, genericClasses);
    }

    /**
     * 构建JavaType，用于自定义范型
     */
    public static JavaType constructParametricType(Class<?> objClass, JavaType... genericTypes) {
        return SIMPLE_MAPPER.getTypeFactory().constructParametricType(objClass, genericTypes);
    }

    public static boolean isJson(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof String)) return false;
        String json = (String) obj;
        json = json.trim();
        return !json.isEmpty()
                && ((json.startsWith("{") && json.endsWith("}"))
                || (json.startsWith("[") && json.endsWith("]"))
                || (json.startsWith("\"") && json.endsWith("\"")));
    }
}
