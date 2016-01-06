package org.mcupdater.certs;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.mcupdater.util.MCUpdater;

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
import java.util.List;

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
		MCUpdater.apiLogger.info("Registering root certificates");
		keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		Path ksPath = Paths.get(System.getProperty("java.home")).resolve("lib").resolve("security").resolve("cacerts");
		String ksPassword = "changeit";
		keyStore.load(Files.newInputStream(ksPath), ksPassword.toCharArray());
		List<String> resources = IOUtils.readLines(MCUpdater.class.getResourceAsStream("/org/mcupdater/certs/certlist.txt"), Charsets.UTF_8);
		for (String rsrc : resources) {
			if (rsrc.endsWith(".pem")) {
				addCertificateFromStream(MCUpdater.class.getResourceAsStream("/org/mcupdater/certs/" + rsrc), rsrc.substring(0, rsrc.length() - 4));
				MCUpdater.apiLogger.info("Registered root certificate: " + rsrc.substring(0, rsrc.length() - 4));
			}
		}
		updateSSLContext();
	}

	public void addCertificateFromStream(InputStream cert, String alias) throws Exception {
		Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(cert);
		keyStore.setCertificateEntry(alias, ca);
	}

	private void updateSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
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
