package com.logica.ngph.utils;

import iaik.asn1.ASN;
import iaik.asn1.ASN1Object;
import iaik.asn1.ObjectID;
import iaik.asn1.structures.AlgorithmID;
import iaik.asn1.structures.Attribute;
import iaik.asn1.structures.ChoiceOfTime;
import iaik.pkcs.pkcs7.IssuerAndSerialNumber;
import iaik.pkcs.pkcs7.SignedData;
import iaik.pkcs.pkcs7.SignerInfo;
import iaik.security.provider.IAIK;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class CertificateVerifier 
{	
	static Logger logger = Logger.getLogger(CertificateVerifier.class);
	private final static String propName = "System.properties";
	static PrivateKey pKey = null;
	static X509Certificate cert = null;
	static CertStore certStore = null;
	static KeyStore ks = null;
	static String alias = null;
	static String CertName = null;
	
	static 
	{
		//loading property file in memory
		Properties props = new Properties();
		try 
		{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName));
			CertName=props.getProperty("CertificateName");
			Security.insertProviderAt(new IAIK(), 2);
			IAIK.addAsProvider();
			ks = KeyStore.getInstance("Windows-MY");
			ks.load(null, null);
		} 
		catch (IOException e) 
		{
			logger.error(e, e);
		}
		catch (Exception e) 
		{
			logger.error(e, e);
		}
	}
	
	public static void main(String[] args) 
	{/*
		try
		{
		//Open the file that is the first 
		FileInputStream fstream = new FileInputStream("C:/MessageFormats/IFN700.txt");
		//FileInputStream fstream = new FileInputStream("C:/MessageFormats/N07.txt");
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		String mes = "";
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)
		{
		//Print the content on the console
		mes = mes + strLine + "\r\n"; 
		//System.out.println(strLine);
		}
		//Close the input stream
		in.close();
		//System.out.println(mes);  

		System.out.println(CertificateVerifier.validateCertificate(mes));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	*/}
	public static String validateCertificate(String mes) 
	{
		String encrypted = null;
		if(StringUtils.isNotBlank(mes) && StringUtils.isNotEmpty(mes))
		{
			try
			{
				AlgorithmID[] algIDs = {AlgorithmID.md5};
				SignedData signed_data = new SignedData(mes.getBytes(),algIDs);	
				boolean isCertPrsnt = ks.containsAlias(CertName);
				if(isCertPrsnt == true)
				{
					alias = CertName;
				}
				else
				{
					logger.error("No Certificate Installed on Computer", new CertificateException());
					return null;
				}
				cert = (X509Certificate)ks.getCertificate(alias);
				pKey = (PrivateKey)ks.getKey(alias, null);
				logger.info("Found a private key with Alias name:"+alias);
				cert.checkValidity();
				logger.info(cert);			 
				iaik.x509.X509Certificate[] certsToBeAttached = null;
				certsToBeAttached = new iaik.x509.X509Certificate[1];
				certsToBeAttached[0] =  new iaik.x509.X509Certificate (cert.getEncoded());	
				if (certsToBeAttached != null) 
				{
					signed_data.setCertificates(certsToBeAttached);
				}
				IssuerAndSerialNumber issuer = new IssuerAndSerialNumber(new iaik.x509.X509Certificate (cert.getEncoded()));
				SignerInfo signer_info = new SignerInfo(issuer, AlgorithmID.md5, pKey);
				Attribute[] attributes = new Attribute[2];
				attributes[0] =	new Attribute(ObjectID.contentType,	new ASN1Object[] { ObjectID.pkcs7_data });
				ChoiceOfTime choiceOfTime =new ChoiceOfTime(new java.util.Date(), ASN.GeneralizedTime);
				attributes[1] =	new Attribute(ObjectID.signingTime,	new ASN1Object[] { choiceOfTime.toASN1Object()});
				signer_info.setAuthenticatedAttributes(attributes);
				signed_data.addSignerInfo(signer_info);
				//encrypted = Base64.encodeBytes(signed_data.getEncoded());  
				String ss = new String(org.apache.commons.codec.binary.Base64.encodeBase64(signed_data.getEncoded()));
				int i = 0;
				String tempStr = ss.substring(i,i+64);
				i = i + 64;
				while (ss.length() - i > 64)
				{
					tempStr = tempStr + "\r\n" + ss.substring(i,i+64);
					i = i + 64;
				}
				tempStr = tempStr + "\r\n" + ss.substring(i,ss.length());
				encrypted = tempStr;
				logger.info("Message :" + encrypted + "\r\nMessage Length : " + encrypted.length());
				/*
                Signature sig = Signature.getInstance("MD5withRSA");
                sig.initSign(pKey);
                sig.update(mes.getBytes());
                byte[] signature = sig.sign();
                System.out.println(signature.length+"*******");
                //load X500Name
                X500Name xName   = X500Name.asX500Name(cert.getSubjectX500Principal());
                //load serial number
                BigInteger serial   = cert.getSerialNumber();
                //load digest algorithm
                AlgorithmId digestAlgorithmId = new AlgorithmId(AlgorithmId.MD5_oid);
                //load signing algorithm
                AlgorithmId signAlgorithmId = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
                //Create SignerInfo:
                SignerInfo sInfo = new SignerInfo(xName, serial, digestAlgorithmId, signAlgorithmId, signature);
                //Create ContentInfo:
                ContentInfo cInfo = new ContentInfo(ContentInfo.DATA_OID, new DerValue(DerValue.tag_OctetString, mes.getBytes()));
                //Create PKCS7 Signed data
                PKCS7 p7 = new PKCS7(new AlgorithmId[] { digestAlgorithmId }, cInfo, new java.security.cert.X509Certificate[] { cert }, new SignerInfo[] { sInfo });
                //Write PKCS7 to bYteArray
                ByteArrayOutputStream bOut = new DerOutputStream(2048);
                p7.encodeSignedData(bOut);
                byte[] encodedPKCS7 = bOut.toByteArray();
                logger.info(encodedPKCS7.length);
                encrypted = Base64Coder.encodeString(new String(encodedPKCS7)); 
                */
			}
			catch (KeyStoreException e) 
			{                
				logger.error(e, e);
			}
			catch (NoSuchAlgorithmException e) 
			{                
				logger.error(e, e);
			} 
			catch (CertificateException e) 
			{             
				logger.error(e, e);
			} 
			catch (UnrecoverableKeyException e) 
			{               
				logger.error(e, e);
			} 
			catch (Exception e) 
			{
				logger.error(e, e);
			}
		}		
		else
		{
			logger.error("Null message received by validateCertificate");
		}
		return encrypted;
	}
}
