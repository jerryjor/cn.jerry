package cn.jerry.es;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ule.merchant.tools.json.JsonUtil;
import com.ule.merchant.tools.lang.DateUtil;
import com.ule.merchant.tools.lang.StringUtils;

import java.io.IOException;
import java.util.*;

public class EsQueryBuilder {
    private static final String PRE_PARAM = "?";
    private static final String DATE_FORMAT = "yyyyMMddHHmmssZ";
    private static final String MSG_MUST_NOT_BLANK = "%s can not be blank.";
    private static final String MSG_MUST_NOT_EMPTY = "%s can not be empty.";
    private static final String MSG_MUST_NOT_NEGATIVE = "%s can not negative..";
    private static final String MSG_MUST_BE_ASSIGNED = "%s must be assigned.";

    private Query query;
    private Integer from;
    private Integer size;
    private List<String> source;
    private List<HashMap<String, String>> sort;
    private LinkedHashMap<String, IAggregation> aggs;

    private final List<String> queryParams = new ArrayList<>();
    private final List<String> sourceParams = new ArrayList<>();
    private final List<String> sortParams = new ArrayList<>();
    private final List<String> aggsParams = new ArrayList<>();

    public static EsQueryBuilder newBuilder() {
        EsQueryBuilder builder = new EsQueryBuilder();
        builder.query = new Query();
        return builder;
    }

