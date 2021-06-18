/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.mysql;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * This class includes utility functions.
 *
 * @since 1.2.0
 */
public class Utils {

    public static BMap generateOptionsMap(BMap mysqlOptions) {
        if (mysqlOptions != null) {
            BMap<BString, Object> options = ValueCreator.createMapValue();
            addSSLOptions(mysqlOptions.getMapValue(Constants.Options.SSL), options);

            long connectTimeout = getTimeout(mysqlOptions.get(Constants.Options.CONNECT_TIMEOUT));
            if (connectTimeout > 0) {
                options.put(Constants.DatabaseProps.CONNECT_TIMEOUT, connectTimeout);
            }

            long socketTimeout = getTimeout(mysqlOptions.get(Constants.Options.SOCKET_TIMEOUT));
            if (socketTimeout > 0) {
                options.put(Constants.DatabaseProps.SOCKET_TIMEOUT, socketTimeout);
            }

            BString connectionAttributes = mysqlOptions.getStringValue(Constants.Options.CONNECTION_ATTRIBUTES);
            if (connectionAttributes != null) {
                options.put(Constants.DatabaseProps.CONNECTION_ATTRIBUTES, connectionAttributes);
            }

            BString useConfigs = mysqlOptions.getStringValue(Constants.Options.USE_CONFIGS);
            if (useConfigs != null) {
                options.put(Constants.DatabaseProps.USE_CONFIGS, useConfigs);
            }

            boolean allowPublicKeyRetrieval = mysqlOptions.getBooleanValue(Constants.Options.ALLOW_PUBLICKEY_RETRIEVAL);
            options.put(Constants.DatabaseProps.ALLOW_PUBLICKEY_RETRIEVAL, allowPublicKeyRetrieval);

            BString serverTimezone = mysqlOptions.getStringValue(Constants.Options.SERVER_TIMEZONE);
            if (serverTimezone != null) {
                options.put(Constants.DatabaseProps.SERVER_TIMEZONE, serverTimezone);
            }
            return options;
        }
        return null;
    }

    public static long getTimeout(Object secondsDecimal) {
        if (secondsDecimal instanceof BDecimal) {
            BDecimal timeoutSec = (BDecimal) secondsDecimal;
            if (timeoutSec.floatValue() > 0) {
                return Double.valueOf(timeoutSec.floatValue() * 1000).longValue();
            }
        }
        return -1;
    }

    public static void addSSLOptions(BMap secureSocket, BMap<BString, Object> options) {
        if (secureSocket == null) {
            options.put(Constants.DatabaseProps.SSL_MODE, Constants.DatabaseProps.SSL_MODE_DISABLED);
        } else {
            BString mode = secureSocket.getStringValue(Constants.SecureSocket.MODE);
            options.put(Constants.DatabaseProps.SSL_MODE, mode);

            BMap clientCertKeystore = secureSocket.getMapValue(Constants.SecureSocket.CLIENT_KEY);
            if (clientCertKeystore != null) {
                options.put(Constants.DatabaseProps.CLIENT_KEYSTORE_URL, StringUtils.fromString(
                        Constants.FILE + clientCertKeystore.getStringValue(
                                Constants.SecureSocket.CryptoKeyStoreRecord.KEY_STORE_RECORD_PATH_FIELD)));
                options.put(Constants.DatabaseProps.CLIENT_KEYSTORE_PASSWORD,
                        clientCertKeystore.getStringValue(
                                Constants.SecureSocket.CryptoKeyStoreRecord.KEY_STORE_RECORD_PASSWORD_FIELD));
                options.put(Constants.DatabaseProps.CLIENT_KEYSTORE_TYPE,
                        Constants.DatabaseProps.KEYSTORE_TYPE_PKCS12);
            }

            BMap trustCertKeystore = secureSocket.getMapValue(Constants.SecureSocket.CLIENT_CERT);
            if (trustCertKeystore != null) {
                options.put(Constants.DatabaseProps.TRUST_KEYSTORE_URL,
                        StringUtils.fromString(Constants.FILE + trustCertKeystore.getStringValue(
                                Constants.SecureSocket.CryptoTrustStoreRecord.TRUST_STORE_RECORD_PATH_FIELD)));
                options.put(Constants.DatabaseProps.TRUST_KEYSTORE_PASSWORD,
                        trustCertKeystore.getStringValue(
                                Constants.SecureSocket.CryptoTrustStoreRecord.TRUST_STORE_RECORD_PASSWORD_FIELD));
                options.put(Constants.DatabaseProps.TRUST_KEYSTORE_TYPE,
                        Constants.DatabaseProps.KEYSTORE_TYPE_PKCS12);
            }
        }
    }
}
