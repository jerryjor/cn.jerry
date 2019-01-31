package cn.jerry.fabric.api.fabric.convert;

import cn.jerry.fabric.api.fabric.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.util.List;

public class StringToArrayConverter implements Converter<String, String[]> {
    private static Logger logger = LogManager.getLogger();

    @Override
    public String[] convert(String s) {
        if ((s = s.trim()).isEmpty()) return null;

        String[] array;
        if (isStringArrayInJson(s)) {
            // json
            try {
                List<Object> list = JsonUtil.toList(s, Object.class);
                if (list == null) return null;
                array = new String[list.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = list.get(i).toString();
                }
            } catch (IOException re) {
                logger.error("param {} cannot trans to String[]", s, re);
                return new String[]{s};
            }
        } else {
            // not json
            array = s.split("[ \t]*,[ \t]*");
        }
        return array;
    }

    private boolean isStringArrayInJson(String s) {
        if (s.charAt(0) != '[' || s.charAt(s.length() - 1) != ']') return false;
        int index = s.indexOf(",");
        while (index != -1) {
            if (s.charAt(index - 1) != '"') return false;
            index = s.indexOf(",", index + 1);
        }
        return true;
    }

}
