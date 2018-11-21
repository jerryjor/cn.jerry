package cn.jerry.lang;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayUtil {

    /**
     * 集合转数组
     * 
     * @param list
     * @param cls
     * @return
     */
    public static <T> T[] listToArray(List<T> list, Class<T> cls) {
        if (list == null || list.isEmpty()) return null;

        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(cls, list.size());

        return list.toArray(array);
    }

    /**
     * 打印数组
     * 
     * @param array
     * @return
     */
    public static <T> String toString(T[] array) {
        StringBuilder str = new StringBuilder("[");
        if (array != null && array.length > 0) {
            for (T t : array) {
                str.append(t.toString()).append(",");
            }
            str.setLength(str.length() - 1);
        }
        str.append("]");
        return str.toString();
    }
}
