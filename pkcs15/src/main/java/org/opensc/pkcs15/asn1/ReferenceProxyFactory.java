/***********************************************************
 * $Id$
 * 
 * PKCS#15 cryptographic provider of the opensc project.
 * http://www.opensc-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created: 29.12.2007
 * 
 ***********************************************************/

package org.opensc.pkcs15.asn1;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bouncycastle.asn1.DEREncodable;

/**
 * A static factory for referenced entities.
 * 
 * @author wglas
 */
public class ReferenceProxyFactory<ReferenceType extends DEREncodable,EntityType extends DEREncodable> {

    private static class DirectoryInvocationHandler<ReferenceType extends DEREncodable,EntityType extends DEREncodable>
    implements InvocationHandler
    {
        private Directory<ReferenceType,EntityType> directory;
        private EntityType resolvedEntity;
        private ReferenceType reference;
        
        DirectoryInvocationHandler(ReferenceType reference, Directory<ReferenceType,EntityType> directory)
        {
            this.reference = reference;
            this.directory = directory;
            this.resolvedEntity = null;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
           
            // A proxy is serialized by its original reference.
            if (method.getParameterTypes().length == 0 &&
                    method.getName().equals("getDERObject"))
                return this.reference.getDERObject();
            
            if (this.resolvedEntity == null) {
                
                if (this.directory == null)
                    throw new IllegalArgumentException("Try to resolve a proxy with directory specified.");
                
                this.resolvedEntity =
                    this.directory.resolveReference(this.reference,this.resolvedEntity);
             
                if (this.resolvedEntity == null)
                    throw new IllegalArgumentException("The refernce ["+this.reference+"] could not be resolved.");
            }
                
            // Implement the marker interface ReferenceProxy.
            if (method.getParameterTypes().length == 0 &&
                    method.getName().equals("resolveEntity"))
                return this.resolvedEntity;
            
            return method.invoke(this.resolvedEntity,args);
        }
    }
    
    private Class<?>[] interfaces;
    
    public ReferenceProxyFactory(Class<?> entityInterface)
    {
        this.interfaces = new Class<?>[] {entityInterface,ReferenceProxy.class};
    }
    
    /**
     * Return a proxy, which lazily resolves a reference by means of
     * the supplied directory.
     * 
     * @param reference An integer reference.
     * @param directory The directory used for dereferencing the reference.
     * @return An instance of EntityType, which delegates to a resolved entity. 
     * @throws IllegalArgumentException
     */
    public EntityType getProxy(ReferenceType reference, Directory<ReferenceType,EntityType> directory)
    throws IllegalArgumentException
    {
        return (EntityType)
        Proxy.newProxyInstance(ReferenceProxyFactory.class.getClassLoader(),
                this.interfaces,
                new DirectoryInvocationHandler<ReferenceType,EntityType>(reference,directory));
        
    }
    
}
