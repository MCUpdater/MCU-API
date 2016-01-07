package org.mcupdater.certs;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

public class SSLExpansion {
	private static SSLExpansion instance;
	private KeyStore keyStore;

	public static SSLExpansion getInstance() {
		if (instance == null) {
			try {
				instance = new SSLExpansion();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public SSLExpansion() throws Exception {
		keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		Path ksPath = Paths.get(System.getProperty("java.home")).resolve("lib").resolve("security").resolve("cacerts");
		String ksPassword = "changeit";
		keyStore.load(Files.newInputStream(ksPath), ksPassword.toCharArray());
	}

	public void addCertificateFromStream(InputStream cert, String alias) throws Exception {
		Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(cert);
		keyStore.setCertificateEntry(alias, ca);
	}

	public void updateSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);
		for (Enumeration<String> elements = keyStore.aliases(); elements.hasMoreElements(); ) {
			System.out.println("@ " + elements.nextElement());
		}
		System.out.println();
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, tmf.getTrustManagers(), null);
		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
	}

}
