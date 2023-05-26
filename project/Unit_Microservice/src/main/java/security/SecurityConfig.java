package security;

public class SecurityConfig {
	
	public static String realmName = "cocoa-infra";
	public static String keycloakURL = "https://172.16.0.100:8443";
	public static String clientId = "unit";
	public static String clientSecret = System.getenv("UNITCLIENT_SECRET");
	public static String storePassword = System.getenv("BELLINI_PROJECT_PASSWORD");
	public static String username = "userTest";
	public static String userpass = System.getenv("BELLINI_PROJECT_PASSWORD")+"!";
}
