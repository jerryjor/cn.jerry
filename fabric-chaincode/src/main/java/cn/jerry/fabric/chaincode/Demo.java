package cn.jerry.fabric.chaincode;

import cn.jerry.fabric.chaincode.model.PagedData;
import cn.jerry.fabric.chaincode.util.GzipUtil;
import cn.jerry.fabric.chaincode.util.JsonUtil;
import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Demo extends ChaincodeBase {
    private static Log logger = LogFactory.getLog(Demo.class);
    private static final int PAGE_SIZE = 20;

    /**
     * 应当定义两类记录，一类存账户的transactionID列表，一类存transactionID对应的业务数据
     */
    @Override
    public Response init(ChaincodeStub stub) {
        try {
            logger.info("Init merchant contract chaincode[java]");
            String func = stub.getFunction();

            if (!func.equals("init")) {
                return newErrorResponse("function other than init is not supported");
            }

            // Initialize the chaincode
            List<String> params = stub.getParameters();
            if (params.size() % 2 == 1) {
                return newErrorResponse("Incorrect number of arguments. Arguments must in pairs. e.g. [\"account1\",\"value1\",\"account2\",\"value2\"]");
            }
            saveData(stub, params);

            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    private void saveData(ChaincodeStub stub, List<String> params) {
        String account, value;
        for (int i = 0, total = params.size() / 2; i < total; i++) {
            account = params.get(i * 2);
            value = params.get(i * 2 + 1);
            logger.info(String.format("account %s, value = %s", account, value));
            // 保存交易ID对应的业务数据
            stub.putState(stub.getTxId(), GzipUtil.gzip(value));
            // 更新交易ID索引
            CompositeKey pageKey = stub.createCompositeKey(account, "0");
            int pages = queryTotalPages(stub, pageKey);
            boolean newPage = false;
            if (pages == 0) {
                // 该账户目前没有交易历史，初始化第一页
                newPage = true;
            } else {
                // 取最后一页数据
                CompositeKey detailKey = stub.createCompositeKey(account, "" + pages);
                List<String> txIDs = queryPageIndex(stub, detailKey);
                if (txIDs.size() < PAGE_SIZE) {
                    // 分页数据不满，直接加入后更新
                    txIDs.add(stub.getTxId());
                    savePageIndex(stub, detailKey, txIDs);
                } else {
                    // 分页数据已满，创建新的分页
                    newPage = true;
                }
            }
            if (newPage) {
                pages++;
                List<String> txIDs = new ArrayList<>();
                txIDs.add(stub.getTxId());
                CompositeKey detailKey = stub.createCompositeKey(account, "" + pages);
                savePageIndex(stub, detailKey, txIDs);
                stub.putStringState(pageKey.toString(), "" + pages);
            }
        }
    }

    private int queryTotalPages(ChaincodeStub stub, CompositeKey pageKey) {
        // 查询总页数
        int pages = 0;
        QueryResultsIterator<KeyValue> keyValues = stub.getStateByPartialCompositeKey(pageKey);
        Iterator<KeyValue> it = keyValues.iterator();
        if (it.hasNext()) {
            try {
                pages = Integer.valueOf(it.next().getStringValue());
            } catch (RuntimeException e) {}
        }
        try {
            keyValues.close();
        } catch (Exception e) {}
        return pages;
    }

    private List<String> queryPageIndex(ChaincodeStub stub, CompositeKey pageKey) {
        List<String> txIDs = null;
        QueryResultsIterator<KeyValue> keyValues = stub.getStateByPartialCompositeKey(pageKey);
        Iterator<KeyValue> it = keyValues.iterator();
        if (it.hasNext()) {
            try {
                txIDs = JsonUtil.toList(GzipUtil.unzip(it.next().getValue()), String.class);
            } catch (IOException e) {}
        }
        try {
            keyValues.close();
        } catch (Exception e) {}
        return txIDs == null ? new ArrayList<>() : txIDs;
    }

    private void savePageIndex(ChaincodeStub stub, CompositeKey pageKey, List<String> txIDs) {
        try {
            stub.putState(pageKey.toString(), GzipUtil.gzip(JsonUtil.toJsonNonNull(txIDs)));
        } catch (IOException e) {}
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            // logger.info("Invoke merchant contract chaincode[java]");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("update")) {
                return update(stub, params);
            }
            if (func.equals("delete")) {
                return delete(stub, params);
            }
            if (func.equals("queryLatest")) {
                return queryLatest(stub, params);
            }
            if (func.equals("queryTx")) {
                return queryTx(stub, params);
            }
            if (func.equals("queryHisTx")) {
                return queryHisTx(stub, params);
            }
            return newErrorResponse("Invalid invoke function name."
                    + " Expecting one of: [\"update\", \"delete\", \"queryLatest\", \"queryTx\", \"queryHisTx\"]");
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    private Response update(ChaincodeStub stub, List<String> args) {
        if (args.size() % 2 == 1) {
            return newErrorResponse("Incorrect number of arguments. Arguments must in pairs. e.g. [\"account1\",\"value1\",\"account2\",\"value2\"]");
        }

        saveData(stub, args);

        Map<String, byte[]> transientMap = stub.getTransient();
        if (null != transientMap) {
            if (transientMap.containsKey("event") && transientMap.get("event") != null) {
                stub.setEvent("event", transientMap.get("event"));
            }
            if (transientMap.containsKey("result") && transientMap.get("result") != null) {
                return newSuccessResponse(transientMap.get("result"));
            }
        }
        return newSuccessResponse();
    }

    private Response delete(ChaincodeStub stub, List<String> accounts) {
        accounts.forEach(account -> {
            if (account != null && !account.isEmpty()) {
                // 查询总页数
                int pages = queryTotalPages(stub, stub.createCompositeKey(account, "0"));
                if (pages > 0) {
                    for (int i = 0; i < pages; i++) {
                        stub.delState(stub.createCompositeKey(account, "" + i).toString());
                    }
                }
            }
        });
        return newSuccessResponse();
    }

    private Response queryLatest(ChaincodeStub stub, List<String> accounts) {
        Map<String, String> results = new HashMap<>();
        accounts.forEach(account -> {
            if (account != null && !account.isEmpty()) {
                // 查询总页数
                int pages = queryTotalPages(stub, stub.createCompositeKey(account, "0"));
                if (pages > 0) {
                    // 取最后一页数据
                    List<String> txIDs = queryPageIndex(stub, stub.createCompositeKey(account, "" + pages));
                    if (!txIDs.isEmpty()) {
                        // 取最后一条交易ID，查询业务数据
                        results.put(account, GzipUtil.unzip(stub.getState(txIDs.get(txIDs.size() - 1))));
                    }
                }
            }
        });
        String value;
        try {
            value = JsonUtil.toJson(results);
        } catch (Exception e) {
            return newErrorResponse(String.format("Got %d latest, but to json failed.", results.size()));
        }
        // logger.info(String.format("Query Response:\nAccount: %s, Value: %s\n", account, value));
        return newSuccessResponse("succeed", ByteString.copyFrom(value, UTF_8).toByteArray());
    }

    private Response queryTx(ChaincodeStub stub, List<String> txIDs) {
        Map<String, String> results = new HashMap<>();
        txIDs.forEach(txID -> {
            if (txID != null && !txID.isEmpty()) {
                String value = GzipUtil.unzip(stub.getState(txID));
                results.put(txID, value);
            }
        });
        String value;
        try {
            value = JsonUtil.toJson(results);
        } catch (Exception e) {
            return newErrorResponse(String.format("Got %d transaction, but to json failed.", results.size()));
        }
        // logger.info(String.format("Query Response:\nAccount: %s, Value: %s\n", account, value));
        return newSuccessResponse("succeed", ByteString.copyFrom(value, UTF_8).toByteArray());
    }

    private Response queryHisTx(ChaincodeStub stub, List<String> args) {
        if (args.size() < 1) {
            return newErrorResponse("Incorrect number of arguments. Expecting account and page to query");
        }
        String account = args.get(0);
        Integer startPage;
        try {
            startPage = Integer.valueOf(args.get(1));
        } catch (RuntimeException re) {
            startPage = null;
        }
        if (startPage == null || startPage <= 0) startPage = 1;

        PagedData result = new PagedData();
        result.setPageSize(PAGE_SIZE);
        result.setCurrPage(startPage);
        // 查询总页数
        CompositeKey pageKey = stub.createCompositeKey(account, "0");
        int pages = queryTotalPages(stub, pageKey);
        result.setTotalPages(pages);
        if (pages < startPage) {
            // 超出分页，无数据
            result.setData(new ArrayList<>());
        } else {
            // 取该页数据
            CompositeKey detailKey = stub.createCompositeKey(account, "" + startPage);
            List<String> txIDs = queryPageIndex(stub, detailKey);
            result.setData(txIDs);
        }

        String value;
        try {
            value = JsonUtil.toJson(result);
        } catch (Exception e) {
            return newErrorResponse(String.format("Got %d history, but to json failed.", result.getData().size()));
        }
        // logger.info(String.format("Query Response:\nAccount: %s, History.size: %s\n", account, returnList.size()));
        return newSuccessResponse("succeed", ByteString.copyFrom(value, UTF_8).toByteArray());
    }

    public static void main(String[] args) {
        System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new Demo().start(args);
    }

}
