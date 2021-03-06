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
