//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007,2008 Xerox Corporation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.xerox.amazonws.devpay;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.xerox.amazonws.common.AWSQueryConnection;
import com.xerox.amazonws.typica.jaxb.ActivateDesktopProductResponse;
import com.xerox.amazonws.typica.jaxb.ActivateDesktopProductResult;
import com.xerox.amazonws.typica.jaxb.ActivateHostedProductResponse;
import com.xerox.amazonws.typica.jaxb.ActivateHostedProductResult;
import com.xerox.amazonws.typica.jaxb.GetActiveSubscriptionsByPidResponse;
import com.xerox.amazonws.typica.jaxb.GetActiveSubscriptionsByPidResult;
import com.xerox.amazonws.typica.jaxb.RefreshUserTokenResponse;
import com.xerox.amazonws.typica.jaxb.RefreshUserTokenResult;
import com.xerox.amazonws.typica.jaxb.VerifyProductSubscriptionByPidResponse;
import com.xerox.amazonws.typica.jaxb.VerifyProductSubscriptionByPidResult;
import com.xerox.amazonws.typica.jaxb.VerifyProductSubscriptionByTokensResponse;
import com.xerox.amazonws.typica.jaxb.VerifyProductSubscriptionByTokensResult;

/**
 * This class provides an interface with the Amazon DevPay LS service. It provides high level
 * methods for listing and creating and deleting domains.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class DevPayLS extends AWSQueryConnection {

    private static Log logger = LogFactory.getLog(DevPayLS.class);

	/**
	 * Initializes the devpay service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
	 */
    public DevPayLS(String awsAccessId, String awsSecretKey) {
        this(awsAccessId, awsSecretKey, true);
    }

	/**
	 * Initializes the devpay service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from LS.
	 */
    public DevPayLS(String awsAccessId, String awsSecretKey, boolean isSecure) {
        this(awsAccessId, awsSecretKey, isSecure, "ls.amazonaws.com");
    }

	/**
	 * Initializes the devpay service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from LS.
     * @param server Which host to connect to.  Usually, this will be ls.amazonaws.com
	 */
    public DevPayLS(String awsAccessId, String awsSecretKey, boolean isSecure,
                             String server)
    {
        this(awsAccessId, awsSecretKey, isSecure, server,
             isSecure ? 443 : 80);
    }

    /**
	 * Initializes the devpay service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from LS.
     * @param server Which host to connect to.  Usually, this will be ls.amazonaws.com
     * @param port Which port to use.
     */
    public DevPayLS(String awsAccessId, String awsSecretKey, boolean isSecure,
                             String server, int port)
    {
		super(awsAccessId, awsSecretKey, isSecure, server, port);
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
	 * Activates a desktop product.
	 *
	 * @param activationKey key obtained from the customer
	 * @param productToken token for your product
	 * @return the product info
	 * @throws DevPayException wraps checked exceptions
	 */
	public DesktopProductInfo activateDesktopProduct(String activationKey, String productToken) throws DevPayException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ActivationKey", activationKey);
		params.put("ProductToken", productToken);
		GetMethod method = new GetMethod();
		try {
			ActivateDesktopProductResponse response =
						makeRequest(method, "ActivateDesktopProduct", params, ActivateDesktopProductResponse.class);

			ActivateDesktopProductResult result = response.getActivateDesktopProductResult();
			return new DesktopProductInfo(result.getAWSAccessKeyId(), result.getSecretAccessKey(), result.getUserToken());
		} catch (JAXBException ex) {
			throw new DevPayException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Activates a hosted product.
	 *
	 * @param activationKey key obtained from the customer
	 * @param productToken token for your product
	 * @return the product info
	 * @throws DevPayException wraps checked exceptions
	 */
	public HostedProductInfo activateHostedProduct(String activationKey, String productToken) throws DevPayException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ActivationKey", activationKey);
		params.put("ProductToken", productToken);
		GetMethod method = new GetMethod();
		try {
			ActivateHostedProductResponse response =
						makeRequest(method, "ActivateHostedProduct", params, ActivateHostedProductResponse.class);

			ActivateHostedProductResult result = response.getActivateHostedProductResult();
			return new HostedProductInfo(result.getPersistentIdentifier(), result.getUserToken());
		} catch (JAXBException ex) {
			throw new DevPayException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Gets list of active subscriptions by persistent identifier
	 *
	 * @param persistentIdentifier customers's PID
	 * @return true if product is subscribed
	 * @throws DevPayException wraps checked exceptions
	 */
	public List<String> getActiveSubscriptionsByPid(String persistentIdentifier) throws DevPayException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("PersistentIdentifier", persistentIdentifier);
		GetMethod method = new GetMethod();
		try {
			GetActiveSubscriptionsByPidResponse response =
						makeRequest(method, "GetActiveSubscriptionsByPid", params, GetActiveSubscriptionsByPidResponse.class);

			GetActiveSubscriptionsByPidResult result = response.getGetActiveSubscriptionsByPidResult();
			return result.getProductCodes();
		} catch (JAXBException ex) {
			throw new DevPayException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Verifies that a specified product is subscribed to by a customer.
	 *
	 * @param persistentIdentifier customers's PID
	 * @param productCode the product code
	 * @return true if product is subscribed
	 * @throws DevPayException wraps checked exceptions
	 */
	public boolean isProductSubscribedByPid(String persistentIdentifier, String productCode) throws DevPayException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("PersistentIdentifier", persistentIdentifier);
		params.put("ProductCode", productCode);
		GetMethod method = new GetMethod();
		try {
			VerifyProductSubscriptionByPidResponse response =
						makeRequest(method, "VerifyProductSubscriptionByPid", params, VerifyProductSubscriptionByPidResponse.class);

			VerifyProductSubscriptionByPidResult result = response.getVerifyProductSubscriptionByPidResult();
			return result.isSubscribed();
		} catch (JAXBException ex) {
			throw new DevPayException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Verifies that a specified product is subscribed to by a customer.
	 *
	 * @param productToken the product token
	 * @param userToken the user token
	 * @return the list of product codes 
	 * @throws DevPayException wraps checked exceptions
	 */
	public boolean isProductSubscribedByTokens(String productToken, String userToken) throws DevPayException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ProductToken", productToken);
		params.put("UserToken", userToken);
		GetMethod method = new GetMethod();
		try {
			VerifyProductSubscriptionByTokensResponse response =
						makeRequest(method, "VerifyProductSubscriptionByTokens", params, VerifyProductSubscriptionByTokensResponse.class);

			VerifyProductSubscriptionByTokensResult result = response.getVerifyProductSubscriptionByTokensResult();
			return result.isSubscribed();
		} catch (JAXBException ex) {
			throw new DevPayException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Gets the most up-to-date version of the user token.
	 *
	 * @param userToken the user token
	 * @param additionalTokens optional token (see dev guide), null if not used
	 * @return the list of product codes 
	 * @throws DevPayException wraps checked exceptions
	 */
	public String refreshUserToken(String userToken, String additionalTokens) throws DevPayException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("UserToken", userToken);
		if (additionalTokens != null) {
			params.put("AdditionalTokens", additionalTokens);
		}
		GetMethod method = new GetMethod();
		try {
			RefreshUserTokenResponse response =
						makeRequest(method, "RefreshUserToken", params, RefreshUserTokenResponse.class);

			RefreshUserTokenResult result = response.getRefreshUserTokenResult();
			return result.getUserToken();
		} catch (JAXBException ex) {
			throw new DevPayException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new DevPayException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	static void setVersionHeader(AWSQueryConnection connection) {
		ArrayList vals = new ArrayList();
		vals.add("2008-04-28");
		connection.getHeaders().put("Version", vals);
	}
}
