package security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.json.JSONArray;
import org.json.JSONObject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class CocoaKeycloakAdapter {

	private SSLContext sslContext;
	private SSLSocketFactory sslSocketFactory;
	private String realmName;
	private String keycloakURL;
	private Map<String, SecurityRole> roles;

	public CocoaKeycloakAdapter(String keystorePath, String keystorePassword, String truststorePath,
			String trustStorePassword, String realmName, String keycloakURL) {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			FileInputStream inputStream = new FileInputStream(new File(keystorePath));
			keyStore.load(inputStream, keystorePassword.toCharArray());
			KeyStore trustStore = KeyStore.getInstance("PKCS12");
			FileInputStream inputStream2 = new FileInputStream(new File(truststorePath));
			trustStore.load(inputStream2, trustStorePassword.toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trustStore);
			this.sslContext = SSLContext.getInstance("TLS");
			this.sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			this.sslSocketFactory = new SSLSocketFactory(sslContext);
			this.realmName = realmName;
			this.keycloakURL = keycloakURL;
			this.roles = new HashMap<>();
			SecurityRole customer = new SecurityRole("customer");
			SecurityRole manager = new SecurityRole("manager");
			manager.addRole(customer);
			SecurityRole owner = new SecurityRole("owner");
			owner.addRole(manager);
			roles.put(customer.getName(), customer);
			roles.put(manager.getName(), manager);
			roles.put(owner.getName(), owner);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CocoaKeycloakAdapter(String realmName, String keycloakURL, String keystorePassword,
			String trustStorePassword) {
		try {
			ModelControllerClient client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"),
					9990);
			ModelNode request = new ModelNode();
			request.get("operation").set("read-attribute");
			request.get("name").set("path");
			request.get("address").add("subsystem", "elytron");
			request.get("address").add("key-store", "twoWayKS");
			ModelNode response = client.execute(request);
			String keystorePath = System.getProperty("jboss.server.config.dir") + "\\"
					+ response.get("result").asString();

			request.clear();
			request.get("operation").set("read-attribute");
			request.get("name").set("path");
			request.get("address").add("subsystem", "elytron");
			request.get("address").add("key-store", "twoWayTS");
			response = client.execute(request);
			String truststorePath = System.getProperty("jboss.server.config.dir") + "\\"
					+ response.get("result").asString();

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			FileInputStream inputStream = new FileInputStream(new File(keystorePath));
			keyStore.load(inputStream, keystorePassword.toCharArray());
			KeyStore trustStore = KeyStore.getInstance("PKCS12");
			FileInputStream inputStream2 = new FileInputStream(new File(truststorePath));
			trustStore.load(inputStream2, trustStorePassword.toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trustStore);
			this.sslContext = SSLContext.getInstance("TLS");
			this.sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

			this.sslSocketFactory = new SSLSocketFactory(sslContext);
			this.realmName = realmName;
			this.keycloakURL = keycloakURL;
			this.roles = new HashMap<>();
			SecurityRole customer = new SecurityRole("customer");
			SecurityRole manager = new SecurityRole("manager");
			manager.addRole(customer);
			SecurityRole owner = new SecurityRole("owner");
			owner.addRole(manager);
			roles.put(customer.getName(), customer);
			roles.put(manager.getName(), manager);
			roles.put(owner.getName(), owner);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SSLContext getSSLContext() {
		return sslContext;
	}

	public SSLSocketFactory getSSLSocketFactory() {
		return sslSocketFactory;
	}

	public boolean verify(String token, PublicKey publicKey) throws Exception {
		String[] tokenParts = token.split("\\.");
		String header = tokenParts[0];
		String payload = tokenParts[1];
		String signature = tokenParts[2];
		String headerPayload = header + "." + payload;
		byte[] signatureBytes = Base64.getUrlDecoder().decode(signature);
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(publicKey);
		sig.update(headerPayload.getBytes("UTF-8"));
		return sig.verify(signatureBytes);
	}

	public boolean verify(String token) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, CertificateException, IOException, SignatureException {
		String[] tokenParts = token.split("\\.");
		String header = tokenParts[0];
		String payload = tokenParts[1];
		String signature = tokenParts[2];
		String headerPayload = header + "." + payload;
		byte[] signatureBytes = Base64.getUrlDecoder().decode(signature);
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(this.getPublicKey());
		sig.update(headerPayload.getBytes("UTF-8"));
		return sig.verify(signatureBytes);
	}

	public PublicKey getPublicKey()
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
		URL url = new URL(keycloakURL + "/auth/realms/" + realmName + "/protocol/openid-connect/certs");
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setSSLSocketFactory(sslContext.getSocketFactory());
		connection.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			response.append(line);
		}
		in.close();

		JSONObject jwks = new JSONObject(response.toString());
		JSONArray keys = jwks.getJSONArray("keys");
		String x5cString = "";
		for (int i = 0; i < keys.length(); i++) {
			JSONObject entry = keys.getJSONObject(i);
			if (entry.getString("alg").equals("RS256")) {
				x5cString = entry.getJSONArray("x5c").getString(0);
			}
		}
		if (x5cString.isEmpty()) {
			throw new RuntimeException("Could not find RSA256 key in Keycloak response");
		}
		byte[] x5cBytes = Base64.getDecoder().decode(x5cString);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(x5cBytes));
		return cert.getPublicKey();
	}

	public String getAccessToken(String username, String password, String clientId, String clientSecret)
			throws IOException {
		String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
		URL url = new URL("https://localhost:61001/auth/realms/cocoa-infra/protocol/openid-connect/token");
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setSSLSocketFactory(sslContext.getSocketFactory());
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Authorization", "Basic " + credentials);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		String body = "grant_type=password&username=" + username + "&password=" + password;
		connection.setDoOutput(true);
		connection.getOutputStream().write(body.getBytes());

		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new IOException();
		}
		JSONObject response = new JSONObject(new String(connection.getInputStream().readAllBytes()));
		return response.getString("access_token");
	}

	public void verifyRole(String token, String allowed)
			throws NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, IOException {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = this.getPublicKey();
		Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
		// Extract the roles from the realm_access claim
		ArrayList<String> rolesArray = ((LinkedHashMap<String, ArrayList<String>>) claims.get("realm_access"))
				.get("roles");
		for (String tokenRole : rolesArray) {
			if (roles.containsKey(tokenRole)) {
				if (roles.get(tokenRole).containsRole(allowed) || tokenRole.equals(allowed)) {
					return;
				}
			}
		}
		throw new CertificateException("Given token is invalid");
	}
}
