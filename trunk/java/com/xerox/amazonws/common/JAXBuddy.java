//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//

package com.xerox.amazonws.common;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class implements some helpful methods to marshal and unmarshal xml.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class JAXBuddy {
    public static <T> InputStream serializeXMLFile(Class<T> c, Object object) 
            throws JAXBException, IOException{
        Marshaller m = getMarshaller(c);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.marshal(object, baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return bais;
    }
 
    public static <T> T deserializeXMLStream(Class<T> c, InputStream is) throws JAXBException{
        Unmarshaller u = getUnmarshaller(c);
        T result = c.cast(u.unmarshal(is));
        return result;
    }
    
    private static Marshaller getMarshaller(Class<?> c) throws JAXBException{     
        String typePackage = c.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(typePackage);
        Marshaller m = jc.createMarshaller();
        return m;
    }

    private static Unmarshaller getUnmarshaller(Class<?> c) throws JAXBException{           
        String typePackage = c.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(typePackage, c.getClassLoader());
        Unmarshaller u = jc.createUnmarshaller();
        return u;
    }
}
