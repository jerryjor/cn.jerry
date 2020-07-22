package cn.jerry.es;

import cn.jerry.ex.ThrowableUtil;
import cn.jerry.json.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsResultReader {
    private static final String KEY_STATS = "count";
    private static final String KEY_BUCKETS = "buckets";
    private static final String HTTP_400 = "400 Bad Request";
    private static final String HTTP_404 = "404 Not Found";
    private String resultJson;
    private EsEnum.Code code;
    private String message;
    private Long total = 0L;
    private final List<Map<String, Object>> queryResult = new ArrayList<>();
    private final List<Map<String, Object>> aggResult = new ArrayList<>();

    public EsResultReader(String resultJson) {
        this.resultJson = resultJson;
        readJson();
    }

    public EsResultReader(EsEnum.Code code, String message) {
        this.code = code;
        this.message = message;
    }

    public EsResultReader(Exception e) {
        this.code = EsEnum.Code.ERROR;
        this.total = 0L;
        if (e != null) {
            Throwable t = ThrowableUtil.findRootCause(e);
            if (t.getMessage().contains(HTTP_404)) {
                this.message = "index partition not exists.";
            } else if (t.getMessage().contains(HTTP_400)) {
                    this.message = "Failed to read query json.";
            } else {
                this.message = e.getMessage();
            }
        }
    }

    public String getResultJson() {
        return resultJson;
    }

    public EsEnum.Code getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Long getTotal() {
        return total;
    }

    public List<Map<String, Object>> getQueryResult() {
        return queryResult;
    }

    public List<Map<String, Object>> getAggResult() {
        return aggResult;
    }

    private void readJson() {
        EsResult esResult;
        try {
            esResult = JsonUtil.toObject(resultJson, EsResult.class);
        } catch (Exception e) {
            this.code = EsEnum.Code.ERROR;
            this.message = ThrowableUtil.findRootCause(e).getMessage();
            return;
        }
        if (esResult == null) {
            this.code = EsEnum.Code.FAIL;
            this.message = "No response fro ES.";
        } else {
            this.code = EsEnum.Code.SUCCESS;
            readCountResult(esResult);
            readQueryResult(esResult.hits);
            readAggResult(esResult.aggregations);
        }
    }

    private void readCountResult(EsResult esResult) {
        if (esResult.count != null) {
            this.total = esResult.count;
            this.message = "success";
        }
    }

    private void readQueryResult(EsHits hits) {
        if (hits == null) {
            return;
        }
        this.message = "success";

        if (hits.total != null && hits.total.value != null) {
            this.total = hits.total.value;
        }

        if (hits.hits == null || hits.hits.isEmpty()) {
            return;
        }
        hits.hits.forEach(hit -> this.queryResult.add(hit.source));
    }

    private void readAggResult(Map<String, Map<String, Object>> aggregations) {
        if (aggregations == null || aggregations.isEmpty()) {
            return;
        }
        Map<String, Object> fieldAgg = new HashMap<>();
        List<Bucket> buckets = new ArrayList<>();
        aggregations.forEach((k, v) -> {
            if (v.containsKey(KEY_STATS)) {
                readStatsResult(k, v, fieldAgg);
            } else if (v.containsKey(KEY_BUCKETS)) {
                Bucket bucket = new Bucket();
                bucket.name = k;
                //noinspection unchecked
                bucket.subBuckets = (List<Map<String, Object>>)v.get(KEY_BUCKETS);
                readBucketsResult(bucket, buckets);
            } else {
                readValueResult(k, v, fieldAgg);
            }
        });
        if (!fieldAgg.isEmpty()) {
            this.aggResult.add(fieldAgg);
        }
        buckets.stream().filter(b -> !b.hasSub).forEach(b -> {
            b.values.forEach((k, v) -> {
                if (v.containsKey(KEY_STATS)) {
                    readStatsResult(k, v, b.keys);
                } else {
                    readValueResult(k, v, b.keys);
                }
            });
            this.aggResult.add(b.keys);
        });
    }

    private void readValueResult(String key, Map<String, Object> valueMap, Map<String, Object> result) {
        Number value = (Number) valueMap.get("value");
        result.put(key, value);
    }

    private void readStatsResult(String key, Map<String, Object> statsMap, Map<String, Object> result) {
        Number count = (Number) statsMap.get(KEY_STATS);
        result.put(key, count);
        Number min = (Number) statsMap.get("min");
        result.put(key + "_min", min);
        Number max = (Number) statsMap.get("max");
        result.put(key + "_max", max);
        Number avg = (Number) statsMap.get("avg");
        result.put(key + "_avg", avg);
        Number sum = (Number) statsMap.get("sum");
        result.put(key + "_sum", sum);
    }

    private void readBucketsResult(Bucket baseBucket, List<Bucket> buckets) {
        if (baseBucket == null || baseBucket.subBuckets == null || baseBucket.subBuckets.isEmpty()) {
            return;
        }
        List<Bucket> parents = new ArrayList<>();
        baseBucket.subBuckets.forEach(valueMap -> {
            Bucket bucket = new Bucket();
            Object key = valueMap.get("key");
            bucket.keys.put(baseBucket.name, key);
            Number count = (Number) valueMap.get("doc_count");
            bucket.keys.put(KEY_STATS, count);
            valueMap.forEach((k, v) -> {
                if (v instanceof Map) {
                    //noinspection unchecked
                    Map<String, Object> vMap = (Map<String, Object>) v;
                    if (vMap.containsKey(KEY_BUCKETS)) {
                        bucket.hasSub = true;
                        bucket.name = k;
                        //noinspection unchecked
                        bucket.subBuckets = (List<Map<String, Object>>)vMap.get(KEY_BUCKETS);
                    } else {
                        bucket.values.put(k, vMap);
                    }
                }
            });
            if (bucket.hasSub) {
                parents.add(bucket);
                readSubBuckets(parents, buckets);
            } else {
                buckets.add(bucket);
            }
        });
    }

    private void readSubBuckets(List<Bucket> parents, List<Bucket> buckets) {
        for (int i = 0; i< parents.size(); i++) {
            Bucket parent = parents.get(i);
            parent.subBuckets.forEach(pv -> {
                Bucket bucket = new Bucket();
                bucket.keys.putAll(parent.keys);
                Object key = pv.get("key");
                bucket.keys.put(parent.name, key);
                Number count = (Number) pv.get("doc_count");
                bucket.keys.put(KEY_STATS, count);
                pv.forEach((k, v) -> {
                    if (v instanceof Map) {
                        //noinspection unchecked
                        Map<String, Object> vMap = (Map<String, Object>) v;
                        if (vMap.containsKey(KEY_BUCKETS)) {
                            bucket.hasSub = true;
                            bucket.name = k;
                            //noinspection unchecked
                            bucket.subBuckets = (List<Map<String, Object>>)vMap.get(KEY_BUCKETS);
                            parents.add(bucket);
                        } else {
                            bucket.values.put(k, vMap);
                        }
                    }
                });
                if (!bucket.hasSub) {
                    buckets.add(bucket);
                }
            });
        }
    }

    private static class Bucket {
        private final Map<String, Object> keys = new HashMap<>();
        private final Map<String, Map<String, Object>> values = new HashMap<>();
        private boolean hasSub = false;
        private String name;
        private List<Map<String, Object>> subBuckets;
    }

    public static class EsResult {
        private Long count;
        private EsHits hits;
        private Map<String, Map<String, Object>> aggregations;
        private EsError error;

        @JsonProperty(value = "count")
        public void setCount(Long count) {
            this.count = count;
        }

        @JsonProperty(value = "hits")
        public void setHits(EsHits hits) {
            this.hits = hits;
        }

        @JsonProperty(value = "aggregations")
        public void setAggregations(Map<String, Map<String, Object>> aggregations) {
            this.aggregations = aggregations;
        }

        @JsonProperty(value = "error")
        public void setError(EsError error) {
            this.error = error;
        }
    }

    public static class EsHits {
        private EsTotal total;
        private List<EsSource> hits;

        @JsonProperty(value = "total")
        public void setTotal(EsTotal total) {
            this.total = total;
        }

        @JsonProperty(value = "hits")
        public void setHits(List<EsSource> hits) {
            this.hits = hits;
        }
    }

    public static class EsTotal {
        private Long value;

        @JsonProperty(value = "value")
        public void setValue(Long value) {
            this.value = value;
        }
    }

    public static class EsSource {
        private Map<String, Object> source;

        @JsonProperty(value = "_source")
        public void setSource(Map<String, Object> source) {
            this.source = source;
        }
    }

    public static class EsError {
        private String type;
        private String reason;
        private Integer line;
        private Integer col;

        @JsonProperty(value = "type")
        public void setType(String type) {
            this.type = type;
        }

        @JsonProperty(value = "reason")
        public void setReason(String reason) {
            this.reason = reason;
        }

        @JsonProperty(value = "line")
        public void setLine(Integer line) {
            this.line = line;
        }

        @JsonProperty(value = "col")
        public void setCol(Integer col) {
            this.col = col;
        }

        @Override
        public String toString() {
            return "{\"type\":\"" + type + "\", \"reason\":" + reason + "\", \"line\":" + line + ", \"col\":" + col + '}';
        }
    }
}
