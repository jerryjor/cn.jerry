package cn.jerry.test.file;

import org.apache.commons.lang.StringUtils;

public class LogContentFilter implements IContentFilter {
    private static final String[] KEY_WORDS = new String[] {
            "OrderPaidLimitAmountProcessHandler",
            "PaymentLimitAmountBoImpl",
            "PaymentLimitAmountDAO",
            "BaseDao",
            "PhoneRechargePaiDanFailHandler",
            "PhoneRechargeBean",
            "TopUpSuAdminEjbImpl",
            "javax.persistence.RollbackException",
            "javax.jms.TransactionRolledBackException",
            "javax.jms.JMSException",
            "Caused by",
            "\tat ",
            "\t... ",
            "org.apache.activemq.transport.failover.FailoverTransport",
            "org.apache.activemq.TransactionContext",
            "org.apache.activemq.ActiveMQMessageConsumer"
    };

    public boolean canBeIgnored(String content) {
        if (StringUtils.isBlank(content)) {
            return true;
        } else {
            for (String keyWord : KEY_WORDS) {
                if (content.contains(keyWord)) {
                    return true;
                }
            }
            return false;
        }
    }
}
