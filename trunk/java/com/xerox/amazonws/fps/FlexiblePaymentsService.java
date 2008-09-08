package com.xerox.amazonws.fps;

import com.xerox.amazonws.common.AWSError;
import com.xerox.amazonws.common.AWSException;
import com.xerox.amazonws.common.AWSQueryConnection;
import com.xerox.amazonws.sdb.DataUtils;
import com.xerox.amazonws.typica.fps.jaxb.CancelTokenResponse;
import com.xerox.amazonws.typica.fps.jaxb.DiscardResultsResponse;
import com.xerox.amazonws.typica.fps.jaxb.FundPrepaidResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetAccountActivityResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetAccountBalanceResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetAllCreditInstrumentsResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetDebtBalanceResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetOutstandingDebtBalanceResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetPaymentInstructionResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetTokenByCallerResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetTokensResponse;
import com.xerox.amazonws.typica.fps.jaxb.GetTransactionResponse;
import com.xerox.amazonws.typica.fps.jaxb.InstallPaymentInstructionResponse;
import com.xerox.amazonws.typica.fps.jaxb.OutstandingDebtBalance;
import com.xerox.amazonws.typica.fps.jaxb.PayResponse;
import com.xerox.amazonws.typica.fps.jaxb.RefundResponse;
import com.xerox.amazonws.typica.fps.jaxb.ResponseStatus;
import com.xerox.amazonws.typica.fps.jaxb.ServiceError;
import com.xerox.amazonws.typica.fps.jaxb.ServiceErrors;
import com.xerox.amazonws.typica.fps.jaxb.SettleDebtResponse;
import com.xerox.amazonws.typica.fps.jaxb.TransactionResponse;
import com.xerox.amazonws.typica.fps.jaxb.WriteOffDebtResponse;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.Collator;

/**
 * This class provides an interface with the Amazon FPS service.
 *
 * @author J. Bernard
 * @author Elastic Grid, LLC.
 * @author jerome.bernard@elastic-grid.com
 */
public class FlexiblePaymentsService extends AWSQueryConnection {
    private final String callerToken;
    private final String recipientToken;
    private final DescriptorPolicy descriptorPolicy;
    private final String uiPipeline;
    private static Log logger = LogFactory.getLog(FlexiblePaymentsService.class);

    /**
     * Initializes the FPS service with your AWS login information.
     *
     * @param awsAccessId  The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     */
    public FlexiblePaymentsService(String awsAccessId, String awsSecretKey) {
        this(awsAccessId, awsSecretKey, true, null, null, null);
    }


    /**
     * Initializes the FPS service with your AWS login information.
     *
     * @param awsAccessId  The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param callerToken  the default caller token to be used when not explicitely specified
     * @param recipientToken the default recipient token to be used when not explicitely specified
     * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
     */
    public FlexiblePaymentsService(String awsAccessId, String awsSecretKey,
                                   String callerToken, String recipientToken, DescriptorPolicy descriptorPolicy) {
        this(awsAccessId, awsSecretKey, true, callerToken, recipientToken, descriptorPolicy);
    }

    /**
     * Initializes the FPS service with your AWS login information.
     *
     * @param awsAccessId  The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure     True if the data should be encrypted on the wire on the way to or from FPS.
     * @param callerToken  the default caller token to be used when not explicitely specified
     * @param recipientToken the default recipient token to be used when not explicitely specified
     * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
     */
    public FlexiblePaymentsService(String awsAccessId, String awsSecretKey, boolean isSecure,
                                   String callerToken, String recipientToken, DescriptorPolicy descriptorPolicy) {
        this(awsAccessId, awsSecretKey, isSecure, callerToken, recipientToken, descriptorPolicy,
                "fps.amazonaws.com", "https://authorize.payments.amazon.com/cobranded-ui/actions/start");
    }

    /**
     * Initializes the FPS service with your AWS login information.
     *
     * @param awsAccessId  The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure     True if the data should be encrypted on the wire on the way to or from FPS.
     * @param callerToken  the default caller token to be used when not explicitely specified
     * @param recipientToken the default recipient token to be used when not explicitely specified
     * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
     * @param server       Which host to connect to.  Usually, this will be fps.amazonaws.com.
     *                     You can also use fps.sandbox.amazonaws.com instead if you want to test your code within the Sandbox environment
     * @param uiPipeline   the URL of the UI pipeline
     */
    public FlexiblePaymentsService(String awsAccessId, String awsSecretKey, boolean isSecure,
                                   String callerToken, String recipientToken, DescriptorPolicy descriptorPolicy,
                                   String server, String uiPipeline) {
        this(awsAccessId, awsSecretKey, isSecure, callerToken, recipientToken, descriptorPolicy,
                server, isSecure ? 443 : 80, uiPipeline);
    }

    /**
     * Initializes the FPS service with your AWS login information.
     *
     * @param awsAccessId  The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure     True if the data should be encrypted on the wire on the way to or from FPS.
     * @param callerToken  the default caller token to be used when not explicitely specified
     * @param recipientToken the default recipient token to be used when not explicitely specified
     * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
     * @param server       Which host to connect to.  Usually, this will be fps.amazonaws.com.
     *                     You can also use fps.sandbox.amazonaws.com instead if you want to test your code within the Sandbox environment
     * @param port         Which port to use
     * @param uiPipeline   the URL of the UI pipeline
     */
    public FlexiblePaymentsService(String awsAccessId, String awsSecretKey, boolean isSecure,
                                   String callerToken, String recipientToken, DescriptorPolicy descriptorPolicy,
                                   String server, int port, String uiPipeline) {
        super(awsAccessId, awsSecretKey, isSecure, server, port);
        this.uiPipeline = uiPipeline;
        this.callerToken = callerToken;
        this.recipientToken = recipientToken;
        this.descriptorPolicy = descriptorPolicy;
        setVersionHeader(this);
    }

    /**
     * This method returns the signature version
     *
     * @return the version
     */
    public int getSignatureVersion() {
        return super.getSignatureVersion();
    }

    /**
     * This method sets the signature version used to sign requests (0 or 1).
     *
     * @param version signature version
     */
    public void setSignatureVersion(int version) {
        super.setSignatureVersion(version);
    }

    /**
     * Cancel any token that you installed on your own account.
     *
     * @param tokenID the token to be cancelled
     * @throws FPSException wraps checked exceptions
     */
    public void cancelToken(String tokenID) throws FPSException {
        cancelToken(tokenID, "");
    }

