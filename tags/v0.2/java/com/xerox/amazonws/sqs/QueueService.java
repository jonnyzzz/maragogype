package com.xerox.amazonws.sqs;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import ch.inventec.Base64Coder;

import com.xerox.amazonws.common.JAXBuddy;
import com.xerox.amazonws.sqs.jaxb.CreateQueueResponse;
import com.xerox.amazonws.sqs.jaxb.ListQueuesResponse;
import com.xerox.amazonws.tools.LoggingConfigurator;

/**
 * This class provides an interface with the Amazon SQS service. It provides high level
 * methods for listing and creating message queues.
 *
 * Http authentication code borrowed from Amazon S3 AWSAuthConnection code
 * (see amazon copyright below).
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class QueueService {
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private boolean isSecure;
    private String server;
    private int port;
	protected Map headers;

    private static Logger logger = LoggingConfigurator.configureLogging(QueueService.class);

	/**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
	 */
    public QueueService(String awsAccessId, String awsSecretAccessKey) {
        this(awsAccessId, awsSecretAccessKey, true);
    }

	/**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
	 */
    public QueueService(String awsAccessId, String awsSecretAccessKey, boolean isSecure) {
        this(awsAccessId, awsSecretAccessKey, isSecure, "queue.amazonaws.com");
    }

	/**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
     * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
	 */
    public QueueService(String awsAccessId, String awsSecretAccessKey, boolean isSecure,
                             String server)
    {
        this(awsAccessId, awsSecretAccessKey, isSecure, server,
             isSecure ? 443 : 80);
    }

    /**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
     * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
     * @param port Which port to use.
     */
    public QueueService(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure,
                             String server, int port)
    {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.isSecure = isSecure;
        this.server = server;
        this.port = port;
		this.headers = new TreeMap();
		ArrayList vals = new ArrayList();
		vals.add("2006-04-01");
		this.headers.put("AWS-Version", vals);
    }

	/**
	 * This method provides the URL for the queue service based on initialization.
	 *
	 * @return generated queue service url
	 */
	public URL getUrl() {
		try {
			return makeURL("");
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Creates a new message queue. The queue name must be unique within the scope of the
	 * queues you own.
	 *
	 * @param queueName name of queue to be created
	 * @return object representing the message queue
	 */
    public MessageQueue getOrCreateMessageQueue(String queueName) throws SQSException {
		try {
			InputStream iStr =
					makeRequest("POST", "?QueueName="+queueName, this.headers).getInputStream();
			CreateQueueResponse response =
					JAXBuddy.deserializeXMLStream(CreateQueueResponse.class, iStr);
			return new MessageQueue(response.getQueueUrl(),
								this.awsAccessKeyId, this.awsSecretAccessKey,
								this.isSecure, this.server);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
    }

	/**
	 * Retrieves a list of message queues. A maximum of 10,000 queue URLs are returned.
	 * If a value is specified for the optional queueNamePrefix parameter, only those queues
	 * with a queue name beginning with the value specified are returned. The queue name is
	 * specified in the QueueName parameter when a queue is created.
	 *
	 * @param queueNamePrefix the optional prefix for filtering results. can be null.
	 * @return a list of objects representing the message queues defined for this account
	 */
    public List<MessageQueue> listMessageQueues(String queueNamePrefix) throws SQSException {
		try {
			InputStream iStr =
				makeRequest("GET", (queueNamePrefix!=null)?("?QueueNamePrefix="+queueNamePrefix):"",
									this.headers).getInputStream();
			ListQueuesResponse response =
					JAXBuddy.deserializeXMLStream(ListQueuesResponse.class, iStr);
			return MessageQueue.createList(response.getQueueUrl().toArray(new String[] {}),
								this.awsAccessKeyId, this.awsSecretAccessKey,
								this.isSecure, this.server);
		} catch (ArrayStoreException ex) {
			logger.error("ArrayStore problem, fetching response again to aid in debug.");
			try {
				logger.error(makeRequest("GET", (queueNamePrefix!=null)?("?QueueNamePrefix="+queueNamePrefix):"", this.headers).getResponseMessage());
			} catch (Exception e) {
				logger.error("Had trouble re-fetching the request response.", e);
			}
			throw new SQSException("ArrayStore problem, maybe SQS responded poorly?", ex);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
    }

	//  This software code is made available "AS IS" without warranties of any
	//  kind.  You may copy, display, modify and redistribute the software
	//  code either by itself or as incorporated into your code; provided that
	//  you do not remove any proprietary notices.  Your use of this software
	//  code is at your own risk and you waive any claim against Amazon
	//  Digital Services, Inc. or its affiliates with respect to your use of
	//  this software code. (c) 2006 Amazon Digital Services, Inc. or its
	//  affiliates.

    /**
     * Make a new HttpURLConnection.
     * @param method The HTTP method to use (GET, PUT, DELETE)
     * @param resource The resource name (bucketName + "/" + key).
     * @param headers A Map of String to List of Strings representing the http
     * headers to pass (can be null).
     */
    protected HttpURLConnection makeRequest(String method, String resource, Map headers)
        throws MalformedURLException, IOException
    {
        URL url = makeURL(resource);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);

        addHeaders(connection, headers);
        addAuthHeader(connection, method, resource);

        return connection;
    }

    /**
     * Add the given headers to the HttpURLConnection.
     * @param connection The HttpURLConnection to which the headers will be added.
     * @param headers A Map of String to List of Strings representing the http
     * headers to pass (can be null).
     */
    private void addHeaders(HttpURLConnection connection, Map headers) {
        addHeaders(connection, headers, "");
    }

    /**
     * Add the given metadata fields to the HttpURLConnection.
     * @param connection The HttpURLConnection to which the headers will be added.
     * @param metadata A Map of String to List of Strings representing the s3
     * metadata for this resource.
     */
    private void addMetadataHeaders(HttpURLConnection connection, Map metadata) {
        addHeaders(connection, metadata, "x-amz-meta-");
    }

    /**
     * Add the given headers to the HttpURLConnection with a prefix before the keys.
     * @param connection The HttpURLConnection to which the headers will be added.
     * @param headers A Map of String to List of Strings representing the http
     * headers to pass (can be null).
     * @param prefix The string to prepend to each key before adding it to the connection.
     */
    private void addHeaders(HttpURLConnection connection, Map headers, String prefix) {
        if (headers != null) {
            for (Iterator i = headers.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                for (Iterator j = ((List)headers.get(key)).iterator(); j.hasNext(); ) {
                    String value = (String)j.next();
                    connection.addRequestProperty(prefix + key, value);
                }
            }
        }
    }

    /**
     * Add the appropriate Authorization header to the HttpURLConnection.
     * @param connection The HttpURLConnection to which the header will be added.
     * @param method The HTTP method to use (GET, PUT, DELETE)
     * @param resource The resource name (bucketName + "/" + key).
     */
    private void addAuthHeader(HttpURLConnection connection, String method, String resource) {
        if (connection.getRequestProperty("Date") == null) {
            connection.setRequestProperty("Date", httpDate());
        }
        if (connection.getRequestProperty("Content-Type") == null) {
            connection.setRequestProperty("Content-Type", "");
        }

        String canonicalString =
            makeCanonicalString(method, resource, connection.getRequestProperties());
        String encodedCanonical = encode(this.awsSecretAccessKey, canonicalString, false);
        connection.setRequestProperty("Authorization",
                                      "AWS " + this.awsAccessKeyId + ":" + encodedCanonical);
    }

    /**
     * Create a new URL object for a given resource.
     * @param resource The resource name (bucketName + "/" + key).
     */
    private URL makeURL(String resource) throws MalformedURLException {
        String protocol = this.isSecure ? "https" : "http";
        return new URL(protocol, this.server, this.port, "/" + resource);
    }

    static String makeCanonicalString(String method, String resource, Map headers) {
        return makeCanonicalString(method, resource, headers, null);
    }

    /**
     * Calculate the canonical string.  When expires is non-null, it will be
     * used instead of the Date header.
     */
    static String makeCanonicalString(String method, String resource,
                                             Map headers, String expires)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(method + "\n");

        // Add all interesting headers to a list, then sort them.  "Interesting"
        // is defined as Content-MD5, Content-Type, Date, and x-amz-
        SortedMap interestingHeaders = new TreeMap();
        if (headers != null) {
            for (Iterator i = headers.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                if (key == null) continue;
                String lk = key.toLowerCase();

                // Ignore any headers that are not particularly interesting.
                if (lk.equals("content-type") || lk.equals("content-md5") || lk.equals("date") ||
                    lk.startsWith("x-amz-"))
                {
                    List s = (List)headers.get(key);
                    interestingHeaders.put(lk, concatenateList(s));
                }
            }
        }

        if (interestingHeaders.containsKey("x-amz-date")) {
            interestingHeaders.put("date", "");
        }

        // if the expires is non-null, use that for the date field.  this
        // trumps the x-amz-date behavior.
        if (expires != null) {
            interestingHeaders.put("date", expires);
        }

        // these headers require that we still put a new line in after them,
        // even if they don't exist.
        if (! interestingHeaders.containsKey("content-type")) {
            interestingHeaders.put("content-type", "");
        }
        if (! interestingHeaders.containsKey("content-md5")) {
            interestingHeaders.put("content-md5", "");
        }

        // Finally, add all the interesting headers (i.e.: all that startwith x-amz- ;-))
        for (Iterator i = interestingHeaders.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (key.startsWith("x-amx-")) {
                buf.append(key).append(':').append(interestingHeaders.get(key));
            } else {
                buf.append(interestingHeaders.get(key));
            }
            buf.append("\n");
        }

        // don't include the query parameters...
        int queryIndex = resource.indexOf('?');
        if (queryIndex == -1) {
            buf.append("/" + resource);
        } else {
            buf.append("/" + resource.substring(0, queryIndex));
        }

        // ...unless there is an acl or torrent parameter
        if (resource.matches(".*[&?]acl($|=|&).*")) {
            buf.append("?acl");
        } else if (resource.matches(".*[&?]torrent($|=|&).*")) {
            buf.append("?torrent");
        }

        return buf.toString();
    }

    /**
     * Calculate the HMAC/SHA1 on a string.
     * @param data Data to sign
     * @param passcode Passcode to sign it with
     * @return Signature
     * @throws NoSuchAlgorithmException If the algorithm does not exist.  Unlikely
     * @throws InvalidKeyException If the key is invalid.
     */
    private static String encode(String awsSecretAccessKey, String canonicalString,
                                boolean urlencode)
    {
        // The following HMAC/SHA1 code for the signature is taken from the
        // AWS Platform's implementation of RFC2104 (amazon.webservices.common.Signature)
        //
        // Acquire an HMAC/SHA1 from the raw key bytes.
        SecretKeySpec signingKey =
            new SecretKeySpec(awsSecretAccessKey.getBytes(), "HmacSHA1");

        // Acquire the MAC instance and initialize with the signing key.
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            throw new RuntimeException("Could not find sha1 algorithm", e);
        }
        try {
            mac.init(signingKey);
        } catch (InvalidKeyException e) {
            // also should not happen
            throw new RuntimeException("Could not initialize the MAC algorithm", e);
        }

        // Compute the HMAC on the digest, and set it.
        String b64 = new String(Base64Coder.encode(mac.doFinal(canonicalString.getBytes())));

        if (urlencode) {
            return urlencode(b64);
        } else {
            return b64;
        }
    }

    private static String urlencode(String unencoded) {
        try {
            return URLEncoder.encode(unencoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException("Could not url encode to UTF-8", e);
        }
    }

    /**
     * Concatenates a bunch of header values, seperating them with a comma.
     * @param values List of header values.
     * @return String of all headers, with commas.
     */
    private static String concatenateList(List values) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, size = values.size(); i < size; ++ i) {
            buf.append(((String)values.get(i)).replaceAll("\n", "").trim());
            if (i != (size - 1)) {
                buf.append(",");
            }
        }
        return buf.toString();
    }

    /**
     * Generate an rfc822 date for use in the Date HTTP header.
     */
    private static String httpDate() {
        final String DateFormat = "EEE, dd MMM yyyy HH:mm:ss ";
        SimpleDateFormat format = new SimpleDateFormat( DateFormat, Locale.US );
        format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return format.format( new Date() ) + "GMT";
    }
}
