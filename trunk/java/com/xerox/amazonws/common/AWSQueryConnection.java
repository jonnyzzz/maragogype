//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
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

package com.xerox.amazonws.common;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;

import com.xerox.amazonws.typica.jaxb.Response;

/**
 * This class provides an interface with the Amazon SQS service. It provides high level
 * methods for listing and creating message queues.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class AWSQueryConnection extends AWSConnection {
	// this is the number of automatic retries
	private int maxRetries = 5;
	private String userAgent = "typica";

    /**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
     * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
     * @param port Which port to use.
     */
    public AWSQueryConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure,
                             String server, int port) {
		super(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port);
    }

	/**
	 * This method returns the number of times to retry when a recoverable error occurs.
	 *
	 * @return the number of times to retry on recoverable error
	 */
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * This method sets the number of times to retry when a recoverable error occurs.
	 *
	 * @param retries the number of times to retry on recoverable error
	 */
	public void setMaxRetries(int retries) {
		maxRetries = retries;
	}

    /**
     * Make a http request and process the response. This method also performs automatic retries.
	 *
     * @param method The HTTP method to use (GET, POST, DELETE, etc)
     * @param action the name of the action for this query request
     * @param params map of request params
     * @param respTpye the class that represents the desired/expected return type
     */
	protected <T> T makeRequest(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
		throws HttpException, IOException, JAXBException {

		// add auth params, and protocol specific headers
		Map<String, String> qParams = new HashMap<String, String>(params);
		qParams.put("Action", action);
		qParams.put("AWSAccessKeyId", getAwsAccessKeyId());
		qParams.put("SignatureVersion", "1");
		qParams.put("Timestamp", httpDate());
        if (headers != null) {
            for (Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
                String key = i.next();
                for (Iterator<String> j = headers.get(key).iterator(); j.hasNext(); ) {
					qParams.put(key, j.next());
                }
            }
        }
		// sort params by key
		ArrayList<String> keys = new ArrayList<String>(qParams.keySet());
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.PRIMARY);
		Collections.sort(keys, stringCollator);

		// build param string
		StringBuilder resource = new StringBuilder();
		for (String key : keys) {
			resource.append(key);
			resource.append(qParams.get(key));
		}

		// calculate signature
        String encoded = urlencode(encode(getSecretAccessKey(), resource.toString(), false));

		// build param string, encoding values and adding request signature
		resource = new StringBuilder();
		for (String key : keys) {
			resource.append("&");
			resource.append(key);
			resource.append("=");
			resource.append(urlencode(qParams.get(key)));
		}
		resource.setCharAt(0, '?');	// set first param delimeter
		resource.append("&Signature=");
		resource.append(encoded);

		// finally, build request object
        URL url = makeURL(resource.toString());
		method.setURI(new URI(url.toString(), true));
		method.setRequestHeader(new Header("User-Agent", userAgent));
		HttpClient hc = new HttpClient();	// maybe, cache this?
		Object response = null;
		boolean done = false;
		int retries = 0;
		do {
			int responseCode = hc.executeMethod(method);
			// 100's are these are handled by httpclient
			if (responseCode < 300) {
				// 200's : parse normal response into requested object
				if (respType != null) {
					InputStream iStr = method.getResponseBodyAsStream();
					response = JAXBuddy.deserializeXMLStream(respType, iStr);
				}
				done = true;
			}
			else if (responseCode < 400) {
				// 300's : what to do?
				throw new HttpException("redirect error : "+responseCode);
			}
			else if (responseCode < 500) {
				// 400's : parse client error message
				InputStream iStr = method.getResponseBodyAsStream();
				Response resp = JAXBuddy.deserializeXMLStream(Response.class, iStr);
				throw new HttpException("Client error : "+resp.getErrors().getError().getMessage());
			}
			else if (responseCode < 600) {
				// 500's : retry...
				retries++;
				if (retries > maxRetries) {
					InputStream iStr = method.getResponseBodyAsStream();
					Response resp = JAXBuddy.deserializeXMLStream(Response.class, iStr);
					throw new HttpException("Number of retries exceeded : "+action+
											", "+resp.getErrors().getError().getMessage());
				}
				try { Thread.sleep(retries*1000); } catch (InterruptedException ex) {}
			}
		} while (!done);
		return (T)response;
	}

    /**
     * Generate an rfc822 date for use in the Date HTTP header.
     */
    private static String httpDate() {
        final String DateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        SimpleDateFormat format = new SimpleDateFormat( DateFormat, Locale.US );
        format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return format.format( new Date() );
    }
}
