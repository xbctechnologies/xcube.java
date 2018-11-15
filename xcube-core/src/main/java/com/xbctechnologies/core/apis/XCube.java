package com.xbctechnologies.core.apis;

import com.xbctechnologies.core.apis.dto.req.tx.TxRequest;
import com.xbctechnologies.core.apis.dto.res.BoolResponse;
import com.xbctechnologies.core.apis.dto.res.LongResponse;
import com.xbctechnologies.core.apis.dto.res.Response;
import com.xbctechnologies.core.apis.dto.res.account.AccountAddrListResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBalanceResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountBondInfoResponse;
import com.xbctechnologies.core.apis.dto.res.account.AccountResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockResponse;
import com.xbctechnologies.core.apis.dto.res.block.BlockTxCntResponse;
import com.xbctechnologies.core.apis.dto.res.data.*;
import com.xbctechnologies.core.apis.dto.res.network.NetworkPeersResponse;
import com.xbctechnologies.core.apis.dto.res.node.XChainInfoResponse;
import com.xbctechnologies.core.apis.dto.res.tx.*;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorBondResponse;
import com.xbctechnologies.core.apis.dto.res.validator.ValidatorSetResponse;
import com.xbctechnologies.core.component.rest.RestHttpClient;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.CurrencyUtil;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class XCube {
    private RestHttpClient restHttpClient;

    public XCube(RestHttpClient restHttpClient) {
        this.restHttpClient = restHttpClient;
    }

    /**
     * Tx
     */
    public Request<?, TxSendResponse> sendTransaction(TxRequest txRequest) throws TransactionException {
        String method = txRequest.getPayloadBody().getMethod();

        if (!txRequest.isNotCheckValidation()) {
            txRequest.validate();
            txRequest.getPayloadBody().validate();
        }
        txRequest.postProcess();

        return new Request<>(
                restHttpClient,
                txRequest.getReqId(),
                method,
                Arrays.asList(txRequest),
                TxSendResponse.class
        );
    }

    public Request<?, TxSendResponse> bonding(TxRequest txRequest) throws TransactionException {
        return sendTransaction(txRequest);
    }

    public Request<?, TxSendResponse> unbonding(TxRequest txRequest) throws TransactionException {
        return sendTransaction(txRequest);
    }

    public Request<?, TxSendResponse> delegating(TxRequest txRequest) throws TransactionException {
        return sendTransaction(txRequest);
    }

    public Request<?, TxSendResponse> undelegating(TxRequest txRequest) throws TransactionException {
        return sendTransaction(txRequest);
    }

    public Request<?, TxSignResponse> signTransaction(Long reqId, TxRequest txRequest) {
        return new Request<>(
                restHttpClient,
                reqId,
                "tx_signTransaction",
                Arrays.asList(txRequest),
                TxSignResponse.class
        );
    }

    public Request<?, TxCheckOriginalResponse> checkOriginal(Long reqId, String targetChainId, String dataAccountAddr, File file) throws TransactionException {
        if (!file.exists()) {
            throw new TransactionException("The file does not exist.");
        }

        return new Request<>(
                restHttpClient,
                reqId,
                "tx_checkOriginal",
                Arrays.asList(targetChainId, dataAccountAddr, Base64Util.encode(file)),
                TxCheckOriginalResponse.class
        );
    }

    public Request<?, TxResponse> getTransaction(Long reqId, String targetChainId, String txHash) {
        return new Request<>(
                restHttpClient,
                reqId,
                "tx_getTransaction",
                Arrays.asList(targetChainId, txHash),
                TxResponse.class
        );
    }

    public Request<?, TxReceiptResponse> getTransactionReceipt(Long reqId, String targetChainId, String txHash) {
        return new Request<>(
                restHttpClient,
                reqId,
                "tx_getTransactionReceipt",
                Arrays.asList(targetChainId, txHash),
                TxReceiptResponse.class
        );
    }

    /**
     * Block
     */
    public Request<?, BlockResponse> getBlock(Long reqId, String targetChainId, String blockHash) {
        return new Request<>(
                restHttpClient,
                reqId,
                "block_getBlock",
                Arrays.asList(targetChainId, blockHash),
                BlockResponse.class
        );
    }

    public Request<?, BlockResponse> getBlockByNumber(Long reqId, String targetChainId, long blockNo) {
        return new Request<>(
                restHttpClient,
                reqId,
                "block_getBlockByNumber",
                Arrays.asList(targetChainId, blockNo),
                BlockResponse.class
        );
    }

    public Request<?, BlockTxCntResponse> getBlockTxCount(Long reqId, String targetChainId, Object block) {
        List params;
        if (NumberUtils.isParsable(block.toString())) {
            params = Arrays.asList(targetChainId, Long.parseLong(block.toString()));
        } else {
            params = Arrays.asList(targetChainId, block.toString());
        }

        return new Request<>(
                restHttpClient,
                reqId,
                "block_getBlockTxCount",
                params,
                BlockTxCntResponse.class
        );
    }

    public Request<?, BlockResponse> getBlockLatestBlock(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "block_getBlockLatestBlock",
                Arrays.asList(targetChainId),
                BlockResponse.class
        );
    }

    /**
     * Account
     */
    public Request<?, AccountResponse> newAccount(Long reqId, String password) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_new",
                Arrays.asList(password),
                AccountResponse.class
        );
    }


    public Request<?, BoolResponse> lockAccount(Long reqId, String targetChainId, String accountAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_lock",
                Arrays.asList(targetChainId, accountAddr),
                BoolResponse.class
        );
    }

    public Request<?, BoolResponse> unlockAccount(Long reqId, String targetChainId, String accountAddr, String password, long secondsDuration) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_unlock",
                Arrays.asList(targetChainId, accountAddr, password, secondsDuration),
                BoolResponse.class
        );
    }

    public Request<?, BoolResponse> importAccount(Long reqId, String priKeyHexstr, String address, String passphrase) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_import",
                //Arrays.asList(content),
                Arrays.asList(priKeyHexstr, address, passphrase),
                BoolResponse.class
        );
    }

    public Request<?, AccountAddrListResponse> getListAccount(Long reqId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_getList",
                Arrays.asList(),
                AccountAddrListResponse.class
        );
    }

    public Request<?, AccountBalanceResponse> getBalance(Long reqId, String targetChainId, String accountAddr, CurrencyUtil.CurrencyType currencyType) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_getBalance",
                Arrays.asList(targetChainId, accountAddr, currencyType == null ? CurrencyUtil.CurrencyType.XTOType : currencyType),
                AccountBalanceResponse.class
        );
    }

    public Request<?, AccountBondInfoResponse> getBonding(Long reqId, String targetChainId, String accountAddr, CurrencyUtil.CurrencyType currencyType) {
        return new Request<>(
                restHttpClient,
                reqId,
                "account_getBonding",
                Arrays.asList(targetChainId, accountAddr, currencyType == null ? CurrencyUtil.CurrencyType.XTOType : currencyType),
                AccountBondInfoResponse.class
        );
    }

    /**
     * Validator
     */
    public Request<?, BoolResponse> isValidator(Long reqId, String targetChainId, String accountAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "validator_isValidator",
                Arrays.asList(targetChainId, accountAddr),
                BoolResponse.class
        );
    }

    public Request<?, ValidatorBondResponse> getValidatorsOf(Long reqId, String targetChainId, String accountAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "validator_getValidatorsOf",
                Arrays.asList(targetChainId, accountAddr),
                ValidatorBondResponse.class
        );
    }

    public Request<?, ValidatorSetResponse> getValidatorSet(Long reqId, String targetChainId, long blockNo) {
        return new Request<>(
                restHttpClient,
                reqId,
                "validator_getValidatorSet",
                Arrays.asList(targetChainId, blockNo),
                ValidatorSetResponse.class
        );
    }

    /**
     * Network
     */
    public Request<?, LongResponse> getPeerCnt(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "network_getPeerCnt",
                Arrays.asList(targetChainId),
                LongResponse.class
        );
    }

    public Request<?, NetworkPeersResponse> getPeers(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "network_getPeers",
                Arrays.asList(targetChainId),
                NetworkPeersResponse.class
        );
    }

    /*public Request<?, BoolResponse> addPeer(Long reqId, String targetChainId, String[] peers, boolean persistent) {
        return new Request<>(
                restHttpClient,
                reqId,
                "network_addPeer",
                Arrays.asList(targetChainId, peers, persistent),
                BoolResponse.class
        );
    }*/

    /**
     * Node
     */
    public Request<?, BoolResponse> sync(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "node_sync",
                Arrays.asList(targetChainId),
                BoolResponse.class
        );
    }

    public Request<?, BoolResponse> isSync(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "node_isSync",
                Arrays.asList(targetChainId),
                BoolResponse.class
        );
    }

    public Request<?, XChainInfoResponse> getXChainInfo(Long reqId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "node_getXChainInfo",
                Arrays.asList(),
                XChainInfoResponse.class
        );
    }

    public Request<?, Response> getVersion(Long reqId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "node_getVersion",
                Arrays.asList(),
                Response.class
        );
    }

    /**
     * Data Qeury
     */
    public Request<?, DataAccountResponse> getDataAccount(Long reqId, String targetChainId, String dataAccountAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getDataAccount",
                Arrays.asList(targetChainId, dataAccountAddr),
                DataAccountResponse.class
        );
    }

    public Request<?, AccountDataResponse> getAccount(Long reqId, String targetChainId, String accountAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getAccount",
                Arrays.asList(targetChainId, accountAddr),
                AccountDataResponse.class
        );
    }

    public Request<?, ValidatorListResponse> getValidatorList(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getValidatorList",
                Arrays.asList(targetChainId),
                ValidatorListResponse.class
        );
    }

    public Request<?, ValidatorResponse> getValidator(Long reqId, String targetChainId, String validatorAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getValidator",
                Arrays.asList(targetChainId, validatorAddr),
                ValidatorResponse.class
        );
    }

    public Request<?, SimpleValidatorResponse> getSimpleValidator(Long reqId, String targetChainId, String validatorAddr) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getSimpleValidator",
                Arrays.asList(targetChainId, validatorAddr),
                SimpleValidatorResponse.class
        );
    }

    public Request<?, SimpleValidatorsResponse> getSimpleValidators(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getSimpleValidators",
                Arrays.asList(targetChainId),
                SimpleValidatorsResponse.class
        );
    }

    public Request<?, TotalAtxResponse> getTotalATX(Long reqId, String targetChainId, CurrencyUtil.CurrencyType currencyType) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getTotalATX",
                Arrays.asList(targetChainId, currencyType),
                TotalAtxResponse.class
        );
    }

    public Request<?, ProgressGovernance> getProgressGovernance(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getProgressGovernance",
                Arrays.asList(targetChainId),
                ProgressGovernance.class
        );
    }

    public Request<?, CurrentGovernance> getCurrentGovernance(Long reqId, String targetChainId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getCurrentGovernance",
                Arrays.asList(targetChainId),
                CurrentGovernance.class
        );
    }

    public Request<?, LongResponse> getRPCSize(Long reqId) {
        return new Request<>(
                restHttpClient,
                reqId,
                "data_getRPCSize",
                Arrays.asList(),
                LongResponse.class
        );
    }
}