    public EsQueryBuilder addQuery(String field, String value, EsEnum.QueryOccur occur) {
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "value"));
        }
        if (occur == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "occur"));
        }
        ArrayList<IQuery> list = this.query.bool.computeIfAbsent(occur.name(), a -> new ArrayList<>());
        list.add(new MatchQuery(field, value, this.queryParams));
        return this;
    }

    public EsQueryBuilder addQuery(String field, String[] values, EsEnum.QueryOccur occur) {
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_EMPTY, "values"));
        }
        if (occur == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "occur"));
        }
        ArrayList<IQuery> list = this.query.bool.computeIfAbsent(occur.name(), a -> new ArrayList<>());
        list.add(new TermQuery(field, values, this.queryParams));
        return this;
    }

    public EsQueryBuilder addQuery(String field, Date startTime, Date endTime, EsEnum.QueryOccur occur) {
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (startTime == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "startTime"));
        }
        if (endTime == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "endTime"));
        }
        if (startTime.after(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime.");
        }
        if (occur == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "occur"));
        }
        ArrayList<IQuery> list = this.query.bool.computeIfAbsent(occur.name(), a -> new ArrayList<>());
        list.add(new RangeQuery(field, startTime, endTime, this.queryParams));
        return this;
    }

    public EsQueryBuilder addQuery(String field, Number minValue, Number maxValue, EsEnum.QueryOccur occur) {
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (minValue == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "minValue"));
        }
        if (maxValue == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "maxValue"));
        }
        if (minValue.doubleValue() > maxValue.doubleValue()) {
            throw new IllegalArgumentException("minValue must be smaller than maxValue.");
        }
        if (occur == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "occur"));
        }
        ArrayList<IQuery> list = this.query.bool.computeIfAbsent(occur.name(), a -> new ArrayList<>());
        list.add(new RangeQuery(field, minValue, maxValue));
        return this;
    }

    public EsQueryBuilder addAggregation(AggOperation operation) {
        if (this.aggs == null) {
            this.aggs = new LinkedHashMap<>();
        }
        this.aggs.putAll(operation.genAggs(this.aggsParams));
        return this;
    }

    public EsQueryBuilder addAggregation(String[] fields, AggOperation[] subAggs) {
        return addAggregation(fields, subAggs, null, null);
    }

    public EsQueryBuilder addAggregation(String[] fields, AggOperation[] subAggs, Integer size, Sort[] sortArr) {
        if (this.aggs == null) {
            this.aggs = new LinkedHashMap<>();
        }
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_EMPTY, "fields"));
        }
        AggTerms childTerms = null;
        List<String> params = new ArrayList<>();
        for (int i = fields.length - 1; i >= 0; i--) {
            AggTerms terms;
            List<String> subParams = new ArrayList<>();
            if (i == fields.length - 1) {
                terms = new AggTerms(fields[i], subAggs, size, sortArr, subParams);
                params.addAll(subParams);
            } else {
                terms = new AggTerms(fields[i], childTerms, subParams);
                params.addAll(0, subParams);
            }
            childTerms = terms;
        }
        this.aggs.putAll(childTerms);
        this.aggsParams.addAll(params);
        return this;
    }

    public EsQueryBuilder addAggregation(String field, EsEnum.AggCalendarInterval interval, AggOperation[] subAggs) {
        return addAggregation(field, interval, null, subAggs);
    }

    public EsQueryBuilder addAggregation(String field, EsEnum.AggCalendarInterval interval, Integer timezone, AggOperation[] subAggs) {
        if (this.aggs == null) {
            this.aggs = new LinkedHashMap<>();
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (interval == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "interval"));
        }
        String name = field + "_" + interval.name();
        AggHistogram ah = new AggHistogram(field, interval, timezone, subAggs, this.aggsParams);
        this.aggs.put(name, ah);
        return this;
    }

    public EsQueryBuilder addAggregation(String field, DateRange[] ranges, AggOperation[] subAggs) {
        return addAggregation(field, ranges, null, null, subAggs);
    }

    public EsQueryBuilder addAggregation(String field, DateRange[] ranges, Date missing, Integer timezone, AggOperation[] subAggs) {
        if (this.aggs == null) {
            this.aggs = new LinkedHashMap<>();
        }
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (ranges == null || ranges.length == 0) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "ranges"));
        }
        String name = field + "_range";
        AggDateRanges ar = new AggDateRanges(field, ranges, missing, timezone, subAggs, this.aggsParams);
        this.aggs.put(name, ar);
        return this;
    }

    public EsQueryBuilder setFrom(int from) {
        if (from < 0) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_NEGATIVE, "from"));
        }
        this.from = from;
        return this;
    }

    public EsQueryBuilder setSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_NEGATIVE, "size"));
        }
        this.size = size;
        return this;
    }

    public EsQueryBuilder addSource(String field) {
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "source"));
        }
        if (this.source == null) {
            this.source = new ArrayList<>();
        }
        this.source.add(PRE_PARAM);
        addParam(this.sourceParams, field);
        return this;
    }

    public EsQueryBuilder addSort(String field, EsEnum.SortOccur occur) {
        if (StringUtils.isBlank(field)) {
            throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
        }
        if (occur == null) {
            throw new IllegalArgumentException(String.format(MSG_MUST_BE_ASSIGNED, "occur"));
        }
        if (this.sort == null) {
            this.sort = new ArrayList<>();
        }
        HashMap<String, String> value = new HashMap<>();
        value.put(field, PRE_PARAM);
        this.sort.add(value);
        addParam(this.sortParams, occur.name());
        return this;
    }

    public BuildResult build() throws IOException {
        String queryPattern = JsonUtil.toJsonNonNull(this);
        List<String> params = new ArrayList<>();
        params.addAll(this.queryParams);
        params.addAll(this.sourceParams);
        params.addAll(this.sortParams);
        params.addAll(this.aggsParams);
        return new BuildResult(queryPattern, params);
    }

    @JsonProperty(value = "query", index = 0)
    public Query getQuery() {
        return query;
    }

    @JsonProperty(value = "from", index = 1)
    public Integer getFrom() {
        return from;
    }

    @JsonProperty(value = "size", index = 2)
    public Integer getSize() {
        return size;
    }

    @JsonProperty(value = "_source", index = 3)
    public List<String> getSource() {
        return source;
    }

    @JsonProperty(value = "sort", index = 4)
    public List<HashMap<String, String>> getSort() {
        return sort;
    }

    @JsonProperty(value = "aggs", index = 5)
    public Map<String, IAggregation> getAggs() {
        return aggs;
    }

    private static void addParam(List<String> params, String value) {
        params.add(value.replace("\"", "\\\\\""));
    }

    private static String timeZoneToString(Integer timezone) {
        return (timezone < 0 ? "-" : "+") + (timezone > -10 && timezone < 10 ? "0" : "")
                + (timezone > 0 ? timezone : -timezone) + ":00";
    }

    public static class Query {
        private final LinkedHashMap<String, ArrayList<IQuery>> bool;

        private Query() {
            this.bool = new LinkedHashMap<>();
        }

        @JsonProperty(value = "bool", index = 0)
        public Map<String, ArrayList<IQuery>> getBool() {
            return bool;
        }
    }

    public interface IQuery {
    }

    public static class TermQuery implements IQuery {
        private final HashMap<String, String[]> terms;

        private TermQuery(String field, String[] values, List<String> params) {
            this.terms = new HashMap<>();
            String[] in = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                in[i] = PRE_PARAM;
                addParam(params, values[i]);
            }
            this.terms.put(field, in);
        }

        @JsonProperty(value = "terms", index = 0)
        public Map<String, String[]> getTerms() {
            return terms;
        }
    }

    public static class MatchQuery implements IQuery {
        private final HashMap<String, String> match;

        private MatchQuery(String field, String value, List<String> params) {
            this.match = new HashMap<>();
            this.match.put(field, PRE_PARAM);
            addParam(params, value);
        }

        @JsonProperty(value = "match", index = 0)
        public Map<String, String> getMatch() {
            return match;
        }
    }

    public static class RangeQuery implements IQuery {
        private final HashMap<String, QueryRange> range;

        private RangeQuery(String field, Date gt, Date lt, List<String> params) {
            this.range = new HashMap<>();
            this.range.put(field, new DateRange(gt, lt, params));
        }

        private RangeQuery(String field, Number gt, Number lt) {
            this.range = new HashMap<>();
            this.range.put(field, new NumberRange(gt, lt));
        }

        @JsonProperty(value = "range", index = 0)
        public Map<String, QueryRange> getRange() {
            return range;
        }
    }

    public interface QueryRange {
    }

    public static class NumberRange implements QueryRange {
        private final Number gt;
        private final Number lt;

        private NumberRange(Number gt, Number lt) {
            this.gt = gt;
            this.lt = lt;
        }

        @JsonProperty(value = "gt", index = 0)
        public Number getGt() {
            return gt;
        }

        @JsonProperty(value = "lt", index = 1)
        public Number getLt() {
            return lt;
        }
    }

    public static class DateRange implements QueryRange {
        private final String gt;
        private final String lt;
        private final String format;
        private final String name;

        private DateRange(Date gt, Date lt, List<String> params) {
            this(gt, lt, null, params);
        }

        private DateRange(Date gt, Date lt, String name, List<String> params) {
            this.gt = PRE_PARAM;
            addParam(params, DateUtil.format(gt, DATE_FORMAT));
            this.lt = PRE_PARAM;
            addParam(params, DateUtil.format(lt, DATE_FORMAT));
            this.format = PRE_PARAM;
            addParam(params, DATE_FORMAT);
            if (StringUtils.isNotBlank(name)) {
                this.name = PRE_PARAM;
                addParam(params, name);
            } else {
                this.name = null;
            }
        }

        public DateRange(Date gt, Date lt, String name) {
            this.gt = DateUtil.format(gt, DATE_FORMAT);
            this.lt = DateUtil.format(lt, DATE_FORMAT);
            this.format = null;
            this.name = name;
        }

        @JsonProperty(value = "gt", index = 0)
        public String getGt() {
            return gt;
        }

        @JsonProperty(value = "lt", index = 1)
        public String getLt() {
            return lt;
        }

        @JsonProperty(value = "format", index = 2)
        public String getFormat() {
            return format;
        }
    }

    public static class Sort {
        private final String field;
        private final EsEnum.SortOccur occur;

        public Sort(String field, EsEnum.SortOccur occur) {
            this.field = field;
            this.occur = occur;
        }
    }

    public static class AggOperation {
        private final String name;
        private final String field;
        private final EsEnum.AggFunction function;
        private final Number missing;

        public AggOperation(String name, String field) {
            this(name, field, EsEnum.AggFunction.stats, null);
        }

        public AggOperation(String name, String field, EsEnum.AggFunction function) {
            this(name, field, function, null);
        }

        public AggOperation(String name, String field, EsEnum.AggFunction function, Number missing) {
            this.field = field;
            this.function = function == null ? EsEnum.AggFunction.stats : function;
            if (StringUtils.isBlank(name)) {
                this.name = this.field + "_" + this.function.name();
            } else {
                this.name = name;
            }
            this.missing = missing;
        }

        private LinkedHashMap<String, IAggregation> genAggs(List<String> params) {
            if (StringUtils.isBlank(this.field)) {
                throw new IllegalArgumentException(String.format(MSG_MUST_NOT_BLANK, "field"));
            }
            LinkedHashMap<String, IAggregation> result = new LinkedHashMap<>();
            AggStats func = new AggStats(this.field, this.function, this.missing, params);
            result.put(name, func);
            return result;
        }
    }

    public static class AggField {
        private final String field;
        private final Integer size;
        private final Number missing;
        private final LinkedHashMap<String, String> order;

        private AggField(String field, Number missing, List<String> params) {
            this.field = PRE_PARAM;
            addParam(params, field);
            this.size = null;
            this.missing = missing;
            this.order = null;
        }

        public AggField(String field, Integer size, Sort[] sortArr, List<String> params) {
            this.field = PRE_PARAM;
            addParam(params, field);
            if (size != null && size > 0) {
                this.size = size;
            } else {
                this.size = null;
            }
            this.missing = null;
            if (sortArr != null && sortArr.length > 0) {
                this.order = new LinkedHashMap<>();
                for (Sort sort : sortArr) {
                    this.order.put(sort.field, PRE_PARAM);
                    addParam(params, sort.occur.name());
                }
            } else {
                this.order = null;
            }
        }

        @JsonProperty(value = "field", index = 0)
        public String getField() {
            return field;
        }

        @JsonProperty(value = "size", index = 1)
        public Integer getSize() {
            return size;
        }

        @JsonProperty(value = "missing", index = 2)
        public Number getMissing() {
            return missing;
        }

        @JsonProperty(value = "order", index = 3)
        public LinkedHashMap<String, String> getOrder() {
            return order;
        }
    }

    public static class AggDateInterval {
        private final String field;
        private final String calendarInterval;
        private final String timeZone;

        private AggDateInterval(String field, EsEnum.AggCalendarInterval interval, Integer timezone, List<String> params) {
            this.field = PRE_PARAM;
            addParam(params, field);
            this.calendarInterval = PRE_PARAM;
            addParam(params, interval.name());
            if (timezone != null) {
                this.timeZone = PRE_PARAM;
                addParam(params, timeZoneToString(timezone));
            } else {
                this.timeZone = null;
            }
        }

        @JsonProperty(value = "field", index = 0)
        public String getField() {
            return field;
        }

        @JsonProperty(value = "calendar_interval", index = 1)
        public String getCalendarInterval() {
            return calendarInterval;
        }

        @JsonProperty(value = "time_zone", index = 2)
        public String getTimeZone() {
            return timeZone;
        }
    }

    public static class AggDateRange {
        private String from;
        private String to;
        private String key;

        private AggDateRange(String from, String to, String key, final List<String> params) {
            if (StringUtils.isNotBlank(from)) {
                this.from = PRE_PARAM;
                addParam(params, from);
            }
            if (StringUtils.isNotBlank(to)) {
                this.to = PRE_PARAM;
                addParam(params, to);
            }
            if (StringUtils.isNotBlank(key)) {
                this.key = PRE_PARAM;
                addParam(params, key);
            }
        }

        @JsonProperty(value = "from", index = 0)
        public String getFrom() {
            return from;
        }

        @JsonProperty(value = "to", index = 1)
        public String getTo() {
            return to;
        }

        @JsonProperty(value = "key", index = 2)
        public String getKey() {
            return key;
        }
    }

    public static class AggDateRangeField {
        private final String field;
        private final String format;
        private final String missing;
        private final String timeZone;
        private final List<AggDateRange> ranges;
        private boolean keyed = false;

        private AggDateRangeField(String field, DateRange[] ranges, Date missing, Integer timezone, final List<String> params) {
            this.field = PRE_PARAM;
            addParam(params, field);
            this.format = PRE_PARAM;
            addParam(params, DATE_FORMAT);
            if (missing != null) {
                this.missing = PRE_PARAM;
                addParam(params, DateUtil.format(missing, DATE_FORMAT));
            } else {
                this.missing = null;
            }
            if (timezone != null) {
                this.timeZone = PRE_PARAM;
                addParam(params, timeZoneToString(timezone));
            } else {
                this.timeZone = null;
            }
            this.ranges = new ArrayList<>();
            for (DateRange range : ranges) {
                this.ranges.add(new AggDateRange(range.gt, range.lt, range.name, params));
                if (range.name != null) {
                    this.keyed = true;
                }
            }
        }

        @JsonProperty(value = "field", index = 0)
        public String getField() {
            return field;
        }

        @JsonProperty(value = "format", index = 1)
        public String getFormat() {
            return format;
        }

        @JsonProperty(value = "missing", index = 2)
        public String getMissing() {
            return missing;
        }

        @JsonProperty(value = "time_zone", index = 3)
        public String getTimeZone() {
            return timeZone;
        }

        @JsonProperty(value = "ranges", index = 4)
        public List<AggDateRange> getRanges() {
            return ranges;
        }

        @JsonProperty(value = "keyed", index = 5)
        public boolean isKeyed() {
            return keyed;
        }
    }

    public interface IAggregation {
    }

    public static class AggStats extends LinkedHashMap<String, AggField> implements IAggregation {

        private AggStats(String field, EsEnum.AggFunction function, Number missing, List<String> params) {
            AggField value = new AggField(field, missing, params);
            this.put(function.name(), value);
        }
    }

    public static class AggTerm implements IAggregation {
        private final AggField terms;
        private final LinkedHashMap<String, IAggregation> aggs;

        private AggTerm(String field, AggOperation[] operations, Integer size, Sort[] sortArr, final List<String> params) {
            this.terms = new AggField(field, size, sortArr, params);
            if (operations != null && operations.length > 0) {
                this.aggs = new LinkedHashMap<>();
                for (AggOperation o : operations) {
                    this.aggs.putAll(o.genAggs(params));
                }
            } else {
                this.aggs = null;
            }
        }

        private AggTerm(String field, AggTerms subTerms, final List<String> params) {
            this.terms = new AggField(field, null, params);
            this.aggs = new LinkedHashMap<>();
            this.aggs.putAll(subTerms);
        }

        @JsonProperty(value = "terms", index = 0)
        public AggField getTerms() {
            return terms;
        }

        @JsonProperty(value = "aggs", index = 1)
        public Map<String, IAggregation> getAggs() {
            return aggs;
        }
    }

    public static class AggTerms extends HashMap<String, AggTerm> {

        private AggTerms(String field, AggOperation[] subAggs, Integer size, Sort[] sortArr, List<String> params) {
            this.put(field, new AggTerm(field, subAggs, size, sortArr, params));
        }

        private AggTerms(String field, AggTerms childTerms, List<String> params) {
            this.put(field, new AggTerm(field, childTerms, params));
        }
    }

    public static class AggHistogram implements IAggregation {
        private final AggDateInterval dateHistogram;
        private final LinkedHashMap<String, IAggregation> aggs;

        private AggHistogram(String field, EsEnum.AggCalendarInterval interval, Integer timezone, AggOperation[] operations, List<String> params) {
            this.dateHistogram = new AggDateInterval(field, interval, timezone, params);
            if (operations != null && operations.length > 0) {
                this.aggs = new LinkedHashMap<>();
                for (AggOperation o : operations) {
                    this.aggs.putAll(o.genAggs(params));
                }
            } else {
                this.aggs = null;
            }
        }

        @JsonProperty(value = "date_histogram", index = 0)
        public AggDateInterval getDateHistogram() {
            return dateHistogram;
        }

        @JsonProperty(value = "aggs", index = 1)
        public Map<String, IAggregation> getAggs() {
            return aggs;
        }
    }

    public static class AggDateRanges implements IAggregation {
        private final AggDateRangeField dateRange;
        private final LinkedHashMap<String, IAggregation> aggs;

        private AggDateRanges(String field, DateRange[] ranges, Date missing, Integer timezone, AggOperation[] operations, final List<String> params) {
            this.dateRange = new AggDateRangeField(field, ranges, missing, timezone, params);
            if (operations != null && operations.length > 0) {
                this.aggs = new LinkedHashMap<>();
                for (AggOperation o : operations) {
                    this.aggs.putAll(o.genAggs(params));
                }
            } else {
                this.aggs = null;
            }
        }

        @JsonProperty(value = "date_range", index = 0)
        public AggDateRangeField getDateRange() {
            return dateRange;
        }

        @JsonProperty(value = "aggs", index = 1)
        public Map<String, IAggregation> getAggs() {
            return aggs;
        }
    }

    public static class BuildResult {
        private final String queryPattern;
        private final String[] queryParams;

        public BuildResult(String queryPattern, List<String> params) {
            this.queryPattern = queryPattern;
            this.queryParams = new String[params.size()];
            params.toArray(this.queryParams);
        }

        public String getQueryPattern() {
            return queryPattern;
        }

        public String[] getQueryParams() {
            return queryParams;
        }

        public String toString() {
            if (queryParams == null || queryParams.length == 0) {
                return queryPattern;
            }
            String key = "\\" + PRE_PARAM;
            String str = queryPattern;
            for (String param : queryParams) {
                str = str.replaceFirst(key, param);
            }
            return str;
        }
    }
}
