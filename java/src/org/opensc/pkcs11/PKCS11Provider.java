/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 * 
 * Copyright (C) 2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 16, 2006
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 * 
 ***********************************************************/

package org.opensc.pkcs11;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.security.Provider;
import java.security.ProviderException;
import java.security.interfaces.DSAKey;
import java.security.interfaces.RSAKey;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensc.pkcs11.wrap.PKCS11Exception;
import org.opensc.pkcs11.wrap.PKCS11SessionChild;
import org.opensc.util.DestroyableHolder;
import org.opensc.util.DestroyableParent;

/**
 * The PKCS#11 provider of the OpenSC project.
 * 
 * @author wglas
 */
public class PKCS11Provider extends Provider implements DestroyableParent
{
	static private final Log log = LogFactory.getLog(PKCS11Provider.class);

	/**
	 * To be changed, when the interface changes.
	 */
	private static final long serialVersionUID = -2568219416560640437L;

	/* This value has to correspond to the value of version in build.xml */
	private static final double version = 0.1;
	/* This value has to correspond to the value of patchlevel in build.xml */
	private static final double patchlevel = 1;
	
	static {
		System.loadLibrary("opensc-PKCS11-"+version); 
	} 
	 
	 private long pkcs11ModuleHandle;
	 private ShutdownThread shutdownThread;
	 private DestroyableHolder destroyableHolder;

	 private native long loadPKCS11Module(byte[] nativeFilename) throws PKCS11Exception;
	 private native void unloadPKCS11Module(long handle) throws PKCS11Exception;
	
	 /* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		cleanup();
		super.finalize();
	}
	
	/**
	 * Additional function, which may e used by application to cleanup all
	 * allocated C resources of the underlying JNI plugin.
	 */
	public synchronized void cleanup()
	{
		if (pkcs11ModuleHandle != 0L)
		{
			try
			{
				destroyableHolder.destroy();
			} catch (DestroyFailedException e)
			{
				log.error("Failure during destruction of C resources:",e);
			}
			
			try
			{
				unloadPKCS11Module(pkcs11ModuleHandle);
			} catch (PKCS11Exception e)
			{
				log.error("Failure unloading PKCS#11 module:",e);
			}
			
			pkcs11ModuleHandle = 0L;
			if (shutdownThread!=null) {
				Runtime.getRuntime().removeShutdownHook(shutdownThread);
				shutdownThread=null;
			}
		}		
	}
	
	private synchronized void deregisterSutdownThread()
	{
		shutdownThread=null;
	}
	
	/**
	 * @author wglas
	 *
	 * A shutdown hook, which is registered using Runtime.registerShutdownHook().
	 * This is used in order to assure, that the allocated C resources of the
	 * JNI plugin are freed.
	 */
	private class ShutdownThread extends Thread
	{
		private final PKCS11Provider provider;
		
		ShutdownThread(PKCS11Provider provider)
		{
			this.provider = provider;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			provider.deregisterSutdownThread();
			provider.cleanup();
		}
	}
	
