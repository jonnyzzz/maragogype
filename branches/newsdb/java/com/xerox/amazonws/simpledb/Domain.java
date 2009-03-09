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

package com.xerox.amazonws.simpledb;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

import com.xerox.amazonws.common.AWSException;
import com.xerox.amazonws.common.AWSQueryConnection;
import com.xerox.amazonws.typica.sdb.jaxb.Attribute;
import com.xerox.amazonws.typica.sdb.jaxb.DeleteAttributesResponse;
import com.xerox.amazonws.typica.sdb.jaxb.DomainMetadataResponse;
import com.xerox.amazonws.typica.sdb.jaxb.GetAttributesResponse;
import com.xerox.amazonws.typica.sdb.jaxb.PutAttributesResponse;
import com.xerox.amazonws.typica.sdb.jaxb.SelectResponse;

/**
 * This class provides an interface with the Amazon SDB service. It provides methods for
 * listing and deleting items.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class Domain extends AWSQueryConnection {

    private static Log logger = LogFactory.getLog(Domain.class);

	private String domainName;

    protected Domain(String domainName, String awsAccessId,
							String awsSecretKey, boolean isSecure,
							String server) throws SDBException {
        super(awsAccessId, awsSecretKey, isSecure, server, isSecure ? 443 : 80);
		this.domainName = domainName;
		SimpleDB.setVersionHeader(this);
    }

	/**
	 * Gets the name of the domain represented by this object.
	 *
     * @return the name of the domain
	 */
	public String getName() {
		return domainName;
	}

	/**
	 * Returns information about the domain.
	 *
     * @return the object containing metadata about this domain
	 * @throws SDBException wraps checked exceptions
	 */
	public DomainMetadataResult getMetadata() throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", domainName);
		GetMethod method = new GetMethod();
		try {
			DomainMetadataResponse response =
						makeRequestInt(method, "DomainMetadata", params, DomainMetadataResponse.class);
			return new DomainMetadataResult(response.getResponseMetadata().getRequestId(),
						response.getResponseMetadata().getBoxUsage(),
						Integer.parseInt(response.getDomainMetadataResult().getItemCount()),
						Integer.parseInt(response.getDomainMetadataResult().getAttributeValueCount()),
						Integer.parseInt(response.getDomainMetadataResult().getAttributeNameCount()),
						Long.parseLong(response.getDomainMetadataResult().getItemNamesSizeBytes()),
						Long.parseLong(response.getDomainMetadataResult().getAttributeValuesSizeBytes()),
						Long.parseLong(response.getDomainMetadataResult().getAttributeNamesSizeBytes()),
						new Date(Long.parseLong(response.getDomainMetadataResult().getTimestamp())*1000));
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Adds an item.
	 *
	 * @param item the item to add to this domain
	 * @throws SDBException wraps checked exceptions
	 */
	public SDBResult addItem(Item item) throws SDBException {
		return addItem(item.getIdentifier(), item.getAttributes());
	}

	/**
	 * Adds an item. This method also works to add attributes to an existing item.
	 *
	 * @param identifier the name of the item to be added
	 * @param attributes the attributes to associate with this item
	 * @throws SDBException wraps checked exceptions
	 */
	public SDBResult addItem(String identifier, Map<String, String> attributes) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", domainName);
		params.put("ItemName", identifier);
		int i=1;
		for (String key : attributes.keySet()) {
			String val = attributes.get(key);
			if (val != null) {
				params.put("Attribute."+i+".Name", key);
				params.put("Attribute."+i+".Value", val);
//				if (attr.isReplace()) {
//					params.put("Attribute."+i+".Replace", "true");
//				}
				i++;
			}
		}
		GetMethod method = new GetMethod();
		try {
			PutAttributesResponse response =
				makeRequestInt(method, "PutAttributes", params, PutAttributesResponse.class);
			return new SDBResult(null, 
						response.getResponseMetadata().getRequestId(),
						response.getResponseMetadata().getBoxUsage());
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Deletes an item.
	 *
	 * @param identifier the name of the item to be deleted
	 * @throws SDBException wraps checked exceptions
	 */
	public SDBResult deleteItem(String identifier) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", domainName);
		params.put("ItemName", identifier);
		GetMethod method = new GetMethod();
		try {
			DeleteAttributesResponse response =
				makeRequestInt(method, "DeleteAttributes", params, DeleteAttributesResponse.class);
			return new SDBResult(null, 
						response.getResponseMetadata().getRequestId(),
						response.getResponseMetadata().getBoxUsage());
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Returns an named item.
	 *
	 * @param identifier the name of the item to be deleted
	 * @throws SDBException wraps checked exceptions
	 */
	public Item getItem(String identifier) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", domainName);
		params.put("ItemName", identifier);
		GetMethod method = new GetMethod();
		try {
			GetAttributesResponse response =
						makeRequestInt(method, "GetAttributes", params, GetAttributesResponse.class);
			Item newItem = new Item(identifier);
			for (Attribute a : response.getGetAttributesResult().getAttributes()) {
				String name = a.getName().getValue();
				String encoding = a.getName().getEncoding();
				if (encoding != null && encoding.equals("base64")) {
					name = new String(Base64.decodeBase64(name.getBytes()));
				}
				String value = a.getValue().getValue();
				encoding = a.getValue().getEncoding();
				if (encoding != null && encoding.equals("base64")) {
					value = new String(Base64.decodeBase64(value.getBytes()));
				}
				newItem.getAttributes().put(name, value);
			}
			return newItem;
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Performs a query against this domain. A set of items is returned which may not be
	 * complete. If the nextToken in the result is set, this method can be called again
	 * with the same query and the supplied nextToken.
	 *
	 * @param selectExpression the query to be run
	 * @param nextToken an optional token to get more results
	 * @throws SDBException wraps checked exceptions
	 */
	public SelectResult selectItems(String selectExpression, String nextToken) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("SelectExpression", selectExpression);
		if (nextToken != null) {
			params.put("NextToken", nextToken);
		}
		GetMethod method = new GetMethod();
		try {
			SelectResponse response =
						makeRequestInt(method, "Select", params, SelectResponse.class);
			List<Item> results = new ArrayList<Item>();
			for (com.xerox.amazonws.typica.sdb.jaxb.Item i : response.getSelectResult().getItems()) {
				String iName = i.getName().getValue();
				String encoding = i.getName().getEncoding();
				if (encoding != null && encoding.equals("base64")) {
					iName = new String(Base64.decodeBase64(iName.getBytes()));
				}
				Item newItem = new Item(iName);
				for (Attribute a : i.getAttributes()) {
					String name = a.getName().getValue();
					encoding = a.getName().getEncoding();
					if (encoding != null && encoding.equals("base64")) {
						name = new String(Base64.decodeBase64(name.getBytes()));
					}
					String value = a.getValue().getValue();
					encoding = a.getValue().getEncoding();
					if (encoding != null && encoding.equals("base64")) {
						value = new String(Base64.decodeBase64(value.getBytes()));
					}
					newItem.getAttributes().put(name, value);
				}
				results.add(newItem);
			}

			return new SelectResult(
						response.getSelectResult().getNextToken(),
						response.getResponseMetadata().getRequestId(),
						response.getResponseMetadata().getBoxUsage(),
						results);
		} finally {
			method.releaseConnection();
		}
	}

	static List<Domain> createList(String [] domainNames, String awsAccessKeyId,
									String awsSecretAccessKey, boolean isSecure,
									String server, int signatureVersion, HttpClient hc)
			throws SDBException {
		ArrayList<Domain> ret = new ArrayList<Domain>();
		for (int i=0; i<domainNames.length; i++) {
			Domain dom = new Domain(domainNames[i], awsAccessKeyId, awsSecretAccessKey, isSecure, server);
			dom.setSignatureVersion(signatureVersion);
			dom.setHttpClient(hc);
			ret.add(dom);
		}
		return ret;
	}

	protected <T> T makeRequestInt(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
		throws SDBException {
		try {
			return makeRequest(method, action, params, respType);
		} catch (AWSException ex) {
			throw new SDBException(ex);
		} catch (JAXBException ex) {
			throw new SDBException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SDBException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SDBException(ex.getMessage(), ex);
		}
	}
}
