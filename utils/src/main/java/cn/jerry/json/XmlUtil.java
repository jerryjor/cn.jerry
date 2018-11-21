package cn.jerry.json;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class XmlUtil {

    /**
     * 把map组装成xml
     *
     * @param data map数据
     * @param rootNode 根节点名称
     * @param ignoreEmptyNode 是否忽略空值属性
     * @return 组装好的xml
     */
    public static String buildXml(Map<String, Object> data, String rootNode, boolean ignoreEmptyNode) {
        StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<").append(rootNode).append(">");
        //
        List<XmlNode> nodes = new ArrayList<>();
        builder.append(buildValueFromMap(null, data, nodes, ignoreEmptyNode));
        //
        builder.append("</").append(rootNode).append(">");
        String result = builder.toString();
        XmlNode node;
        String nodeXml;
        while (!nodes.isEmpty()) {
            node = nodes.get(0);
            nodes.remove(0);
            nodeXml = buildValue(node, nodes, ignoreEmptyNode);
            result = result.replace(node.name, nodeXml);
        }
        return result;
    }

    private static String buildValue(XmlNode node, List<XmlNode> nodes, boolean ignoreEmptyNode) {
        if (node.value == null) {
            return "";
        }
        // 数字和字符串，直接返回
        else if (node.value instanceof Number || node.value instanceof String) {
            return node.value.toString();
        }
        // enum，返回name
        else if (node.value instanceof Enum) {
            return ((Enum) node.value).name();
        }
        // map对象，返回预组装节点字符串
        else if (node.value instanceof Map) {
            return buildValueFromMap(node.name, (Map<?, ?>) node.value, nodes, ignoreEmptyNode);
        }
        // 集合
        else if (node.value instanceof Collection) {
            Collection<?> valueArr = (Collection<?>) node.value;
            return buildValueFromCollection(node.name, valueArr, nodes);
        }
        // 其他，可能是个对象
        else {
            return buildValueFromObj(node.name, node.value, nodes, ignoreEmptyNode);
        }
    }

    private static String buildValueFromCollection(String parentNode, Collection<?> values, List<XmlNode> nodes) {
        List<XmlNode> subNodes = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        int index = 0;
        String nodeName;
        for (Object obj : values) {
            if (obj == null) continue;
            nodeName = genNodeName(parentNode, "" + index);
            builder.append(nodeName);
            nodes.add(new XmlNode(nodeName, obj));
            index++;
        }
        return builder.toString();
    }

    private static String buildValueFromMap(String parentNode, Map<?, ?> value, List<XmlNode> nodes, boolean ignoreEmptyNode) {
        List<XmlNode> subNodes = new ArrayList<>();
        try {
            StringBuilder builder = new StringBuilder();
            String nodeName;
            for (Map.Entry<?, ?> entry : value.entrySet()) {
                if (ignoreEmptyNode && entry.getValue() == null) continue;
                nodeName = genNodeName(parentNode, entry.getKey() == null ? "null" : entry.getKey().toString());
                builder.append("<").append(entry.getKey()).append(">");
                builder.append(nodeName);
                builder.append("</").append(entry.getKey()).append(">");
                subNodes.add(new XmlNode(nodeName, entry.getValue()));
            }
            return builder.toString();
        } finally {
            if (!subNodes.isEmpty()) {
                nodes.addAll(subNodes);
            }
        }
    }

    private static String buildValueFromObj(String parentNode, Object value, List<XmlNode> nodes, boolean ignoreEmptyNode) {
        List<XmlNode> subNodes = new ArrayList<>();
        try {
            StringBuilder builder = new StringBuilder();
            String nodeName;
            Class<?> clazz = value.getClass();
            Object propertyValue;
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);//修改访问权限
                propertyValue = field.get(value);
                if (ignoreEmptyNode && propertyValue == null) continue;
                nodeName = genNodeName(parentNode, field.getName());
                builder.append("<").append(field.getName()).append(">");
                builder.append(nodeName);
                builder.append("</").append(field.getName()).append(">");
                subNodes.add(new XmlNode(nodeName, propertyValue));
            }
            return builder.toString();
        } catch (Exception e) {
            //
            return null;
        } finally {
            if (!subNodes.isEmpty()) {
                nodes.addAll(subNodes);
            }
        }
    }

    private static String genNodeName(String parentNode, String propName) {
        if (parentNode == null) {
            return "${" + propName + "}";
        } else {
            return parentNode.replace("}", "." + propName + "}");
        }
    }

    public static String formatXmlStr(String xmlStr, String indentStr) {
        String lineBr = "\r\n";
        int nodeDeep = 0;
        int start = 0, end, temp;
        String nodeDeclare, nodeValue;
        StringBuilder builder = new StringBuilder();
        temp = xmlStr.indexOf("?>");
        if (temp != -1) {
            end = temp + "?>".length();
            builder.append(xmlStr, start, end);
            xmlStr = xmlStr.substring(end);
        }
        while (xmlStr.length() > 0) {
            start = xmlStr.indexOf("<");
            if (start != -1) {
                nodeValue = xmlStr.substring(0, start).trim();
                builder.append(nodeValue);
            } else {
                // 找不到<了，结束
                break;
            }
            temp = xmlStr.indexOf(">", start + 1);
            end = temp + ">".length();
            nodeDeclare = xmlStr.substring(start, end);
            // 是个结束标识
            if (nodeDeclare.indexOf("/") == 1) {
                nodeDeep--;
                // 如果前面的字符是节点，换行，加缩进
                if (builder.charAt(builder.length() - 1) == '>') {
                    builder.append(lineBr).append(genIndent(indentStr, nodeDeep));
                }
            }
            // 是个自结束节点，换行，加缩进
            else if (nodeDeclare.indexOf("/") == nodeDeclare.length() - 2) {
                builder.append(lineBr).append(genIndent(indentStr, nodeDeep));
            }
            // 是个开始节点，换行，加缩进，标记下一次节点深度+1
            else {
                builder.append(lineBr).append(genIndent(indentStr, nodeDeep));
                nodeDeep++;
            }
            builder.append(nodeDeclare);
            xmlStr = xmlStr.substring(end);
        }
        return builder.toString();
    }

    private static String genIndent(String indentStr, int nodeDeep) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodeDeep; i++) {
            builder.append(indentStr);
        }
        return builder.toString();
    }

    static class XmlNode {

        private String name;
        private Object value;

        XmlNode(String name, Object value) {
            this.name = name;
            this.value = value;
        }

    }

}
