package integrationTest.businessLogic;

import static org.hamcrest.Matchers.equalTo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import businessLogic.dto.ConversionRatioDTO;
import businessLogic.dto.ServiceLocatorDTO;
import businessLogic.dto.UnitDTO;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HeaderConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import security.CocoaKeycloakAdapter;
import security.SecurityConfig;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

@Category(IntegrationTest.class)
public class ConversionRatioEndpointTest {

	private static String accessToken;
	
	@BeforeClass
	public static void setupForAll() throws Exception {
		String keystorePath = "certificates/unit.p12";
		String truststorePath = "certificates/unittruststore.p12";
		String password = System.getenv("BELLINI_PROJECT_PASSWORD");
		String localKeycloakURL = "https://localhost:61001";
		CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(keystorePath, password, truststorePath, password,
				SecurityConfig.realmName,localKeycloakURL);
		HeaderConfig headerConfig = HeaderConfig.headerConfig().overwriteHeadersWithName("Token");
        RestAssured.config = RestAssured.config().sslConfig(new SSLConfig().with().sslSocketFactory(adapter.getSSLSocketFactory())).headerConfig(headerConfig);
        RestAssured.baseURI = "https://localhost:8443/unit/api/conversionRatio";
		RestAssured.authentication = RestAssured.basic(SecurityConfig.username,SecurityConfig.userpass);
    
        String access = adapter.getAccessToken("manager", password, SecurityConfig.clientId, SecurityConfig.clientSecret);
        String accessTokenVerified = null;
        adapter.verify(access);
        accessTokenVerified = access;

		accessToken = accessTokenVerified;
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", accessToken).build();
		
		RequestSpecification request = RestAssured.given();
		request.body(new ServiceLocatorDTO(localKeycloakURL)).contentType(ContentType.JSON);
		request.post("https://localhost:8443/unit/api/serviceLocator/post/keycloakServiceLocation").then().log().all().statusCode(200);
		
		request = RestAssured.given();
		request.get("ping").then().statusCode(200);
		
		resetTableId("src/test/resources/unitJson/alterTableUnit.sql");
		resetTableId("src/test/resources/conversionRatioJson/alterTableConversionRatio.sql");
		
		// adding 6 units to create 3 conversion ratios
		FileInputStream is = new FileInputStream("src/test/resources/conversionRatioJson/unitListJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonArray jsonArray = (JsonArray) JsonParser.parseString(data).getAsJsonArray();
		for (JsonElement json : jsonArray) {
			request = RestAssured.given();
			request.body(json.toString()).contentType(ContentType.JSON);
			request.post("https://localhost:8443/unit/api/unit/post");
		}
	}

	@Before
	public void setup() throws IOException, SQLException {
		resetTableId("src/test/resources/conversionRatioJson/alterTableConversionRatio.sql");
		// create 3 conversion controller
		FileInputStream is = new FileInputStream("src/test/resources/conversionRatioJson/conversionRatioListJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonArray jsonArray = (JsonArray) JsonParser.parseString(data).getAsJsonArray();
		for (JsonElement json : jsonArray) {
			RequestSpecification request = RestAssured.given().baseUri("https://localhost:8443/unit/api/conversionRatio");
			request.body(json.toString()).contentType(ContentType.JSON);
			request.post("/post");
		}
	}
	

	@Test
	public void getConversionRatioByIdTest() {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		RestAssured.when().request("GET", "/get/conversionRatioById/1").then().statusCode(200).and().assertThat()
				.body("id", equalTo("1")).body(matchesJsonSchema(schema));
	}
	
	@Test
	public void getConversionRatioByIdTokenAndRoleFailTest() throws Exception {
		CodeStatus500TokenAndRoleTest("/get/conversionRatioListById/1", null, Method.GET);
	}
	
	

	@Test
	public void getConversionRatioByIdTestFailTest() {
		RestAssured.when().request("GET", "/get/conversionRatioById/150").then().statusCode(500);
	}

	@Test
	public void getAllConversionRatiosTest() {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		File listSchema = new File("src/test/resources/conversionRatioJson/conversionRatioListJsonValidator.json");
		RestAssured.when().request("GET", "/get/allConversionRatios").then().statusCode(200).and().assertThat()
				.body(matchesJsonSchema(listSchema)).body("size()", equalTo(3));
		for (int i = 1; i <= 3; i++) {
			RestAssured.when().request("GET", "/get/conversionRatioById/" + i).then().statusCode(200).and().assertThat()
					.body("id", equalTo(String.valueOf(i))).body(matchesJsonSchema(schema));
		}
	}
	
	@Test
	public void getConversionRatiosTokenAndRoleFailTest() throws Exception {
		CodeStatus500TokenAndRoleTest("/get/allConversionRatios", null, Method.GET);
	}
	
	@Test
	public void getConversionRatioListByUnitEndTest() {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		File listSchema = new File("src/test/resources/conversionRatioJson/conversionRatioListJsonValidator.json");
		RestAssured.when().request("GET", "/get/conversionRatioListByUnitEnd/4").then().statusCode(200).and()
				.assertThat().body(matchesJsonSchema(listSchema)).body("size()", equalTo(2));
		for (int i = 2; i <= 3; i++) {
			RestAssured.when().request("GET", "/get/conversionRatioById/" + i).then().statusCode(200).and().assertThat()
					.body("id", equalTo(String.valueOf(i))).body(matchesJsonSchema(schema));
		}
	}
	
	@Test
	public void getConversionRatioListByUnitEndTokenAndRoleFailTest() throws Exception {
		CodeStatus500TokenAndRoleTest("/get/conversionRatioListByUnitEnd/4", null, Method.GET);
	}
	

	@Test
	public void getConversionRatioListByUnitEndFailTest() {
		File emptySchema = new File("src/test/resources/conversionRatioJson/emptyListJsonValidator.json");
		RestAssured.when().request("GET", "/get/conversionRatioListByUnitEnd/12").then().statusCode(200).and()
				.assertThat().body(matchesJsonSchema(emptySchema));
	}

	@Test
	public void getConversionRatioListByUnitStarTest() {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		File listSchema = new File("src/test/resources/conversionRatioJson/conversionRatioListJsonValidator.json");
		RestAssured.when().request("GET", "/get/conversionRatioListByUnitStart/1").then().statusCode(200).and()
				.assertThat().body(matchesJsonSchema(listSchema)).body("size()", equalTo(2));
		for (int i = 1; i <= 2; i++) {
			RestAssured.when().request("GET", "/get/conversionRatioById/" + i).then().statusCode(200).and().assertThat()
					.body("id", equalTo(String.valueOf(i))).body(matchesJsonSchema(schema));
		}
	}
	
	@Test
	public void getConversionRatioListByUnitStartTokenAndRoleFailTest() throws Exception {
		CodeStatus500TokenAndRoleTest("/get/conversionRatioListByUnitStart/1", null, Method.GET);
	}

	@Test
	public void getConversionRatioListByUnitStarFailTest() {
		File emptySchema = new File("src/test/resources/conversionRatioJson/emptyListJsonValidator.json");
		RestAssured.when().request("GET", "/get/conversionRatioListByUnitStart/12").then().statusCode(200).and()
				.assertThat().body(matchesJsonSchema(emptySchema));
	}

	@Test
	public void postConversationRatioTest() throws IOException {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		FileInputStream is = new FileInputStream("src/test/resources/conversionRatioJson/conversionRatioPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.post("/post").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		RestAssured.when().request("GET", "/get/conversionRatioById/4").then().statusCode(200);
	}
	
	@Test
	public void postConversionRatioTokenAndRoleFailTest() throws Exception {
		FileInputStream is = new FileInputStream("src/test/resources/conversionRatioJson/conversionRatioPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		CodeStatus500TokenAndRoleTest("/get/conversionRatioListByUnitStart/1", jsonElement.toString(), Method.POST);
	}

	@Test
	public void postConversationRatioFailTest() throws IOException {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		FileInputStream is = new FileInputStream("src/test/resources/conversionRatioJson/conversionRatioPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.post("/post").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		RestAssured.when().request("GET", "/get/conversionRatioById/4").then().statusCode(200);
		request.post("/post").then().statusCode(500);
	}

	@Test
	public void deleteConversionRatioTest() throws IOException {
		File schema = new File("src/test/resources/conversionRatioJson/conversionRatioJsonValidator.json");
		RequestSpecification request = RestAssured.given();
		request.delete("/delete/1").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		request.delete("/delete/2").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		request.delete("/delete/3").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
	}
	
	@Test
	public void deleteConversionRatioTokenAndRoleFailTest() throws Exception {
		CodeStatus500TokenAndRoleTest("/delete/1",null, Method.DELETE);
	}
	
	@Test
	public void convertTest() {
		RequestSpecification request = RestAssured.given();
		request.get("/get/convert/unit1/unit2").then().statusCode(200).and().assertThat().body("conversionRatio", equalTo((float) 3.0));
		request.get("/get/convert/unit1/unit6").then().statusCode(500);
		request.get("/get/convert/unit1/tallala").then().statusCode(500);
	}
	
	@Test
	public void convertConversionRatioTokenAndRoleFailTest() throws Exception {
		CodeStatus500TokenAndRoleTest("/get/convert/unit1/unit2",null, Method.GET);
	}

	@After
	public void teardown() {
		Response response = RestAssured.request("GET", "get/allConversionRatios");
		String data = response.asString();
		JsonArray jsonArray = (JsonArray) JsonParser.parseString(data).getAsJsonArray();
		for (JsonElement json : jsonArray) {
			String id = ConversionRatioDTO.fromJson(json.toString()).getConversionRatioId();
			System.out.println("deleting conversion ratio" + Long.valueOf(id));
			RequestSpecification request = RestAssured.given();
			request.delete("/delete/" + id);
		}
	}

	@AfterClass
	public static void teardownForAll() throws IOException, SQLException {
		Response response = RestAssured.request("GET", "https://localhost:8443/unit/api/unit/get/allUnits");
		String data = response.asString();
		JsonArray jsonArray = (JsonArray) JsonParser.parseString(data).getAsJsonArray();
		for (JsonElement json : jsonArray) {
			String id = UnitDTO.fromJson(json.toString()).getId();
			System.out.println("deleting unit" + Long.valueOf(id));
			RestAssured.request("DELETE", "https://localhost:8443/unit/api/unit/delete/" + id);
		}
		resetTableId("src/test/resources/unitJson/alterTableUnit.sql");
		RestAssured.reset();
	}

	private static String getFileContent(FileInputStream fis, String encoding) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		}
	}
	
	private static void CodeStatus500TokenAndRoleTest(String requestFunctionPath, String requestBody, Method requestMethod) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, SignatureException{
		String localKeycloakURL = "https://localhost:61001";
		String fakeAccessToken = "sdbfhijdjknflksfdfgsdfgdssfgdf.gfjhojosedfoidfgjoi.fhud9gsiojefopk";
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", fakeAccessToken).build();
		RequestSpecification request = RestAssured.given();
			if(requestBody != null)
				request.body(requestBody);
		switch(requestMethod.toString()) {
			case "POST":
				request.post(requestFunctionPath);
				break;
			case "PUT":
				request.put(requestFunctionPath);
				break;
			case "DELETE":
				request.delete(requestFunctionPath);
				break;
			case "GET":
				request.get(requestFunctionPath);
				break;
		}
		String keystorePath = "certificates/unit.p12";
		String truststorePath = "certificates/unittruststore.p12";
		String password = "progettobellini2023";
		CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(keystorePath,password,truststorePath,password, SecurityConfig.realmName, localKeycloakURL);
		String access = adapter.getAccessToken("customer", password, SecurityConfig.clientId, SecurityConfig.clientSecret);
	    String accessTokenVerified = null;
	    adapter.verify(access);
	    accessTokenVerified = access;
		String accessTokenWithCustomerRole = accessTokenVerified;
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", accessTokenWithCustomerRole).build();
		request = RestAssured.given();
		if(requestBody != null)
			request.body(requestBody);
	switch(requestMethod.toString()) {
		case "POST":
			request.post(requestFunctionPath);
			break;
		case "PUT":
			request.put(requestFunctionPath);
			break;
		case "DELETE":
			request.delete(requestFunctionPath);
			break;
		case "GET":
			request.get(requestFunctionPath);
			break;
	}
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", accessToken).build();
	}

	private static void resetTableId(String resetResourcePath) throws SQLException, FileNotFoundException {
		// Registering the Driver
		DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
		// Getting the connection
		String mysqlUrl = "jdbc:mysql://localhost:50002/unitDS";
		//String mysqlUrl = "jdbc:mysql://localhost:3306/unit-ds";
		Connection con = DriverManager.getConnection(mysqlUrl, "javaclient", System.getenv("MYSQL_PASSWORD"));
		// Initialize the script runner
		ScriptRunner sr = new ScriptRunner(con);
		// Creating a reader object
		Reader reader = new BufferedReader(new FileReader(resetResourcePath));
		// Running the script
		sr.runScript(reader);
	}
}
