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

import com.xerox.amazonws.typica.sdb.jaxb.Attribute;
import com.xerox.amazonws.typica.sdb.jaxb.DeleteAttributesResponse;
import com.xerox.amazonws.typica.sdb.jaxb.GetAttributesResponse;
import com.xerox.amazonws.typica.sdb.jaxb.PutAttributesResponse;

/**
 * This class provides an interface with the Amazon SDB service. It provides methods for
 * listing items and adding/removing attributes.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class Item extends Domain {
    private static Log logger = LogFactory.getLog(Item.class);

	private String identifier;

    protected Item(String identifier, String domainName, String awsAccessKeyId,
							String awsSecretAccessKey, boolean isSecure,
							String server) throws SDBException {
        super(domainName, awsAccessKeyId, awsSecretAccessKey, isSecure, server);
		this.identifier = identifier;
	}

	/**
	 * Gets the name of the identifier that is unique to this Item
	 *
     * @return the id
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets a map of all attributes for this item
	 *
     * @return the map of attributes
	 * @throws SDBException wraps checked exceptions
	 */
	public List<ItemAttribute> getAttributes() throws SDBException {
		return getAttributes(null);
	}

	/**
	 * Gets attributes of a given name. The parameter limits the results to those of
	 * the name given.
	 *
	 * @param attributeName a name that limits the results
     * @return the list of attributes
	 * @throws SDBException wraps checked exceptions
	 */
	public List<ItemAttribute> getAttributes(String attributeName) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", getName());
		params.put("ItemName", identifier);
		if (attributeName != null) {
			params.put("AttributeName", attributeName);
		}
		GetMethod method = new GetMethod();
		try {
			GetAttributesResponse response =
						makeRequest(method, "GetAttributes", params, GetAttributesResponse.class);
			List<ItemAttribute> ret = new ArrayList<ItemAttribute>();
			List<Attribute> attrs = response.getGetAttributesResult().getAttributes();
			for (Attribute attr : attrs) {
				ret.add(new ItemAttribute(attr.getName(), attr.getValue(), false));
			}
			return ret;
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
	 * Creates attributes for this item. Each item can have "replace" specified which
	 * indicates to replace the Attribute/Value or ad a new Attribute/Value.
	 *
	 * @param attributes list of attributes to add
	 * @throws SDBException wraps checked exceptions
	 */
	public void putAttributes(List<ItemAttribute> attributes) throws SDBException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("DomainName", getName());
		params.put("ItemName", identifier);
		int i=1;
		for (ItemAttribute attr : attributes) {
			params.put("Attribute."+i+".Name", attr.getName());
			params.put("Attribute."+i+".Value", attr.getValue());
			if (attr.isReplace()) {
				params.put("Attribute."+i+".Replace", "true");
			}
			i++;
		}
		GetMethod method = new GetMethod();
		try {
			PutAttributesResponse response =
						makeRequest(method, "PutAttributes", params, PutAttributesResponse.class);
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
	 * Deletes one or more attributes.
	 *
	 * @param identifier the name of the item to be deleted
	 * @throws SDBException wraps checked exceptions
	 */
	public void deleteAttributes(List<ItemAttribute> attributes) throws SDBException {
		deleteAttributes(identifier, attributes);
	}

	static List<Item> createList(String [] itemNames, String domainName, String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server) throws SDBException {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (int i=0; i<itemNames.length; i++) {
			ret.add(new Item(itemNames[i], domainName, awsAccessKeyId, awsSecretAccessKey, isSecure, server));
		}
		return ret;
	}
}
