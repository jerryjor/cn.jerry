/*
 *  Copyright 2016, 2017 IBM, DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package cn.jerry.fabric.api.fabric.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ChaincodeConfig {
    private static final Logger logger = LogManager.getLogger();

    private static final String CONF_FILE = "/chaincode/chaincode.properties";
    private static final String CHAINCODE_FILE_DIR = LocalPathTool.removeWindowsDrive(
            ChaincodeConfig.class.getResource("/chaincode").getPath());

    private static final String KEY_DEPLOY_TIMEOUT = "deploy.timeout";
    private static final String KEY_PROPOSAL_TIMEOUT = "proposal.timeout";
    private static final String KEY_INVOKE_TIMEOUT = "invoke.timeout";

    private static ChaincodeConfig instance = new ChaincodeConfig();
    private final Properties cache = new Properties();

    private ChaincodeConfig() {
        try {
            cache.load(ChaincodeConfig.class.getResourceAsStream(CONF_FILE));
        } catch (IOException e) {
            logger.error("load properties failed. file: {}", CONF_FILE, e);
        }
    }

    public static ChaincodeConfig getInstance() {
        return instance;
    }

    public File getDefaultEndorsementPolicy() throws FileNotFoundException {
        File pf = new File(CHAINCODE_FILE_DIR + "/policy.default.yaml");
        if (!pf.exists() || !pf.isFile()) {
            throw new FileNotFoundException("Missing chaincode policy file " + pf.getAbsolutePath());
        }
        return pf;
    }

    public int getDeployTimeout() {
        return Integer.parseInt(cache.getProperty(KEY_DEPLOY_TIMEOUT, "120000"));
    }

    public int getProposalTimeout() {
        return Integer.parseInt(cache.getProperty(KEY_PROPOSAL_TIMEOUT, "120000"));
    }

    public int getInvokeTimeout() {
        return Integer.parseInt(cache.getProperty(KEY_INVOKE_TIMEOUT, "120000"));
    }

}
