package cn.jerry.es;

public enum EsEnum {
    ;

    public enum QueryOccur {
        must,
        //should,
        must_not
    }

    public enum SortOccur {
        asc,
        desc
    }

    public enum AggFunction {
        // 计数（不去重）
        value_count,
        // 计数（去重）
        cardinality,
        min,
        max,
        sum,
        avg,
        stats
    }

    public enum AggCalendarInterval {
        month,
        week,
        day,
        hour,
        minute
    }

    public enum Operation {
        _count,
        _search
    }

    public enum Code {
        SUCCESS,
        FAIL,
        ERROR
    }
}
