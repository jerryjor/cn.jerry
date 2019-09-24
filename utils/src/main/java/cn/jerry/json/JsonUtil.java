package cn.jerry.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * @param json    json字符串
     * @param typeOfT 对象
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json, JavaType typeOfT) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, typeOfT);
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
    public static <K, V> HashMap<K, V> toHashMap(String json, Class<K> classOfK, Class<V> classOfV) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructHashMapType(classOfK, classOfV));
    }

    /**
     * json转Map，抛异常
     *
     * @param json    json字符串
     * @param typeOfK 键类
     * @param typeOfV 值类
     * @return
     * @throws IOException
     */
    public static <K, V> HashMap<K, V> toHashMap(String json, JavaType typeOfK, JavaType typeOfV) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructHashMapType(typeOfK, typeOfV));
    }

    /**
     * json转List，抛异常
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @return
     * @throws IOException
     */
    public static <E> ArrayList<E> toArrayList(String json, Class<E> classOfE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructArrayListType(classOfE));
    }

    /**
     * json转List，抛异常
     *
     * @param json    json字符串
     * @param typeOfE 元素类
     * @return
     * @throws IOException
     */
    public static <E> ArrayList<E> toArrayList(String json, JavaType typeOfE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructArrayListType(typeOfE));
    }

    /**
     * json转HashSet，抛异常
     *
     * @param json     json字符串
     * @param classOfE 元素类
     * @return
     * @throws IOException
     */
    public static <E> HashSet<E> toHashSet(String json, Class<E> classOfE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructHashSetType(classOfE));
    }

    /**
     * json转HashSet，抛异常
     *
     * @param json    json字符串
     * @param typeOfE 元素类
     * @return
     * @throws IOException
     */
    public static <E> HashSet<E> toHashSet(String json, JavaType typeOfE) throws IOException {
        if (json == null || json.isEmpty()) return null;

        return SIMPLE_MAPPER.readValue(json, constructHashSetType(typeOfE));
    }

    /**
     * 构建无内部范型的JavaType
     *
     * @param clazz 类
     * @return
     * @throws IOException
     */
    public static JavaType constructSimpleType(Class<?> clazz) {
        return SIMPLE_MAPPER.getTypeFactory().constructType(clazz);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleClass element的类
     * @return
     * @throws IOException
     */
    public static JavaType constructArrayListType(Class<?> eleClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleClass);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleType element的类
     * @return
     * @throws IOException
     */
    public static JavaType constructArrayListType(JavaType eleType) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, eleType);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleClass element的类
     * @return
     * @throws IOException
     */
    public static JavaType constructHashSetType(Class<?> eleClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleClass);
    }

    /**
     * 构建ArrayList的JavaType，用于自定义范型
     *
     * @param eleType element的类
     * @return
     * @throws IOException
     */
    public static JavaType constructHashSetType(JavaType eleType) {
        return SIMPLE_MAPPER.getTypeFactory().constructCollectionType(HashSet.class, eleType);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     *
     * @param keyClass   key的类
     * @param valueClass value泛型
     * @return
     * @throws IOException
     */
    public static JavaType constructHashMapType(Class<?> keyClass, Class<?> valueClass) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
    }

    /**
     * 构建HashMap的JavaType，用于自定义范型
     *
     * @param keyType   key的类
     * @param valueType value泛型
     * @return
     * @throws IOException
     */
    public static JavaType constructHashMapType(JavaType keyType, JavaType valueType) {
        return SIMPLE_MAPPER.getTypeFactory().constructMapType(HashMap.class, keyType, valueType);
    }

    /**
     * 构建JavaType，用于自定义范型
     *
     * @param objClass       Object真实的类
     * @param genericClasses Object内部泛型
     * @return
     * @throws IOException
     */
    public static JavaType constructParametricType(Class<?> objClass, Class<?>... genericClasses) {
        return SIMPLE_MAPPER.getTypeFactory().constructParametricType(objClass, genericClasses);
    }

    /**
     * 构建JavaType，用于自定义范型
     *
     * @param objClass     Object真实的类
     * @param genericTypes Object内部泛型
     * @return
     * @throws IOException
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
                || (json.startsWith("[") && json.endsWith("]")));
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
