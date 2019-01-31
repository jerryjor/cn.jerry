package cn.jerry.fabric.api.fabric.convert;

import cn.jerry.fabric.api.fabric.model.SampleChaincode;
import cn.jerry.fabric.api.fabric.util.JsonUtil;
import cn.jerry.fabric.api.fabric.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

public class StringToChaincodeConverter implements Converter<String, SampleChaincode> {
    private static Logger logger = LogManager.getLogger();

    @Override
    public SampleChaincode convert(String s) {
        if (StringUtils.isBlank(s))
            return null;

        try {
            return JsonUtil.toObject(s, SampleChaincode.class);
        } catch (IOException re) {
            logger.error("param {} cannot recognized by TransactionRequest.Type", s);
            return null;
        }
    }
}
