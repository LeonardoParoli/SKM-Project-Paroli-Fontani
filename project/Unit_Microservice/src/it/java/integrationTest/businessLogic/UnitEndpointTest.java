package integrationTest.businessLogic;

import static org.hamcrest.Matchers.equalTo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import businessLogic.dto.ServiceLocatorDTO;
import businessLogic.dto.UnitDTO;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import security.CocoaKeycloakAdapter;
import security.SecurityConfig;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;

@Category(IntegrationTest.class)
public class UnitEndpointTest {

	private static String accessToken;

	@BeforeClass
	public static void setupForAll1() throws Exception {
		String keystorePath = "certificates/unit.p12";
		String truststorePath = "certificates/unittruststore.p12";
		String password = System.getenv("BELLINI_PROJECT_PASSWORD");
		String localKeycloakURL = "https://localhost:61001";
		
		CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(keystorePath, password, truststorePath, password,
				SecurityConfig.realmName,localKeycloakURL);

		RestAssured.config = RestAssured.config()
				.sslConfig(new SSLConfig().with().sslSocketFactory(adapter.getSSLSocketFactory()));
		RestAssured.baseURI = "https://localhost:8443/unit/api/unit";
		RestAssured.authentication = RestAssured.basic(SecurityConfig.username, SecurityConfig.userpass);

		String access = adapter.getAccessToken("manager", password, SecurityConfig.clientId,
				SecurityConfig.clientSecret);
		String accessTokenVerified = null;
		adapter.verify(access);
		accessTokenVerified = access;

		accessToken = accessTokenVerified;
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", accessToken).build();

		RequestSpecification request = RestAssured.given();
		request.body(new ServiceLocatorDTO(localKeycloakURL)).contentType(ContentType.JSON);
		request.post("https://localhost:8443/unit/api/serviceLocator/post/keycloakServiceLocation").then().statusCode(200);
		
		request = RestAssured.given();
		request.log().all().get("/ping").then().log().all();

		resetTableId("src/test/resources/unitJson/alterTableUnit.sql");
		resetTableId("src/test/resources/conversionRatioJson/alterTableConversionRatio.sql");
	}

	@Before
	public void setup() throws IOException, SQLException, ClassNotFoundException {
		resetTableId("src/test/resources/unitJson/alterTableUnit.sql");

		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitListJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonArray jsonArray = (JsonArray) JsonParser.parseString(data).getAsJsonArray();
		for (JsonElement json : jsonArray) {
			RequestSpecification request = RestAssured.given();
			request.body(json.toString()).contentType(ContentType.JSON);
			request.post("/post").then().statusCode(200);
		}
	}

	@Test
	public void getUnitByIdTest() {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		RestAssured.when().request("GET", "/get/unitById/1").then().statusCode(200).and().assertThat()
				.body("id", equalTo("1")).body(matchesJsonSchema(schema));
	}

	@Test
	public void getUnitByIdFailTokenAndRoleTest() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException, IOException {
		CodeStatus500TokenAndRoleTest("/get/unitById/1", null, Method.GET);
	}

	@Test
	public void getUnitByIdFailTest() {
		RestAssured.when().request("GET", "/get/unitById/150").then().statusCode(500);
	}

	@Test
	public void getUnitByNameTest() {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		RestAssured.when().request("GET", "/get/unitByName/unit1").then().statusCode(200).and().assertThat()
				.body("name", equalTo("unit1")).body(matchesJsonSchema(schema));
	}

	@Test
	public void getUnitByNameFailTest() {
		RestAssured.when().request("GET", "/get/unitByName/dfaswasdf").then().statusCode(500);
	}

	@Test
	public void getUnitByNameFailTokenAndRoleTest() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException, IOException {
		CodeStatus500TokenAndRoleTest("/get/unitByName/unit1", null, Method.GET);
	}

	@Test
	public void getAllUnitsTest() {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		File listSchema = new File("src/test/resources/unitJson/unitListJsonValidator.json");
		RestAssured.when().request("GET", "/get/allUnits").then().statusCode(200).and().assertThat()
				.body(matchesJsonSchema(listSchema)).body("size()", equalTo(3));
		for (int i = 1; i <= 3; i++) {
			RestAssured.when().request("GET", "/get/unitById/" + i).then().statusCode(200).and().assertThat()
					.body("id", equalTo(String.valueOf(i))).body(matchesJsonSchema(schema));
		}
	}

	@Test
	public void getAllUnitsFailTokenAndRoleTest() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException, IOException {
		CodeStatus500TokenAndRoleTest("/get/allUnits", null, Method.GET);
	}