    /**
     * Cancel any token that you installed on your own account.
     *
     * @param tokenID the token to be cancelled
     * @param reason  reason for cancelling the token -- max 64 characters
     * @return the request ID
     * @throws FPSException wraps checked exceptions
     */
    public String cancelToken(String tokenID, String reason) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("TokenId", tokenID);
        params.put("ReasonText", reason);
        GetMethod method = new GetMethod();
        try {
            CancelTokenResponse response =
                    makeRequestInt(method, "CancelToken", params, CancelTokenResponse.class);
            return response.getRequestId();
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Discard the results that are fetched using the {@link #getResults()} operation.
     *
     * @param transactionIDs the list of transaction to be discarded
     * @throws FPSException wraps checked exceptions
     */
    public void discardResults(String... transactionIDs) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < transactionIDs.length; i++)
            params.put("TransactionID." + i, transactionIDs[i]);
        GetMethod method = new GetMethod();
        try {
            makeRequestInt(method, "DiscardResults", params, DiscardResultsResponse.class);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Transfer money from the sender's payment instrument specified in the funding token to the recipient's account
     * balance. This operation creates a prepaid balance on the sender' prepaid instrument.
     * Note: there is no support for <tt>NewSenderTokenUsage</tt> yet.
     *
     * @param senderTokenID       the token identifying the funding payment instructions
     * @param prepaidInstrumentID the prepaid instrument ID returned by the prepaid instrument installation pipeline
     * @param fundingAmount       amount to fund the prepaid instrument
     * @param callerReference     a unique reference that you specify in your system to identify a transaction
     * @return the completed transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction fundPrepaid(String senderTokenID, String prepaidInstrumentID,
                                   double fundingAmount, String callerReference) throws FPSException {
        return fundPrepaid(senderTokenID, callerToken, prepaidInstrumentID,
                fundingAmount, new Date(),
                null, null, callerReference,
                ChargeFeeTo.RECIPIENT,
                null, null, null,
                null
        );
    }

    /**
     * Transfer money from the sender's payment instrument specified in the funding token to the recipient's account
     * balance. This operation creates a prepaid balance on the sender' prepaid instrument.
     * Note: there is no support for <tt>NewSenderTokenUsage</tt> yet.
     *
     * @param senderTokenID        the token identifying the funding payment instructions
     * @param callerTokenID        the caller's token ID
     * @param prepaidInstrumentID  the prepaid instrument ID returned by the prepaid instrument installation pipeline
     * @param fundingAmount        amount to fund the prepaid instrument
     * @param transactionDate      the date specified by the caller and stored with the transaction
     * @param senderReference      any reference that the caller might use to identify the sender in the transaction
     * @param recipientReference   any reference that the caller might use to identify the recipient in the transaction
     * @param callerReference      a unique reference that you specify in your system to identify a transaction
     * @param chargeFeeTo          the participant paying the fee for the transaction
     * @param senderDescription    128-byte field to store transaction description
     * @param recipientDescription 128-byte field to store transaction description
     * @param callerDescription    128-byte field to store transaction description
     * @param metadata             a 2KB free-form field used to store transaction data
     * @return the completed transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction fundPrepaid(String senderTokenID, String callerTokenID, String prepaidInstrumentID,
                                   double fundingAmount, Date transactionDate,
                                   String senderReference, String recipientReference, String callerReference,
                                   ChargeFeeTo chargeFeeTo,
                                   String senderDescription, String recipientDescription, String callerDescription,
                                   String metadata) throws FPSException {
        if (callerTokenID == null)
            throw new IllegalArgumentException("Caller Token ID can't be null");
        Map<String, String> params = new HashMap<String, String>();
        params.put("SenderTokenId", senderTokenID);
        params.put("CallerTokenId", callerTokenID);
        params.put("PrepaidInstrumentId", prepaidInstrumentID);
        params.put("FundingAmount", Double.toString(fundingAmount));
        params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
        if (senderReference != null)
            params.put("SenderReference", senderReference);
        if (recipientReference != null)
            params.put("RecipientReference", recipientReference);
        params.put("CallerReference", callerReference);
        params.put("ChargeFeeTo", chargeFeeTo.value());
        params.put("ChargeFeeTo", chargeFeeTo.value());
        if (senderDescription != null)
            params.put("SenderDescription", senderDescription);
        if (recipientDescription != null)
            params.put("RecipientDescription", recipientDescription);
        if (callerDescription != null)
            params.put("CallerDescription", callerDescription);
        if (metadata != null)
            params.put("MetaData", metadata);
        GetMethod method = new GetMethod();
        try {
            FundPrepaidResponse response =
                    makeRequestInt(method, "FundPrepaid", params, FundPrepaidResponse.class);
            TransactionResponse transactionResponse = response.getTransactionResponse();
            return new Transaction(
                    transactionResponse.getTransactionId(),
                    Transaction.Status.valueOf(transactionResponse.getStatus().value()),
                    transactionResponse.getStatusDetail()
                    // todo: transactionResponse.getNewSenderTokenUsage()
            );
        } finally {
            method.releaseConnection();
        }
    }

    public AccountActivity getAccountActivity(Date startDate) throws FPSException {
        return getAccountActivity(null, null, 0, startDate, null, null);
    }

    public AccountActivity getAccountActivity(Date startDate, Date endDate) throws FPSException {
        return getAccountActivity(null, null, 0, startDate, endDate, null);
    }
    
    public AccountActivity getAccountActivity(FPSOperation filter, PaymentMethod paymentMethod, int maxBatchSize,
                                              Date startDate, Date endDate, Transaction.Status transactionStatus)
            throws FPSException {
        if (startDate == null)
            throw new IllegalArgumentException("The start date should not be null!");
        Map<String, String> params = new HashMap<String, String>();
        if (filter != null)
            params.put("Operation", filter.value());
        if (paymentMethod != null)
            params.put("PaymentMethod", paymentMethod.value());
        if (maxBatchSize != 0)
            params.put("MaxBatchSize", Integer.toString(maxBatchSize));
        params.put("StartDate", DataUtils.encodeDate(startDate));
        if (endDate != null)
            params.put("EndDate", DataUtils.encodeDate(endDate));
        if (transactionStatus != null)
            params.put("Status", transactionStatus.value());
        GetMethod method = new GetMethod();
        try {
            GetAccountActivityResponse response =
                    makeRequestInt(method, "GetAccountActivity", params, GetAccountActivityResponse.class);
            Date nextStartDate = null;
            if (response.getStartTimeForNextTransaction() != null)
                nextStartDate = response.getStartTimeForNextTransaction().toGregorianCalendar().getTime();
            BigInteger nbTransactions = response.getResponseBatchSize();
            List<com.xerox.amazonws.typica.fps.jaxb.Transaction> rawTransactions = response.getTransactions();
            List<Transaction> transactions = new ArrayList<Transaction>(rawTransactions.size());
            for (com.xerox.amazonws.typica.fps.jaxb.Transaction txn : rawTransactions) {
                com.xerox.amazonws.typica.fps.jaxb.Amount txnAmount = txn.getTransactionAmount();
                com.xerox.amazonws.typica.fps.jaxb.Amount fees = txn.getFees();
                com.xerox.amazonws.typica.fps.jaxb.Amount balance = txn.getBalance();
                transactions.add(new Transaction(
                        txn.getTransactionId(), Transaction.Status.fromValue(txn.getStatus().value()),
                        txn.getDateReceived().toGregorianCalendar().getTime(),
                        txn.getDateCompleted().toGregorianCalendar().getTime(),
                        new Amount(new BigDecimal(txnAmount.getAmount()), txnAmount.getCurrencyCode().toString()),
                        FPSOperation.fromValue(txn.getOperation().value()),
                        PaymentMethod.fromValue(txn.getPaymentMethod().value()),
                        txn.getSenderName(), txn.getCallerName(), txn.getRecipientName(),
                        new Amount(new BigDecimal(fees.getAmount()), fees.getCurrencyCode().toString()),
                        new Amount(new BigDecimal(balance.getAmount()), balance.getCurrencyCode().toString()),
                        txn.getCallerTokenId(), txn.getSenderTokenId(), txn.getRecipientTokenId()
                ));
            }
            return new AccountActivity(nextStartDate, nbTransactions, transactions,
                    filter, paymentMethod, maxBatchSize, endDate, transactionStatus, this);
        } finally {
            method.releaseConnection();
        }
    }

    /** TODO: getAllPrepaidInstruments
     public void getAllPrepaidInstruments() throws FPSException {

     }
     **/

    /**
     * Retrieve all credit instruments associated with an account.
     * @return the list of credit instruments IDs associated with the account
     * @throws FPSException wraps checked exceptions
     */
    public List<String> getAllCreditInstruments() throws FPSException {
        return getAllCreditInstruments(null);
    }

    /**
     * Retrieve all credit instruments associated with an account.
     * @return the list of credit instruments balances associated with the account
     * @throws FPSException wraps checked exceptions
     */
    public List<DebtBalance> getAllCreditInstrumentBalances() throws FPSException {
        return getAllCreditInstrumentBalances(null);
    }

    /**
     * Retrieve all credit instruments associated with an account.
     * @param instrumentStatus filter instruments by status
     * @return the list of credit instruments IDs associated with the account
     * @throws FPSException wraps checked exceptions
     */
    public List<String> getAllCreditInstruments(Instrument.Status instrumentStatus) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        if (instrumentStatus != null)
            params.put("InstrumentStatus", instrumentStatus.value());
        GetMethod method = new GetMethod();
        try {
            GetAllCreditInstrumentsResponse response =
                    makeRequestInt(method, "GetAllCreditInstruments", params, GetAllCreditInstrumentsResponse.class);
            return response.getCreditInstrumentIds();
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Retrieve all credit instruments associated with an account.
     * @param instrumentStatus filter instruments by status
     * @return the list of credit instruments balances associated with the account
     * @throws FPSException wraps checked exceptions
     */
    public List<DebtBalance> getAllCreditInstrumentBalances(Instrument.Status instrumentStatus) throws FPSException {
        List<String> creditInstruments = getAllCreditInstruments(instrumentStatus);
        List<DebtBalance> balances = new ArrayList<DebtBalance>(creditInstruments.size());
        for (String instrument : creditInstruments)
            balances.add(getDebtBalance(instrument));
        return balances;
    }

    /**
     * Retrieve the balance of a credit instrument.
     * Note: nly on the instruments for which you are the sender or the recipient can be queried
     * @param creditInstrumentId the credit instrument Id for which debt balance is queried
     * @return the balance
     * @throws FPSException wraps checked exceptions
     */
    public DebtBalance getDebtBalance(String creditInstrumentId) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("CreditInstrumentId", creditInstrumentId);
        GetMethod method = new GetMethod();
        try {
            GetDebtBalanceResponse response =
                    makeRequestInt(method, "GetDebtBalance", params, GetDebtBalanceResponse.class);
            com.xerox.amazonws.typica.fps.jaxb.DebtBalance balance = response.getDebtBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount availableBalance = balance.getAvailableBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount pendingOutBalance = balance.getPendingOutBalance();
            return new DebtBalance(
                    new Amount(new BigDecimal(availableBalance.getAmount()), availableBalance.getCurrencyCode().toString()),
                    new Amount(new BigDecimal(pendingOutBalance.getAmount()), pendingOutBalance.getCurrencyCode().toString())
            );
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Retrieve balances of all credit instruments owned by the sender.
     * Note: nly on the instruments for which you are the sender or the recipient can be queried
     * @return the aggregated balance
     * @throws FPSException wraps checked exceptions
     */
    public DebtBalance getOutstandingDebtBalance() throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        GetMethod method = new GetMethod();
        try {
            GetOutstandingDebtBalanceResponse response =
                    makeRequestInt(method, "GetOutstandingDebtBalance", params, GetOutstandingDebtBalanceResponse.class);
            OutstandingDebtBalance balance = response.getOutstandingDebt();
            com.xerox.amazonws.typica.fps.jaxb.Amount outstanding = balance.getOutstandingBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount pendingOut = balance.getPendingOutBalance();
            return new DebtBalance(
                    new Amount(new BigDecimal(outstanding.getAmount()), outstanding.getCurrencyCode().toString()),
                    new Amount(new BigDecimal(pendingOut.getAmount()), pendingOut.getCurrencyCode().toString())
            );
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Install tokens (payment instructions) on your own accounts.
     *
     * @param tokenID token for which the payment instruction is to be retrieved
     * @return a 64-character alphanumeric string that represents the installed payment instruction
     * @throws FPSException wraps checked exceptions
     */
    public PaymentInstructionDetail getPaymentInstruction(String tokenID) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("TokenId", tokenID);
        GetMethod method = new GetMethod();
        try {
            GetPaymentInstructionResponse response =
                    makeRequestInt(method, "GetPaymentInstruction", params, GetPaymentInstructionResponse.class);
            Token token = new Token(
                    response.getToken().getTokenId(),
                    response.getToken().getFriendlyName(),
                    Token.Status.fromValue(response.getToken().getStatus().value()),
                    response.getToken().getDateInstalled().toGregorianCalendar().getTime(),
                    response.getToken().getCallerInstalled(),
                    TokenType.fromValue(response.getToken().getTokenType().value()),
                    response.getToken().getOldTokenId(),
                    response.getToken().getPaymentReason()
            );
            return new PaymentInstructionDetail(token, response.getPaymentInstruction(),
                    response.getAccountId(), response.getTokenFriendlyName());
        } finally {
            method.releaseConnection();
        }
    }

    /** TODO: getPrepaidBalance
     public void getPrepaidBalance() throws FPSException {

     }
     **/

    /** TODO: getResults
     public void getResults() throws FPSException {

     }
     **/

    public List<Token> getAllTokens() throws FPSException {
        return getTokens(null, null, null);
    }

    public List<Token> getTokensByFriendlyName(String friendlyName) throws FPSException {
        return getTokens(friendlyName, null, null);
    }

    public List<Token> getTokensByFriendlyName(Token.Status tokenStatus) throws FPSException {
        return getTokens(null, tokenStatus, null);
    }

    public List<Token> getTokensByCallerReference(String callerRefence) throws FPSException {
        return getTokens(null, null, callerRefence);
    }

    public List<Token> getTokens(String tokenFriendlyName, Token.Status tokenStatus, String callerReference) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        if (tokenFriendlyName != null)
            params.put("TokenFriendlyName", tokenFriendlyName);
        if (tokenStatus != null)
            params.put("TokenStatus", tokenStatus.value());
        if (callerReference != null)
            params.put("CallerReference", callerReference);
        GetMethod method = new GetMethod();
        try {
            GetTokensResponse response =
                    makeRequestInt(method, "GetTokens", params, GetTokensResponse.class);
            List<com.xerox.amazonws.typica.fps.jaxb.Token> rawTokens = response.getTokens();
            List<Token> tokens = new ArrayList<Token>(rawTokens.size());
            for (com.xerox.amazonws.typica.fps.jaxb.Token token : rawTokens) {
                tokens.add(new Token(
                    token.getTokenId(),
                    token.getFriendlyName(),
                    Token.Status.fromValue(token.getStatus().value()),
                    token.getDateInstalled().toGregorianCalendar().getTime(),
                    token.getCallerInstalled(),
                    TokenType.fromValue(token.getTokenType().value()),
                    token.getOldTokenId(),
                    token.getPaymentReason()
                ));
            }
            return tokens;
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Fetch the details of a particular token you installed using the Amazon FPS co-branded UI pipeline
     *
     * @param tokenID the token Id of the specific token installed on the callers account
     * @return the token
     * @throws FPSException wraps checked exceptions
     */
    public Token getTokenByID(String tokenID) throws FPSException {
        return getToken(tokenID, null);
    }

    /**
     * Fetch the details of a particular token you installed using the Amazon FPS co-branded UI pipeline
     *
     * @param callerReference the caller reference that was passed at the time of the token installation
     * @return the token
     * @throws FPSException wraps checked exceptions
     */
    public Token getTokenByCaller(String callerReference) throws FPSException {
        return getToken(null, callerReference);
    }

    /**
     * Fetch the details of a particular token you installed using the Amazon FPS co-branded UI pipeline
     *
     * @param tokenID the token Id of the specific token installed on the callers account
     * @param callerReference the caller reference that was passed at the time of the token installation
     * @return the token
     * @throws FPSException wraps checked exceptions
     */
    private Token getToken(String tokenID, String callerReference) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        if (tokenID == null && callerReference == null)
            throw new IllegalArgumentException("Either the token ID or the caller reference must be given!");
        if (tokenID != null)
            params.put("TokenId", tokenID);
        if (callerReference != null)
            params.put("CallerReference", callerReference);
        GetMethod method = new GetMethod();
        try {
            GetTokenByCallerResponse response =
                    makeRequestInt(method, "GetTokenByCaller", params, GetTokenByCallerResponse.class);
            return new Token(
                    response.getToken().getTokenId(),
                    response.getToken().getFriendlyName(),
                    Token.Status.fromValue(response.getToken().getStatus().value()),
                    response.getToken().getDateInstalled().toGregorianCalendar().getTime(),
                    response.getToken().getCallerInstalled(),
                    TokenType.fromValue(response.getToken().getTokenType().value()),
                    response.getToken().getOldTokenId(),
                    response.getToken().getPaymentReason()
            );
        } finally {
            method.releaseConnection();
        }
    }

    /** TODO: getTokenUsage
     public void getTokenUsage() throws FPSException {

     }
     **/

    /** TODO: getTotalPrepaidLiability
     public void getTotalPrepaidLiability() throws FPSException {

     }
     **/

    /**
     * Fetch details of a transaction referred by the <tt>transactionId</tt>.
     * @param transactionID a transaction Id for the query
     * @return the transaction
     * @throws FPSException wraps checked exceptions
     */
    public TransactionDetail getTransaction(String transactionID) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("TransactionId", transactionID);
        GetMethod method = new GetMethod();
        try {
            GetTransactionResponse response =
                    makeRequestInt(method, "GetTransaction", params, GetTransactionResponse.class);
            com.xerox.amazonws.typica.fps.jaxb.TransactionDetail txn = response.getTransaction();
            return new TransactionDetail(
                    txn.getTransactionId(),
                    txn.getCallerTransactionDate().toGregorianCalendar().getTime(),
                    txn.getDateReceived().toGregorianCalendar().getTime(),
                    txn.getDateCompleted().toGregorianCalendar().getTime(),
                    new Amount(
                            new BigDecimal(txn.getTransactionAmount().getAmount()),
                            txn.getTransactionAmount().getCurrencyCode().value()
                    ),
                    new Amount(
                            new BigDecimal(txn.getFees().getAmount()),
                            txn.getFees().getCurrencyCode().value()
                    ),
                    txn.getCallerTokenId(), txn.getSenderTokenId(), txn.getRecipientTokenId(),
                    txn.getPrepaidInstrumentId(), txn.getCreditInstrumentId(),
                    FPSOperation.fromValue(txn.getOperation().value()),
                    PaymentMethod.fromValue(txn.getPaymentMethod().value()),
                    Transaction.Status.fromValue(txn.getStatus().value()),
                    txn.getErrorCode(), txn.getErrorMessage(), txn.getMetaData(),
                    txn.getSenderName(), txn.getCallerName(), txn.getRecipientName()
            );
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Install unrestricted caller token on your own accounts.
     *
     * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
     *                           tokens that are lost or not received after the payment instruction is installed
     * @return a 64-character alphanumeric string that represents the installed payment instruction
     * @throws FPSException wraps checked exceptions
     */
    public String installUnrestrictedCallerPaymentInstruction(String callerReference)
            throws FPSException {
        return installPaymentInstruction("MyRole == 'Caller' orSay 'Role does not match';",
                callerReference, callerReference, TokenType.UNRESTRICTED, callerReference);
    }

    /**
     * Install unrestricted recipient token on your own accounts.
     *
     * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
     *                           tokens that are lost or not received after the payment instruction is installed
     * @return a 64-character alphanumeric string that represents the installed payment instruction
     * @throws FPSException wraps checked exceptions
     */
    public String installUnrestrictedRecipientPaymentInstruction(String callerReference)
            throws FPSException {
        return installPaymentInstruction("MyRole == 'Recipient' orSay 'Role does not match';",
                callerReference, callerReference, TokenType.UNRESTRICTED, callerReference);
    }

    /**
     * Install tokens (payment instructions) on your own accounts.
     *
     * @param paymentInstruction set of rules in the GateKeeper language format to be installed on the caller's account
     * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
     *                           tokens that are lost or not received after the payment instruction is installed
     * @param type               the type of token
     * @return a 64-character alphanumeric string that represents the installed payment instruction
     * @throws FPSException wraps checked exceptions
     */
    public String installPaymentInstruction(String paymentInstruction, String callerReference, TokenType type)
            throws FPSException {
        return installPaymentInstruction(paymentInstruction, null, callerReference, type, null);
    }

    /**
     * Install tokens (payment instructions) on your own accounts.
     *
     * @param paymentInstruction set of rules in the GateKeeper language format to be installed on the caller's account
     * @param tokenFriendlyName  a human-friendly, readable name for the payment instruction
     * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
     *                           tokens that are lost or not received after the payment instruction is installed
     * @param type               the type of token
     * @param comment             the reason for making the payment
     * @return a 64-character alphanumeric string that represents the installed payment instruction
     * @throws FPSException wraps checked exceptions
     */
    public String installPaymentInstruction(String paymentInstruction, String tokenFriendlyName, String callerReference,
                                            TokenType type, String comment) throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("PaymentInstruction", paymentInstruction);
        if (tokenFriendlyName != null)
            params.put("TokenFriendlyName", tokenFriendlyName);
        params.put("CallerReference", callerReference);
        params.put("TokenType", type.value());
        if (comment != null)
            params.put("PaymentReason", comment);
        GetMethod method = new GetMethod();
        try {
            InstallPaymentInstructionResponse response =
                    makeRequestInt(method, "InstallPaymentInstruction", params, InstallPaymentInstructionResponse.class);
            return response.getTokenId();
        } finally {
            method.releaseConnection();
        }
    }

    public Transaction pay(String senderToken, double amount, String callerReference)
            throws FPSException {
        return pay(recipientToken, senderToken, callerToken, amount, new Date(), ChargeFeeTo.RECIPIENT, callerReference,
                null, null, null, null, null, null, 0, 0, descriptorPolicy);
    }

    public Transaction pay(String senderToken, double amount, String callerReference, DescriptorPolicy descriptorPolicy)
            throws FPSException {
        return pay(recipientToken, senderToken, callerToken, amount, new Date(), ChargeFeeTo.RECIPIENT, callerReference,
                null, null, null, null, null, null, 0, 0, descriptorPolicy);
    }
    
    public Transaction pay(String recipientToken, String senderToken, String callerToken, double amount,
                           Date transactionDate, ChargeFeeTo chargeFeeTo,
                           String callerReference, String senderReference, String recipientReference,
                           String senderDescription, String recipientDescription, String callerDescription,
                           String metadata, double marketplaceFixedFee, int marketplaceVariableFee,
                           DescriptorPolicy descriptorPolicy)
            throws FPSException {
        if (recipientToken == null)
            throw new IllegalArgumentException("Recipient Token ID can't be null");
        if (senderToken == null)
            throw new IllegalArgumentException("Sender Token ID can't be null");
        if (logger.isInfoEnabled())
            logger.info("Payment: " + senderToken + " paying " + recipientToken + " for " + amount);
        Map<String, String> params = new HashMap<String, String>();
        if (recipientToken != null)
            params.put("RecipientTokenId", recipientToken);
        params.put("SenderTokenId", senderToken);
        params.put("CallerTokenId", callerToken);
        params.put("TransactionAmount.Amount", Double.toString(amount));
        params.put("TransactionAmount.CurrencyCode", "USD");
        if (transactionDate != null)
            params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
        params.put("ChargeFeeTo", chargeFeeTo.value());
        params.put("CallerReference", callerReference);
        if (senderReference != null)
            params.put("SenderReference", senderReference);
        if (recipientReference != null)
            params.put("RecipientReference", recipientReference);
        if (senderDescription != null)
            params.put("SenderDescription", senderDescription);
        if (recipientDescription != null)
            params.put("RecipientDescription", recipientDescription);
        if (callerDescription != null)
            params.put("CallerDescription", callerDescription);
        if (metadata != null)
            params.put("MetaData", metadata);
        if (marketplaceFixedFee != 0)
            params.put("MarketplaceFixedFee", Double.toString(marketplaceFixedFee));
        if (marketplaceVariableFee != 0)
            params.put("MarketplaceVariableFee", Integer.toString(marketplaceVariableFee));
        if (descriptorPolicy != null) {
            params.put("SoftDescriptorType", descriptorPolicy.getSoftDescriptorType().value());
            params.put("CSNumberOf", descriptorPolicy.getCSNumberOf().value());
        }
        GetMethod method = new GetMethod();
        try {
            PayResponse response =
                    makeRequestInt(method, "Pay", params, PayResponse.class);
            TransactionResponse transactionResponse = response.getTransactionResponse();
            return new Transaction(
                    transactionResponse.getTransactionId(),
                    Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                    transactionResponse.getStatusDetail()
                    // todo: transactionResponse.getNewSenderTokenUsage()
            );
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Refund a successfully completed payment transaction.
     * @param senderToken token of the original recipient who is now the sender in the refund
     * @param transactionID the transaction that is to be refunded
     * @param callerReference a unique reference that identifies this refund
     * @return the refund transaction
     * @throws FPSException FPSException wraps checked exceptions
     */
    public Transaction refund(String senderToken, String transactionID, String callerReference) throws FPSException {
        return refund(callerToken, senderToken, transactionID, null, ChargeFeeTo.RECIPIENT, new Date(),
                callerReference, null, null, null, null, null, null, null);
    }

    /**
     * Refund a successfully completed payment transaction.
     * @param callerToken the caller token
     * @param senderToken token of the original recipient who is now the sender in the refund
     * @param transactionID the transaction that is to be refunded<br/>
     * @param refundAmount the amount to be refunded<br/>
     *                     If this value is not specified, then the remaining funds from the original transaction
     *                     is refunded.
     * @param chargeFeeTo the participant who pays the fee<br/>
     *                    Currently Amazon FPS does not charge any fee for the refund and this has no impact on
     *                    the transaction
     * @param transactionDate the date of the transaction from the caller
     * @param callerReference a unique reference that identifies this refund
     * @param senderReference the reference created by the recipient of original transaction for this refund transaction
     * @param recipientReference the reference created by the Sender (of the original transaction) for this refund transaction
     * @param senderDescription a 128-byte field to store transaction description
     * @param recipientDescription a 128-byte field to store transaction description
     * @param callerDescription a 128-byte field to store transaction description
     * @param metadata a 2KB free form field used to store transaction data
     * @param policy the refund choice: refund the master transaction, the marketplace fee, or both
     * @return the refund transaction
     * @throws FPSException FPSException wraps checked exceptions
     */
    public Transaction refund(String callerToken, String senderToken, String transactionID, Amount refundAmount,
                       ChargeFeeTo chargeFeeTo, Date transactionDate,
                       String callerReference, String senderReference, String recipientReference,
                       String senderDescription, String recipientDescription, String callerDescription,
                       String metadata, MarketplaceRefundPolicy policy) throws FPSException {
        if (callerToken == null)
            throw new IllegalArgumentException("Caller Token ID can't be null");
        if (logger.isInfoEnabled())
            logger.info("Refund: " + senderToken + " refunding transaction " + transactionID + " for " + refundAmount);
        Map<String, String> params = new HashMap<String, String>();
        params.put("CallerTokenId", callerToken);
        params.put("RefundSenderTokenId", senderToken);
        params.put("TransactionId", transactionID);
        if (refundAmount != null) {
            params.put("RefundAmount.Amount", refundAmount.getAmount().toString());
            params.put("RefundAmount.CurrencyCode", refundAmount.getCurrencyCode());
        }
        params.put("ChargeFeeTo", chargeFeeTo.value());
        if (transactionDate != null)
            params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
        params.put("CallerReference", callerReference);
        if (senderReference != null)
            params.put("RefundSenderReference", senderReference);
        if (recipientReference != null)
            params.put("RefundRecipientReference", recipientReference);
        if (senderDescription != null)
            params.put("RefundSenderDescription", senderDescription);
        if (recipientDescription != null)
            params.put("RefundRecipientDescription", recipientDescription);
        if (callerDescription != null)
            params.put("CallerDescription", callerDescription);
        if (metadata != null)
            params.put("MetaData", metadata);
        if (policy != null)
            params.put("MarketplaceRefundPolicy", policy.value());
        GetMethod method = new GetMethod();
        try {
            RefundResponse response =
                    makeRequestInt(method, "Refund", params, RefundResponse.class);
            TransactionResponse transactionResponse = response.getTransactionResponse();
            return new Transaction(
                    transactionResponse.getTransactionId(),
                    Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                    transactionResponse.getStatusDetail()
                    // todo: transactionResponse.getNewSenderTokenUsage()
            );
        } finally {
            method.releaseConnection();
        }
    }

    /** TODO: reserve
     public void reserve() throws FPSException {

     }
     **/

    /** TODO: retryTransaction
     public void retryTransaction() throws FPSException {

     }
     **/

    /** TODO: settle
     public void settle() throws FPSException {

     }
     **/

    /**
     * The SettleDebt operation takes the settlement amount, credit instrument, and the settlement token among other
     * parameters. Using this operation you can:
     * <ul>
     * <li>
     * Transfer money from sender's payment instrument specified in the settlement token to the recipient's
     * account balance. The fee charged is deducted from the settlement amount and deposited into recipient's
     * account balance.
     * </li>
     * <li>
     * Decrement debt balances by the settlement amount.
     * </li>
     * </ul>
     * @param settlementToken the token ID of the settlement token
     * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
     * @param amount the amount for the settlement
     * @param callerReference a unique reference that you specify in your system to identify a transaction
     * @return the transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction settleDebt(String settlementToken, String creditInstrument, double amount,
                                  String callerReference)
            throws FPSException {
        return settleDebt(settlementToken, callerToken, creditInstrument, amount, new Date(), null, null, callerReference,
                ChargeFeeTo.RECIPIENT, null, null, null, null, descriptorPolicy);
    }

    /**
     * The SettleDebt operation takes the settlement amount, credit instrument, and the settlement token among other
     * parameters. Using this operation you can:
     * <ul>
     * <li>
     * Transfer money from sender's payment instrument specified in the settlement token to the recipient's
     * account balance. The fee charged is deducted from the settlement amount and deposited into recipient's
     * account balance.
     * </li>
     * <li>
     * Decrement debt balances by the settlement amount.
     * </li>
     * </ul>
     * @param settlementToken the token ID of the settlement token
     * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
     * @param amount the amount for the settlement
     * @param callerReference a unique reference that you specify in your system to identify a transaction
     * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
     * @return the transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction settleDebt(String settlementToken, String creditInstrument, double amount,
                                  String callerReference, DescriptorPolicy descriptorPolicy)
            throws FPSException {
        return settleDebt(settlementToken, callerToken, creditInstrument, amount, new Date(), null, null, callerReference,
                ChargeFeeTo.RECIPIENT, null, null, null, null, descriptorPolicy);
    }

    /**
     * The SettleDebt operation takes the settlement amount, credit instrument, and the settlement token among other
     * parameters. Using this operation you can:
     * <ul>
     * <li>
     * Transfer money from sender's payment instrument specified in the settlement token to the recipient's
     * account balance. The fee charged is deducted from the settlement amount and deposited into recipient's
     * account balance.
     * </li>
     * <li>
     * Decrement debt balances by the settlement amount.
     * </li>
     * </ul>
     * @param settlementToken the token ID of the settlement token
     * @param callerToken the callers token
     * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
     * @param amount the amount for the settlement
     * @param transactionDate the date of the callers transaction
     * @param senderReference the unique value that will be used as a reference for the sender in this transaction
     * @param recipientReference the unique value that will be used as a reference for the recipient in this transaction
     * @param callerReference a unique reference that you specify in your system to identify a transaction
     * @param chargeFeeTo the participant paying the fee for the transaction
     * @param senderDescription a 128-byte field to store transaction description
     * @param recipientDescription a 128-byte field to store transaction description
     * @param callerDescription a 128-byte field to store transaction description
     * @param metadata a 2KB free form field used to store transaction data
     * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
     * @return the transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction settleDebt(String settlementToken, String callerToken,
                       String creditInstrument, double amount,
                       Date transactionDate, String senderReference, String recipientReference, String callerReference,
                       ChargeFeeTo chargeFeeTo,
                       String senderDescription, String recipientDescription, String callerDescription,
                       String metadata, DescriptorPolicy descriptorPolicy)
            throws FPSException {
        if (settlementToken == null)
            throw new IllegalArgumentException("Sender Token ID can't be null");
        if (callerToken == null)
            throw new IllegalArgumentException("Caller Token ID can't be null");
        Map<String, String> params = new HashMap<String, String>();
        params.put("SenderTokenId", settlementToken);
        params.put("CallerTokenId", callerToken);
        params.put("CreditInstrumentId", creditInstrument);
        params.put("SettlementAmount.Amount", Double.toString(amount));
        params.put("SettlementAmount.CurrencyCode", "USD");
        if (transactionDate != null)
            params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
        if (senderReference != null)
            params.put("SenderReference", senderReference);
        if (recipientReference != null)
            params.put("RecipientReference", recipientReference);
        params.put("CallerReference", callerReference);
        params.put("ChargeFeeTo", chargeFeeTo.value());
        if (senderDescription != null)
            params.put("SenderDescription", senderDescription);
        if (recipientDescription != null)
            params.put("RecipientDescription", recipientDescription);
        if (callerDescription != null)
            params.put("CallerDescription", callerDescription);
        if (metadata != null)
            params.put("MetaData", metadata);
        if (descriptorPolicy != null) {
            params.put("SoftDescriptorType", descriptorPolicy.getSoftDescriptorType().value());
            params.put("CSNumberOf", descriptorPolicy.getCSNumberOf().value());
        }
        GetMethod method = new GetMethod();
        try {
            SettleDebtResponse response =
                    makeRequestInt(method, "SettleDebt", params, SettleDebtResponse.class);
            TransactionResponse transactionResponse = response.getTransactionResponse();
            return new Transaction(
                    transactionResponse.getTransactionId(),
                    Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                    transactionResponse.getStatusDetail()
                    // todo: transactionResponse.getNewSenderTokenUsage()
            );
        } finally {
            method.releaseConnection();
        }
    }

    /** TODO: subscribeForCallerNotification
     public void subscribeForCallerNotification() throws FPSException {

     }
     **/

    /** TODO: unsubscribeForCallerNotification
     public void unsubscribeForCallerNotification() throws FPSException {

     }
     **/

    /**
     * Write off the debt accumulated by the recipient on any credit instrument
     * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
     * @param adjustmentAmount the amount for the settlement<br/>
     *                         if the <tt>adjustmentAmount</tt> is not a positive value,
     *                         a {@link IllegalArgumentException} is thrown
     * @param callerReference a unique reference that you specify in your system to identify a transaction
     * @return the transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction writeOffDebt(String creditInstrument, double adjustmentAmount, String callerReference)
            throws FPSException {
        return writeOffDebt(callerToken, creditInstrument, adjustmentAmount, new Date(),
                callerReference, null, null, null, null, null, null);
    }

    /**
     * Write off the debt accumulated by the recipient on any credit instrument
     * @param callerToken the callers token
     * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
     * @param adjustmentAmount the amount for the settlement<br/>
     *                         if the <tt>adjustmentAmount</tt> is not a positive value,
     *                         a {@link IllegalArgumentException} is thrown
     * @param transactionDate the date of the callers transaction
     * @param senderReference the unique value that will be used as a reference for the sender in this transaction
     * @param recipientReference the unique value that will be used as a reference for the recipient in this transaction
     * @param callerReference a unique reference that you specify in your system to identify a transaction
     * @param senderDescription a 128-byte field to store transaction description
     * @param recipientDescription a 128-byte field to store transaction description
     * @param callerDescription a 128-byte field to store transaction description
     * @param metadata a 2KB free form field used to store transaction data
     * @return the transaction
     * @throws FPSException wraps checked exceptions
     */
    public Transaction writeOffDebt(String callerToken, String creditInstrument, double adjustmentAmount,
                                    Date transactionDate,
                                    String callerReference, String recipientReference, String senderReference,
                                    String senderDescription, String recipientDescription, String callerDescription,
                                    String metadata)
            throws FPSException {
        if (callerToken == null)
            throw new IllegalArgumentException("Caller Token ID can't be null");
        if (adjustmentAmount <= 0)
            throw new IllegalArgumentException("The adjustment amount should be a positive value");
        if (logger.isInfoEnabled())
            logger.info("Writing off debt instrument " + creditInstrument + " for an amount of " + adjustmentAmount);
        Map<String, String> params = new HashMap<String, String>();
        params.put("CallerTokenId", callerToken);
        params.put("CreditInstrumentId", creditInstrument);
        params.put("AdjustmentAmount.Amount", Double.toString(adjustmentAmount));
        params.put("AdjustmentAmount.CurrencyCode", "USD");
        if (transactionDate != null)
            params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
        if (senderReference != null)
            params.put("SenderReference", senderReference);
        if (recipientReference != null)
            params.put("RecipientReference", recipientReference);
        params.put("CallerReference", callerReference);
        if (senderDescription != null)
            params.put("SenderDescription", senderDescription);
        if (recipientDescription != null)
            params.put("RecipientDescription", recipientDescription);
        if (callerDescription != null)
            params.put("CallerDescription", callerDescription);
        if (metadata != null)
            params.put("MetaData", metadata);
        GetMethod method = new GetMethod();
        try {
            WriteOffDebtResponse response =
                    makeRequestInt(method, "WriteOffDebt", params, WriteOffDebtResponse.class);
            TransactionResponse transactionResponse = response.getTransactionResponse();
            return new Transaction(
                    transactionResponse.getTransactionId(),
                    Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                    transactionResponse.getStatusDetail()
                    // todo: transactionResponse.getNewSenderTokenUsage()
            );
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Get the current balance on your account.
     *
     * @return the balance
     * @throws FPSException wraps checked exceptions
     */
    public AccountBalance getAccountBalance() throws FPSException {
        Map<String, String> params = new HashMap<String, String>();
        GetMethod method = new GetMethod();
        try {
            GetAccountBalanceResponse response =
                    makeRequestInt(method, "GetAccountBalance", params, GetAccountBalanceResponse.class);
            com.xerox.amazonws.typica.fps.jaxb.AccountBalance balance = response.getAccountBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount available = balance.getTotalBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount pendingIn = balance.getPendingInBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount pendingOut = balance.getPendingOutBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount disburse = balance.getAvailableBalances().getDisburseBalance();
            com.xerox.amazonws.typica.fps.jaxb.Amount refund = balance.getAvailableBalances().getRefundBalance();
            return new AccountBalance(
                    new Amount(new BigDecimal(available.getAmount()), available.getCurrencyCode().toString()),
                    new Amount(new BigDecimal(pendingIn.getAmount()), pendingIn.getCurrencyCode().toString()),
                    new Amount(new BigDecimal(pendingOut.getAmount()), pendingOut.getCurrencyCode().toString()),
                    new Amount(new BigDecimal(disburse.getAmount()), disburse.getCurrencyCode().toString()),
                    new Amount(new BigDecimal(refund.getAmount()), refund.getCurrencyCode().toString())
            );
        } finally {
            method.releaseConnection();
        }
    }

    public String acquireSingleUseToken(String callerReference, String returnURL, Amount amount, String reason)
            throws FPSException, MalformedURLException {
        return acquireSingleUseToken(callerReference, returnURL, amount, false, null, null, reason);
    }

    public String acquireSingleUseToken(String callerReference, String returnURL, Amount amount,
                                        PaymentMethod paymentMethod, String reason)
            throws FPSException, MalformedURLException {
        return acquireSingleUseToken(callerReference, returnURL, amount, false, paymentMethod, null, reason);
    }

    public String acquireSingleUseToken(String callerReference, String returnURL, Amount amount, boolean reserve,
                                        PaymentMethod paymentMethod, String recipientToken, String reason)
            throws FPSException, MalformedURLException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("callerReference", callerReference);
        parameters.put("transactionAmount", amount.getAmount().toString());
        parameters.put("currencyCode", amount.getCurrencyCode());
        if (paymentMethod != null)
            parameters.put("paymentMethod", paymentMethod.value());
        if (recipientToken != null)
            parameters.put("recipientToken", recipientToken);
        if (reason != null)
            parameters.put("paymentReason", reason);
        if (reserve)
            parameters.put("reserve", "True");
        return generateUIPipelineURL("SingleUse", returnURL, parameters);
    }

    /** TODO: acquireMultiUseToken
    public String acquireMultiUseToken(String callerReference, String returnURL, Amount amount,
                                        PaymentMethod paymentMethod, String recipientToken, String paymentReason)
            throws FPSException, MalformedURLException {

    }
    */

    public String acquireRecurringToken(String callerReference, String returnURL, Amount amount,
                                        int recurringInterval, RecurringGranularity recurringGranularity,
                                        String reason)
            throws MalformedURLException, FPSException {
        return acquireRecurringToken(callerReference, returnURL, amount, recurringInterval, recurringGranularity,
                null, null, null, null, reason);
    }

    public String acquireRecurringToken(String callerReference, String returnURL, Amount amount,
                                        int recurringInterval, RecurringGranularity recurringGranularity,
                                        Date validityStart, Date validityExpiry,
                                        PaymentMethod paymentMethod, String recipientToken, String reason)
            throws FPSException, MalformedURLException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("callerReference", callerReference);
        parameters.put("transactionAmount", amount.getAmount().toString());
        parameters.put("currencyCode", amount.getCurrencyCode());
        if (paymentMethod != null)
            parameters.put("paymentMethod", paymentMethod.value());
        if (recipientToken != null)
            parameters.put("recipientToken", recipientToken);
        if (reason != null)
            parameters.put("paymentReason", reason);
        if (validityStart != null)
            parameters.put("validityStart", DataUtils.encodeDate(validityStart));
        if (validityExpiry != null)
            parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
        String recurringPeriod = Integer.toString(recurringInterval) + " " + recurringGranularity.getValue();
        parameters.put("recurringPeriod", recurringPeriod);
        return generateUIPipelineURL("Recurring", returnURL, parameters);
    }

    /** TODO: acquireEditToken
    public String acquireEditToken(String callerReference, String returnURL, Amount amount,
                                        PaymentMethod paymentMethod, String recipientToken, String paymentReason)
            throws FPSException, MalformedURLException {

    }
    */

    /** TODO: acquireRecipientToken
    public String acquireRecipientToken(String callerReference, String returnURL, Amount amount,
                                        PaymentMethod paymentMethod, String recipientToken, String paymentReason)
            throws FPSException, MalformedURLException {

    }
    */

    /** TODO: acquirePrepaidToken
    public String acquirePrepaidToken(String callerReference, String returnURL, Amount amount,
                                        PaymentMethod paymentMethod, String recipientToken, String paymentReason)
            throws FPSException, MalformedURLException {

    }
    */

    public String acquirePostPaidToken(String callerReferenceSender, String callerReferenceSettlement,
                                       String returnURL, Amount creditLimit, Amount globalAmountLimit,
                                       String paymentReason)
            throws FPSException, MalformedURLException {
        return acquirePostPaidToken(callerReferenceSender, callerReferenceSettlement, null, null,
                returnURL, creditLimit, globalAmountLimit, null, paymentReason);
    }

    public String acquirePostPaidToken(String callerReferenceSender, String callerReferenceSettlement,
                                       String returnURL, Amount creditLimit, Amount globalAmountLimit,
                                       PaymentMethod paymentMethod, String paymentReason)
            throws FPSException, MalformedURLException {
        return acquirePostPaidToken(callerReferenceSender, callerReferenceSettlement, null, null,
                returnURL, creditLimit, globalAmountLimit, paymentMethod, paymentReason);
    }

    public String acquirePostPaidToken(String callerReferenceSender, String callerReferenceSettlement,
                                       Date validityStart, Date validityExpiry,
                                       String returnURL, Amount creditLimit, Amount globalAmountLimit,
                                       PaymentMethod paymentMethod, String paymentReason)
            throws FPSException, MalformedURLException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("callerReferenceSender", callerReferenceSender);
        parameters.put("callerReferenceSettlement", callerReferenceSettlement);
        if (validityStart != null)
            parameters.put("validityStart", DataUtils.encodeDate(validityStart));
        if (validityExpiry != null)
            parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
        parameters.put("currencyCode", creditLimit.getCurrencyCode());
        parameters.put("creditLimit", creditLimit.getAmount().toString());
        parameters.put("globalAmountLimit", globalAmountLimit.getAmount().toString());
        if (paymentMethod != null)
            parameters.put("paymentMethod", paymentMethod.value());
        if (paymentReason != null)
            parameters.put("paymentReason", paymentReason);
        return generateUIPipelineURL("SetupPostpaid", returnURL, parameters);
    }

    public String generateUIPipelineURL(String pipelineName, String returnURL, Map<String, String> params) throws MalformedURLException {
        // build the map of parameters
        SortedMap<String, String> parameters = new TreeMap<String, String>(params);
        parameters.put("callerKey", super.getAwsAccessKeyId());
        parameters.put("pipelineName", pipelineName);
        parameters.put("returnURL", returnURL);
        // build the URL
        StringBuffer url = new StringBuffer(uiPipeline);
        boolean first = true;
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            if (first) {
                url.append('?');
                first = false;
            } else {
                url.append('&');
            }
            url.append(urlencode(parameter.getKey())).append("=").append(urlencode(parameter.getValue()));
        }
        // calculate the signature
        URL rawURL = new URL(url.toString());
        StringBuilder toBeSigned = new StringBuilder(rawURL.getPath()).append('?').append(rawURL.getQuery());
        String signature = urlencode(encode(getSecretAccessKey(), toBeSigned.toString(), false));
        url.append("&awsSignature=").append(signature);
        return url.toString();
    }

    public String extractSingleUseTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, FPSException {
		// parse status message
		String status = request.getParameter("status");
		String errorMessage = request.getParameter("errorMessage");
		String requestID = request.getParameter("RequestId");
		if ("SE".equals(status))
			throw new FPSException(requestID, status, errorMessage);
		else if ("A".equals(status))
			throw new FPSException(requestID, status, errorMessage);
		else if ("CE".equals(status))
			throw new FPSException(requestID, status, errorMessage);
		else if ("PE".equals(status))
			throw new FPSException(requestID, status, errorMessage);
		else if ("NP".equals(status))
			throw new FPSException(requestID, status, errorMessage);
		else if ("NM".equals(status))
			throw new FPSException(requestID, status, errorMessage);
		System.out.println("Status: " + status);
		System.out.println("Error Message: " + errorMessage);
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
        return request.getParameter("tokenID");
    }

    /* todo: extractMultiseTokenFromCBUI
    public String extractMultiseTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, InvalidSignatureException {
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"));
        return request.getParameter("tokenID");
    }
    */

    public String extractRecurringTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, InvalidSignatureException {
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
        return request.getParameter("tokenID");
    }

    /*
    // todo: extractEditTokenFromCBUI
    public String extractEditTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, InvalidSignatureException {
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"));
        return request.getParameter("tokenID");
    }

    // todo: extractRecipientTokenFromCBUI
    public String extractRecipientTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, InvalidSignatureException {
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"));
        return request.getParameter("tokenID");
    }

    // todo: extractPrePaidTokenFromCBUI
    public String extractPrePaidTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, InvalidSignatureException {
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"));
        return request.getParameter("tokenID");
    }
    */

    public PostPaidInstrument extractPostPaidTokenFromCBUI(HttpServletRequest request)
            throws MalformedURLException, FPSException {
        // ensure first that the request is valid
        if (!isSignatureValid(request))
            throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
        Date expiry = null;
        try {
            String expiryValue = request.getParameter("expiry");
            if (expiryValue != null)
                expiry = DataUtils.decodeDate(expiryValue);
        } catch (ParseException e) {
            // do nothing -- this might happen!
        }
        return new PostPaidInstrument(
                request.getParameter("creditInstrumentID"),
                request.getParameter("creditSenderTokenID"),
                request.getParameter("settlementTokenID"),
                expiry
        );
    }

    @SuppressWarnings("unchecked")
    public boolean isSignatureValid(HttpServletRequest request) throws MalformedURLException {
        String signature = urlencode(request.getParameter("awsSignature"));
        if (signature == null)
            return false;
        List<String> parameters = new ArrayList(request.getParameterMap().keySet());
	Collator stringCollator = Collator.getInstance();
	stringCollator.setStrength(Collator.PRIMARY);
	Collections.sort(parameters, stringCollator);
        parameters.remove("awsSignature");
        // build the URL to sign in order to ensure this is a valid signature we received
        StringBuffer url = new StringBuffer(request.getRequestURL());
        boolean first = true;
        for (String parameter : parameters) {
            System.out.println("Adding parameter " + parameter + " to signature computation");
            if (first) {
                url.append('?');
                first = false;
            } else {
                url.append('&');
            }
            url.append(urlencode(parameter)).append("=").append(urlencode(request.getParameter(parameter)));
        }
        // sign the URL
        URL rawURL = new URL(url.toString());
        StringBuilder toBeSigned = new StringBuilder(rawURL.getPath()).append('?').append(rawURL.getQuery());
        String ourSignature = urlencode(encode(getSecretAccessKey(), toBeSigned.toString(), false));
	ourSignature = ourSignature.replaceAll("%2B", "+");
	System.out.println("AWS sig: " + signature);
	System.out.println("Our sig: " + ourSignature);
        return ourSignature.equals(signature);
    }

    protected <T> T makeRequestInt(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
		throws FPSException {
		try {
			T response = makeRequest(method, action, params, respType);
            Class responseClass = response.getClass();
            ResponseStatus status = (ResponseStatus) responseClass.getMethod("getStatus").invoke(response);
            if (ResponseStatus.FAILURE.equals(status)) {
                String requestID = (String) responseClass.getMethod("getRequestId").invoke(response);
                ServiceErrors rawErrors = (ServiceErrors) responseClass.getMethod("getErrors").invoke(response);
                List<FPSError> errors = new ArrayList<FPSError>(rawErrors.getErrors().size());
                for (ServiceError error : rawErrors.getErrors()) {
                    AWSError.ErrorType type = null;
                    switch (error.getErrorType()) {
                        case BUSINESS:
                            type = AWSError.ErrorType.SENDER;
                            break;
                        case SYSTEM:
                            type = AWSError.ErrorType.RECEIVER;
                            break;
                    }
                    errors.add(new FPSError(type, error.getErrorCode(), error.getReasonText(), error.isIsRetriable()));
                }
                throw new FPSException(requestID, errors);
            }
            return response;
		} catch (AWSException ex) {
			throw new FPSException(ex);
		} catch (JAXBException ex) {
			throw new FPSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new FPSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new FPSException(ex.getMessage(), ex);
		} catch (InvocationTargetException ex) {
            throw new FPSException(ex.getMessage(), ex);
        } catch (NoSuchMethodException ex) {
            throw new FPSException(ex.getMessage(), ex);
        } catch (IllegalAccessException ex) {
            throw new FPSException(ex.getMessage(), ex);
        }
    }

    static void setVersionHeader(AWSQueryConnection connection) {
        List<String> vals = new ArrayList<String>();
        vals.add("2007-01-08");
        connection.getHeaders().put("Version", vals);
    }
}
