package cn.jerry.json;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {
    private static final ObjectMapper SIMPLE_MAPPER = createSimpleMapper(null);
    private static final ObjectMapper NON_NULL_MAPPER = createSimpleMapper(Include.NON_NULL);

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
     * 使用json把子对象转为父对象
     *
     * @param child      子对象
     * @param superClass 父对象类
     * @return
     * @throws IOException
     */
    public static <S, C extends S> S transChildToSuper(C child, Class<S> superClass)
            throws IOException {
        return toObject(toJson(child), superClass);
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
     * @param json     json字符串
     * @param classOfT 对象
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json, Class<T> classOfT) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, classOfT);
    }

    /**
     * json转对象，抛异常
     *
     * @param json     json字符串
     * @param classOfT 对象类
     * @param classOfE 对象内部泛型类
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json, Class<T> classOfT, Class<?>... classOfE)
            throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructParametricType(classOfT, classOfE);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转Map，抛异常
     *
     * @param json     json字符串
     * @param classOfK 键类
     * @param classOfV 值类
     * @return
     * @throws IOException
     */
    public static <K, V> Map<K, V> toHashMap(String json, Class<K> classOfK, Class<V> classOfV) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, classOfK, classOfV);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转Map，默认字符串Key，抛异常
     *
     * @param json      json字符串
     * @param classOfK  键类
     * @param classOfV  值类
     * @param classOfVE 值的内部泛型类
     * @return
     * @throws IOException
     */
    public static <K, V> Map<K, V> toHashMap(String json, Class<K> classOfK, Class<V> classOfV, Class<?>... classOfVE)
            throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType keyType = SIMPLE_MAPPER.getTypeFactory().constructType(classOfK);
        JavaType valType = SIMPLE_MAPPER.getTypeFactory().constructParametricType(classOfV, classOfVE);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转List，抛异常
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @return
     * @throws IOException
     */
    public static <T> List<T> toArrayList(String json, Class<T> classOfE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, classOfE);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转List，抛异常
     *
     * @param json      json字符串
     * @param classOfE  元素类
     * @param classOfEE 元素的内部泛型类
     * @return
     * @throws IOException
     */
    public static <T> List<T> toArrayList(String json, Class<T> classOfE, Class<?>... classOfEE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType eleType = SIMPLE_MAPPER.getTypeFactory().constructParametricType(classOfE, classOfEE);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转HashSet，抛异常
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @return
     * @throws IOException
     */
    public static <T> Set<T> toHashSet(String json, Class<T> classOfE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, classOfE);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转HashSet，抛异常
     *
     * @param json      json字符串
     * @param classOfE  元素类
     * @param classOfEE 元素的内部泛型类
     * @return
     * @throws IOException
     */
    public static <T> Set<T> toHashSet(String json, Class<T> classOfE, Class<?>... classOfEE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType eleType = SIMPLE_MAPPER.getTypeFactory().constructParametricType(classOfE, classOfEE);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转HashMap，内部元素填充为HashMap，抛异常
     *
     * @param json      json字符串
     * @param classOfK  主map键类
     * @param classOfK1 子map键类
     * @param classOfV1 子map值类
     * @return
     * @throws IOException
     */
    public static <K, K1, V1> Map<K, Map<K1, V1>> toHashMapFillWithHashMap(String json, Class<K> classOfK,
            Class<K1> classOfK1, Class<V1> classOfV1) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType keyType = SIMPLE_MAPPER.getTypeFactory().constructType(classOfK);
        JavaType valType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, classOfK1, classOfV1);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转HashMap，内部元素填充为ArrayList，抛异常
     *
     * @param json      json字符串
     * @param classOfK  主map键类
     * @param classOfE1 子List元素类
     * @return
     * @throws IOException
     */
    public static <K, E1> Map<K, List<E1>> toHashMapFillWithArrayList(String json, Class<K> classOfK,
            Class<E1> classOfE1) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType keyType = SIMPLE_MAPPER.getTypeFactory().constructType(classOfK);
        JavaType valType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, classOfE1);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转HashMap，内部元素填充为HashSet，抛异常
     *
     * @param json      json字符串
     * @param classOfK  主map键类
     * @param classOfE1 子Set元素类
     * @return
     * @throws IOException
     */
    public static <K, E1> Map<K, Set<E1>> toHashMapFillWithHashSet(String json, Class<K> classOfK,
            Class<E1> classOfE1) throws IOException {
        if (json == null || json.isEmpty()) return null;

        JavaType keyType = SIMPLE_MAPPER.getTypeFactory().constructType(classOfK);
        JavaType valType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, classOfE1);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转ArrayList，内部元素填充为HashMap，抛异常
     *
     * @param json      json字符串
     * @param classOfK1 子map键类
     * @param classOfV1 子map值类
     * @return
     * @throws IOException
     */
    public static <K1, V1> List<Map<K1, V1>> toArrayListFillWithHashMap(String json, Class<K1> classOfK1,
            Class<V1> classOfV1) throws IOException {
        if (json == null || json.isEmpty()) return null;
        JavaType eleType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, classOfK1, classOfV1);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    /**
     * json转HashSet，内部元素填充为HashMap，抛异常
     *
     * @param json      json字符串
     * @param classOfK1 子map键类
     * @param classOfV1 子map值类
     * @return
     * @throws IOException
     */
    public static <K1, V1> Set<Map<K1, V1>> toHashSetFillWithHashMap(String json, Class<K1> classOfK1,
            Class<V1> classOfV1) throws IOException {
        if (json == null || json.isEmpty()) return null;
        JavaType eleType = SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, classOfK1, classOfV1);
        JavaType javaType = SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleType);
        return SIMPLE_MAPPER.readValue(json, javaType);
    }

    public static String formatJsonStr(String jsonStr, String indentStr) {
        if (jsonStr == null) return null;
        jsonStr = jsonStr.trim();
        char c = jsonStr.charAt(0);
        if (c != '{' && c != '[') return jsonStr;


        String lineBr = "\r\n";
        int nodeDeep = 0;
        int start = 0, end, temp;
        char lastDeclare = ' ', declare;
        String nodeValue, indent;
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
                builder.append(nodeValue.replaceAll(",", "," + lineBr + indent));
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
            }
            jsonStr = jsonStr.substring(start + 1);
            lastDeclare = declare;
        }
        return builder.toString();
    }

    private static int findNextDeclare(String jsonStr) {
        int minIndex = jsonStr.length(), index;
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

}
