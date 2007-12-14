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

package com.xerox.amazonws.sdb;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.xerox.amazonws.typica.sdb.jaxb.QueryResponse;
import com.xerox.amazonws.typica.sdb.jaxb.DeleteAttributesResponse;

/**
 * This class provides an interface with the Amazon SDB service. It provides methods for
 * listing and deleting items.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class Domain extends SimpleDB {

    private static Log logger = LogFactory.getLog(Domain.class);

	private String domainName;

    protected Domain(String domainName, String awsAccessKeyId,
							String awsSecretAccessKey, boolean isSecure,
							String server) throws SDBException {
        super(awsAccessKeyId, awsSecretAccessKey, isSecure, server);
		this.domainName = domainName;
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
	 * Method for getting an Item object without getting a list of them.
	 *
	 * @param identifier id of the item
     * @return the object representing the item
	 * @throws SDBException wraps checked exceptions
	 */
	public Item getItem(String identifier) throws SDBException {
		return new Item(identifier, domainName, getAwsAccessKeyId(), getSecretAccessKey(),
										isSecure(), getServer());
	}

	/**
	 * Gets a list of all items in this domain
	 *
     * @return the object containing the items, a more token, etc.
	 * @throws SDBException wraps checked exceptions
	 */
	public QueryResult listItems() throws SDBException {
		return listItems(null);
	}

	/**
	 * Gets a list of items in this domain filtered by the query string.
	 *
	 * @param queryString the filter statement
     * @return the object containing the items, a more token, etc.
	 * @throws SDBException wraps checked exceptions
	 */
	public QueryResult listItems(String queryString) throws SDBException {
		return listItems(queryString, null);
	}

	/**
	 * Gets a list of items in this domain filtered by the query string.
	 *
	 * @param queryString the filter statement
	 * @param nextToken the token used to return more items in the query result set
     * @return the object containing the items, a more token, etc.
	 * @throws SDBException wraps checked exceptions
	 */
	public QueryResult listItems(String queryString, String nextToken) throws SDBException {
		return listItems(queryString, null, 0);
	}

	/**
	 * Gets a list of items in this domain filtered by the query string.
	 *
	 * @param queryString the filter statement
	 * @param nextToken the token used to return more items in the query result set
	 * @param maxResults a limit to the number of results to return now
     * @return the object containing the items, a more token, etc.
	 * @throws SDBException wraps checked exceptions
	 */
	public QueryResult listItems(String queryString, String nextToken, int maxResults) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", domainName);
		params.put("QueryExpression", (queryString==null)?"":queryString);
		if (nextToken != null) {
			params.put("NextToken", nextToken);
		}
		if (maxResults > 0) {
			params.put("MaxResults", ""+maxResults);
		}
		GetMethod method = new GetMethod();
		try {
			QueryResponse response =
						makeRequest(method, "Query", params, QueryResponse.class);
			return new QueryResult(response.getQueryResult().getNextToken(),
					Item.createList(response.getQueryResult().getItemNames().toArray(new String[] {}), domainName,
								getAwsAccessKeyId(), getSecretAccessKey(),
								isSecure(), getServer()));
		} catch (JAXBException ex) {
			throw new SDBException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SDBException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SDBException(ex.getMessage(), ex);
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
	public void deleteItem(String identifier) throws SDBException {
		deleteAttributes(identifier, null);
	}

	protected void deleteAttributes(String identifier, List<ItemAttribute> attrs) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", domainName);
		params.put("ItemName", identifier);
		if (attrs != null) {
			int i=1;
			for (ItemAttribute attr : attrs) {
				params.put("Attribute."+i+".Name", attr.getName());
				String value = attr.getValue();
				if (value != null) {
					params.put("Attribute."+i+".Value", value);
				}
				i++;
			}
		}
		GetMethod method = new GetMethod();
		try {
			DeleteAttributesResponse response =
						makeRequest(method, "DeleteAttributes", params, DeleteAttributesResponse.class);
		} catch (JAXBException ex) {
			throw new SDBException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SDBException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SDBException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	static List<Domain> createList(String [] domainNames, String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server) throws SDBException {
		ArrayList<Domain> ret = new ArrayList<Domain>();
		for (int i=0; i<domainNames.length; i++) {
			ret.add(new Domain(domainNames[i], awsAccessKeyId, awsSecretAccessKey, isSecure, server));
		}
		return ret;
	}
}