	private void initialize(String filename) throws IOException
	{
		pkcs11ModuleHandle = loadPKCS11Module(filename.getBytes());
		destroyableHolder = new DestroyableHolder();
		shutdownThread = new ShutdownThread(this);
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		
		putService(new PKCS11Service
        		(this, "KeyStore", "PKCS11", "org.opensc.pkcs11.spi.PKCS11KeyStoreSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "NONEwithRSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "MD5withRSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "SHA1withRSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "SHA256withRSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "SHA384withRSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "SHA512withRSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "SHA1withDSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11SignatureService
        		(this, "Signature", "NONEwithDSA", "org.opensc.pkcs11.spi.PKCS11SignatureSpi"));
		putService(new PKCS11CipherService
        		(this, "Cipher", "RSA/ECB/PKCS1Padding", "org.opensc.pkcs11.spi.PKCS11CipherSpi"));
	}
	
	/**
	 * Construct a PKCS11 provider instance named <tt>OpenSC-PKCS11</tt>.
	 * 
	 * @param filename The full filename of the pkcs11 library to load.
	 * @throws IOException Upon errors when reading the config stream.
	 */
	public PKCS11Provider(String filename) throws IOException
	{
		super("OpenSC-PKCS11", version+0.001*patchlevel, "OpenSC PKCS11 provider.");
		this.pkcs11ModuleHandle = 0;
		initialize(filename);
	}

	/**
	 * Construct a PKCS11 provider instance using a modified provider name.
	 * Use this constructor, if you want to load more than one PKCS#11 module
	 * inside your JAVA application.
	 * 
	 * @param filename The full filename of the pkcs11 library to load.
	 * @param suffix The suffix to the provier name. The provider will be named
	 *               <tt>OpenSC-PKCS11-&lt;suffix&gt;</tt>.
	 * @throws IOException Upon errors when reading the config stream.
	 */
	public PKCS11Provider(String filename, String suffix) throws IOException
	{
		super("OpenSC-PKCS11-"+suffix, version+0.001*patchlevel, "OpenSC PKCS11 provider.");
		this.pkcs11ModuleHandle = 0;
		initialize(filename);
	}

	// we have our own ServiceDescription implementation that overrides
	// newInstance()
	// that calls the (Provider, String) constructor instead of the no-args
	// constructor
	private static class PKCS11Service extends Service
	{

		private static final Class[] paramTypes = { PKCS11Provider.class,
				String.class };

		PKCS11Service(Provider provider, String type, String algorithm,
				String className)
		{
			super(provider, type, algorithm, className, null, null);
		}

		public Object newInstance(Object param)
		{
			try
			{
				// get the Class object for the implementation class
				Class clazz;
				PKCS11Provider provider = (PKCS11Provider) getProvider();
				ClassLoader loader = provider.getClass().getClassLoader();
				if (loader == null)
				{
					clazz = Class.forName(getClassName());
				} else
				{
					clazz = loader.loadClass(getClassName());
				}
				
				// fetch the (Provider, String) constructor
				Constructor cons = clazz.getConstructor(paramTypes);
				// invoke constructor and return the SPI object
				Object obj = cons.newInstance(new Object[] { provider,
						getAlgorithm() });
				return obj;

			} catch (Exception e)
			{
				throw new ProviderException("Caught exception in newInstance:",e);
			}
		}
	}

	private static class PKCS11SignatureService extends PKCS11Service
	{
		PKCS11SignatureService(Provider provider, String type, String algorithm, String className)
		{
			super(provider, type, algorithm, className);
		}

		/* (non-Javadoc)
		 * @see java.security.Provider.Service#supportsParameter(java.lang.Object)
		 */
		@Override
		public boolean supportsParameter(Object param)
		{
			if (! (param instanceof PKCS11SessionChild)) return false;
			
			if (param instanceof RSAKey)
				return super.getAlgorithm().endsWith("RSA");
			
			if (param instanceof DSAKey)
				return super.getAlgorithm().endsWith("DSA");
			
			return false;
		}
	}
	
	private static class PKCS11CipherService extends PKCS11Service
	{
		PKCS11CipherService(Provider provider, String type, String algorithm, String className)
		{
			super(provider, type, algorithm, className);
		}

		/* (non-Javadoc)
		 * @see java.security.Provider.Service#supportsParameter(java.lang.Object)
		 */
		@Override
		public boolean supportsParameter(Object param)
		{
			if (! (param instanceof PKCS11SessionChild)) return false;
			
			if (param instanceof RSAKey)
				return super.getAlgorithm().startsWith("RSA");
			
			return false;
		}
	}
	
	/**
	 * @return Returns the pkcs11ModuleHandle used by calls to the
	 *         natvie JNI functions of associated services.
	 */
	public long getPkcs11ModuleHandle()
	{
		return pkcs11ModuleHandle;
	}
	
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.util.DestroyableParent#register(javax.security.auth.Destroyable)
	 */
	public void register(Destroyable destroyable)
	{
		destroyableHolder.register(destroyable);
	}
	
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.util.DestroyableParent#deregister(javax.security.auth.Destroyable)
	 */
	public void deregister(Destroyable destroyable)
	{
		destroyableHolder.deregister(destroyable);
	}
	
}
