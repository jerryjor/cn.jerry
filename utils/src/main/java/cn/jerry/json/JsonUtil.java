package cn.jerry.json;

import cn.jerry.log4j2.annotation.LogManager;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonUtil {
    private static final Logger LOG = LogManager.getLogger(JsonUtil.class);

    private static final ObjectMapper SIMPLE_MAPPER = createSimpleMapper(null);
    private static final ObjectMapper NON_NULL_MAPPER = createSimpleMapper(Include.NON_NULL);

    private JsonUtil() {
        super();
    }

    private static ObjectMapper createSimpleMapper(Include include) {
        ObjectMapper mapper = new ObjectMapper();

        // Include.ALWAYS 默认
        // Include.NON_DEFAULT 属性为默认值不序列化
        // Include.NON_EMPTY 属性为 "" 或者为 NULL 都不序列化
        // Include.NON_NULL 属性为NULL 不序列化
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }
        //
        try {
            mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        } catch (NoSuchMethodError e) {
            //
        }
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        } catch (NoSuchMethodError e) {
            //
        }
        try {
            mapper.disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        } catch (NoSuchMethodError e) {
            //
        }
        // 日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 时区
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        return mapper;
    }

    /**
     * 使用json把子对象转为父对象
     *
     * @param child      子对象
     * @param superClass 父对象类
     * @return super
     * @throws IOException 解析异常
     */
    public static <S, C extends S> S transChildToSuper(C child, Class<S> superClass) throws IOException {
        return toObject(toJson(child), superClass);
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
     * json转HashMap，抛异常
     *
     * @param json     json字符串
     * @param classOfK 键类
     * @param classOfV 值类
     * @return HashMap<K, V>
     * @throws IOException 解析异常
     */
    public static <K, V> HashMap<K, V> toHashMap(String json, Class<K> classOfK, Class<V> classOfV) throws IOException {
        if (classOfK == null || classOfV == null) return null;

        return toObject(json, constructHashMapType(classOfK, classOfV));
    }

    /**
     * json转HashMap，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param classOfK 键类
     * @param classOfV 值类
     * @param failover 失败解决方案
     * @return HashMap<K, V>
     */
    public static <K, V> HashMap<K, V> toHashMap(String json, Class<K> classOfK, Class<V> classOfV, HashMap<K, V> failover) {
        try {
            return toHashMap(json, classOfK, classOfV);
        } catch (IOException e) {
            LOG.error("Failed to read {} to HashMap<{},{}>, failover: {}", json, classOfK.getName(), classOfV.getName(), failover, e);
            return failover;
        }
    }

    /**
     * json转HashMap，抛异常
     *
     * @param json    json字符串
     * @param typeOfK 键类
     * @param typeOfV 值类
     * @return HashMap<K, V>
     * @throws IOException 解析异常
     */
    public static <K, V> HashMap<K, V> toHashMap(String json, JavaType typeOfK, JavaType typeOfV) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructHashMapType(typeOfK, typeOfV));
    }

    /**
     * json转HashMap，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param typeOfK  键类
     * @param typeOfV  值类
     * @param failover 失败解决方案
     * @return HashMap<K, V>
     */
    public static <K, V> HashMap<K, V> toHashMap(String json, JavaType typeOfK, JavaType typeOfV, HashMap<K, V> failover) {
        try {
            return toHashMap(json, typeOfK, typeOfV);
        } catch (IOException e) {
            LOG.error("Failed to read {} to HashMap<{},{}>, failover: {}", json, typeOfK, typeOfV, failover, e);
            return failover;
        }
    }

    /**
     * json转LinkedHashMap，抛异常
     *
     * @param json     json字符串
     * @param classOfK 键类
     * @param classOfV 值类
     * @return LinkedHashMap<K, V>
     * @throws IOException 解析异常
     */
    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(String json, Class<K> classOfK, Class<V> classOfV) throws IOException {
        if (classOfK == null || classOfV == null) return null;

        return toObject(json, constructLinkedHashMapType(classOfK, classOfV));
    }

    /**
     * json转LinkedHashMap，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param classOfK 键类
     * @param classOfV 值类
     * @param failover 失败解决方案
     * @return LinkedHashMap<K, V>
     */
    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(String json, Class<K> classOfK, Class<V> classOfV, LinkedHashMap<K, V> failover) {
        try {
            return toLinkedHashMap(json, classOfK, classOfV);
        } catch (IOException e) {
            LOG.error("Failed to read {} to LinkedHashMap<{},{}>, failover: {}", json, classOfK.getName(), classOfV.getName(), failover, e);
            return failover;
        }
    }

    /**
     * json转LinkedHashMap，抛异常
     *
     * @param json    json字符串
     * @param typeOfK 键类
     * @param typeOfV 值类
     * @return LinkedHashMap<K, V>
     * @throws IOException 解析异常
     */
    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(String json, JavaType typeOfK, JavaType typeOfV) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructLinkedHashMapType(typeOfK, typeOfV));
    }

    /**
     * json转LinkedHashMap，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param typeOfK  键类
     * @param typeOfV  值类
     * @param failover 失败解决方案
     * @return LinkedHashMap<K, V>
     */
    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(String json, JavaType typeOfK, JavaType typeOfV, LinkedHashMap<K, V> failover) {
        try {
            return toLinkedHashMap(json, typeOfK, typeOfV);
        } catch (IOException e) {
            LOG.error("Failed to read {} to LinkedHashMap<{},{}>, failover: {}", json, typeOfK, typeOfV, failover, e);
            return failover;
        }
    }

    /**
     * json转ArrayList，抛异常
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @return ArrayList<E>
     * @throws IOException 解析异常
     */
    public static <E> ArrayList<E> toArrayList(String json, Class<E> classOfE) throws IOException {
        if (classOfE == null) return null;

        return toObject(json, constructArrayListType(classOfE));
    }

    /**
     * json转ArrayList，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @param failover 失败解决方案
     * @return ArrayList<E>
     */
    public static <E> ArrayList<E> toArrayList(String json, Class<E> classOfE, ArrayList<E> failover) {
        try {
            return toArrayList(json, classOfE);
        } catch (IOException e) {
            LOG.error("Failed to read {} to ArrayList<{}>, failover: {}", json, classOfE.getName(), failover, e);
            return failover;
        }
    }

    /**
     * json转ArrayList，抛异常
     *
     * @param json    json字符串
     * @param typeOfE 元素类
     * @return ArrayList<E>
     * @throws IOException 解析异常
     */
    public static <E> ArrayList<E> toArrayList(String json, JavaType typeOfE) throws IOException {
        if (typeOfE == null) return null;

        return toObject(json, constructArrayListType(typeOfE));
    }

    /**
     * json转ArrayList，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param typeOfE  元素类
     * @param failover 失败解决方案
     * @return ArrayList<E>
     */
    public static <E> ArrayList<E> toArrayList(String json, JavaType typeOfE, ArrayList<E> failover) {
        try {
            return toArrayList(json, typeOfE);
        } catch (IOException e) {
            LOG.error("Failed to read {} to ArrayList<{}>, failover: {}", json, typeOfE, failover, e);
            return failover;
        }
    }

    /**
     * json转HashSet，抛异常
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @return HashSet<E>
     * @throws IOException 解析异常
     */
    public static <E> HashSet<E> toHashSet(String json, Class<E> classOfE) throws IOException {
        if (classOfE == null) return null;

        return toObject(json, constructHashSetType(classOfE));
    }

    /**
     * json转HashSet，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @param failover 失败解决方案
     * @return HashSet<E>
     */
    public static <E> HashSet<E> toHashSet(String json, Class<E> classOfE, HashSet<E> failover) {
        try {
            return toHashSet(json, classOfE);
        } catch (IOException e) {
            LOG.error("Failed to read {} to HashSet<{}>, failover: {}", json, classOfE.getName(), failover, e);
            return failover;
        }
    }

    /**
     * json转HashSet，抛异常
     *
     * @param json    json字符串
     * @param typeOfE 元素类
     * @return HashSet<E>
     * @throws IOException 解析异常
     */
    public static <E> HashSet<E> toHashSet(String json, JavaType typeOfE) throws IOException {
        if (typeOfE == null) return null;

        return toObject(json, constructHashSetType(typeOfE));
    }

    /**
     * json转HashSet，不抛异常，调用方提供失败解决方案
     *
     * @param json     json字符串
     * @param typeOfE  元素类
     * @param failover 失败解决方案
     * @return HashSet<E>
     */
    public static <E> HashSet<E> toHashSet(String json, JavaType typeOfE, HashSet<E> failover) {
        try {
            return toHashSet(json, typeOfE);
        } catch (IOException e) {
            LOG.error("Failed to read {} to HashSet<{}>, failover: {}", json, typeOfE, failover, e);
            return failover;
        }
    }

    /**
     * 构建无内部范型的JavaType
     *
     * @param clazz 类
     * @return JavaType
     */
    public static JavaType constructSimpleType(Class<?> clazz) {
        return SIMPLE_MAPPER.getTypeFactory().constructType(clazz);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleClass element的类
     * @return JavaType
     */
    public static JavaType constructArrayListType(Class<?> eleClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleClass);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleType element的类
     * @return JavaType
     */
    public static JavaType constructArrayListType(JavaType eleType) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleType);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleClass element的类
     * @return JavaType
     */
    public static JavaType constructHashSetType(Class<?> eleClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleClass);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleType element的类
     * @return JavaType
     */
    public static JavaType constructHashSetType(JavaType eleType) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleType);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     *
     * @param keyClass   key的类
     * @param valueClass value泛型
     * @return JavaType
     */
    public static JavaType constructHashMapType(Class<?> keyClass, Class<?> valueClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     *
     * @param keyType   key的类
     * @param valueType value泛型
     * @return JavaType
     */
    public static JavaType constructHashMapType(JavaType keyType, JavaType valueType) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valueType);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     *
     * @param keyClass   key的类
     * @param valueClass value泛型
     * @return JavaType
     */
    public static JavaType constructLinkedHashMapType(Class<?> keyClass, Class<?> valueClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, keyClass, valueClass);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     *
     * @param keyType   key的类
     * @param valueType value泛型
     * @return JavaType
     */
    public static JavaType constructLinkedHashMapType(JavaType keyType, JavaType valueType) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType);
    }

    /**
     * 构建JavaType，用于自定义范型
     *
     * @param objClass       Object真实的类
     * @param genericClasses Object内部泛型
     * @return JavaType
     */
    public static JavaType constructParametricType(Class<?> objClass, Class<?>... genericClasses) {
        return SIMPLE_MAPPER.getTypeFactory().constructParametricType(objClass, genericClasses);
    }

    /**
     * 构建JavaType，用于自定义范型
     *
     * @param objClass     Object真实的类
     * @param genericTypes Object内部泛型
     * @return JavaType
     */
    public static JavaType constructParametricType(Class<?> objClass, JavaType... genericTypes) {
        return SIMPLE_MAPPER.getTypeFactory().constructParametricType(objClass, genericTypes);
    }

    /**
     * 格式化json，用于显示/打印等场景
     *
     * @param jsonStr   原始json
     * @param indentStr 缩进符号，一般是\t或两个空格或四个空格
     * @return 格式化后的json
     */
    public static String formatJsonStr(String jsonStr, String indentStr) {
        if (jsonStr == null) return null;
        jsonStr = jsonStr.trim();
        char c = jsonStr.charAt(0);
        if (c != '{' && c != '[') return jsonStr;


        String lineBr = "\r\n";
        int nodeDeep = 0;
        int start = 0;
        char lastDeclare = ' ';
        char declare;
        String nodeValue;
        String indent;
        StringBuilder builder = new StringBuilder();
        while (jsonStr.length() > 0) {
            start = findNextDeclare(jsonStr);
            if (start == -1) {
                // 找不到{}[]了，结束
                break;
            }
            nodeValue = jsonStr.substring(0, start).trim();
            if (nodeValue.isEmpty()) {
                // do nothing...
            } else if (",".equals(nodeValue)) {
                builder.append(nodeValue);
            } else {
                indent = genIndent(indentStr, nodeDeep);
                if (nodeValue.charAt(0) != ',') {
                    builder.append(indent);
                }
                builder.append(nodeValue.replace(",", "," + lineBr + indent));
            }
            declare = jsonStr.charAt(start);
            switch (declare) {
                case '{':
                case '[':
                    indent = genIndent(indentStr, nodeDeep);
                    switch (lastDeclare) {
                        case '[':
                            builder.append(indent).append(declare).append(lineBr);
                            break;
                        case '{':
                            if (nodeValue.isEmpty() || ",".equals(nodeValue)) {
                                builder.append(lineBr).append(indent).append(declare).append(lineBr);
                            } else {
                                builder.append(declare).append(lineBr);
                            }
                            break;
                        default:
                            builder.append(lineBr).append(indent).append(declare).append(lineBr);
                            break;
                    }
                    nodeDeep++;
                    break;
                case '}':
                case ']':
                    nodeDeep--;
                    indent = genIndent(indentStr, nodeDeep);
                    builder.append(lineBr).append(indent).append(declare);
                    break;
                default:
                    // 不存在的
            }
            jsonStr = jsonStr.substring(start + 1);
            lastDeclare = declare;
        }
        return builder.toString();
    }

    private static int findNextDeclare(String jsonStr) {
        int minIndex = jsonStr.length();
        int index;
        for (String flag : new String[]{"{", "}", "[", "]"}) {
            index = jsonStr.indexOf(flag);
            if (index != -1 && index < minIndex) {
                minIndex = index;
            }
        }
        return minIndex;
    }

    private static String genIndent(String indentStr, int nodeDeep) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodeDeep; i++) {
            builder.append(indentStr);
        }
        return builder.toString();
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