	@Test
	public void putUnitTest() throws IOException {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPutJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.put("/put/1").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		RestAssured.when().request("GET", "/get/unitByName/unit300").then().statusCode(200).and().assertThat()
				.body("name", equalTo("unit300")).body(matchesJsonSchema(schema));
	}

	@Test
	public void putUnitFailTest() throws IOException {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.put("/put/1").then().statusCode(200).body(matchesJsonSchema(schema));
		request.put("/put/2").then().statusCode(500);
	}

	@Test
	public void putUnitFailTokenAndRoleTest() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException, IOException {
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		CodeStatus500TokenAndRoleTest("/put/1", jsonElement.toString(), Method.PUT);
	}

	@Test
	public void deleteUnitTest() throws IOException {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		RequestSpecification request = RestAssured.given();
		request.delete("/delete/1").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		request.delete("/delete/2").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		request.delete("/delete/3").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
	}

	@Test
	public void deleteUnitFailTokenAndRoleTest() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException, IOException {
		CodeStatus500TokenAndRoleTest("/delete/1", null, Method.DELETE);
	}

	@Test
	public void postUnitTest() throws IOException {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.post("/post").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		RestAssured.when().request("GET", "/get/unitByName/unit100").then().statusCode(200).and().assertThat()
				.body("name", equalTo("unit100")).body(matchesJsonSchema(schema));
	}

	@Test
	public void postUnitFailTokenAndRoleTest() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException, IOException {
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPostFailJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		CodeStatus500TokenAndRoleTest("/post", jsonElement.toString(), Method.POST);
	}

	@Test
	public void postFailUnitTest() throws IOException {
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPostFailJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.post("/post").then().statusCode(500);
	}

	@Test
	public void doublePostUnitFailTest() throws IOException {
		File schema = new File("src/test/resources/unitJson/unitJsonValidator.json");
		FileInputStream is = new FileInputStream("src/test/resources/unitJson/unitPostJson.json");
		String data = getFileContent(is, "UTF-8");
		JsonElement jsonElement = (JsonElement) JsonParser.parseString(data).getAsJsonObject();
		RequestSpecification request = RestAssured.given();
		request.body(jsonElement.toString()).contentType(ContentType.JSON);
		request.post("/post").then().statusCode(200).and().assertThat().body(matchesJsonSchema(schema));
		RestAssured.when().request("GET", "/get/unitByName/unit100").then().statusCode(200).and().assertThat()
				.body("name", equalTo("unit100")).body(matchesJsonSchema(schema));
		request.post("post").then().statusCode(500);
	}

	@After
	public void teardown() throws SQLException, FileNotFoundException {
		Response response = RestAssured.request("GET", "/get/allUnits");
		String data = response.asString();
		JsonArray jsonArray = (JsonArray) JsonParser.parseString(data).getAsJsonArray();
		for (JsonElement json : jsonArray) {
			String id = UnitDTO.fromJson(json.toString()).getId();
			System.out.println("deleting unit" + Long.valueOf(id));
			RequestSpecification request = RestAssured.given();
			request.delete("/delete/" + id);
		}
	}

	@AfterClass
	public static void teardownAll() {
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

	private static void CodeStatus500TokenAndRoleTest(String requestFunctionPath, String requestBody,
			Method requestMethod) throws IOException, InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, CertificateException, SignatureException {
		String localKeycloakURL = "https://localhost:61001";
		String fakeAccessToken = "sdbfhijdjknflksfdfgsdfgdssfgdf.gfjhojosedfoidfgjoi.fhud9gsiojefopk";
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", fakeAccessToken).build();
		RequestSpecification request = RestAssured.given();
		if (requestBody != null)
			request.body(requestBody);
		switch (requestMethod.toString()) {
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
		CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(keystorePath, password, truststorePath, password,
				SecurityConfig.realmName,localKeycloakURL);
		String access = adapter.getAccessToken("customer", password, SecurityConfig.clientId,
				SecurityConfig.clientSecret);
		String accessTokenVerified = null;
		adapter.verify(access);
		accessTokenVerified = access;
		String accessTokenWithCustomerRole = accessTokenVerified;
		RestAssured.requestSpecification = new RequestSpecBuilder().addHeader("Token", accessTokenWithCustomerRole)
				.build();
		request = RestAssured.given();
		if (requestBody != null)
			request.body(requestBody);
		switch (requestMethod.toString()) {
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

}
