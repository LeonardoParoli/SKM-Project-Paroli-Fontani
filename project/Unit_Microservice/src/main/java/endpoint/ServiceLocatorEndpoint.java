package endpoint;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import businessLogic.dto.ServiceLocatorDTO;
import security.CocoaKeycloakAdapter;
import security.SecurityConfig;

@RequestScoped
@Path("serviceLocator")
public class ServiceLocatorEndpoint {
	
	@GET
    @Path("/get/keycloakServiceLocation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeycloakLocation(@HeaderParam("Token") String token) {
		try {
			CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(SecurityConfig.realmName,SecurityConfig.keycloakURL, SecurityConfig.storePassword , SecurityConfig.storePassword);
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
        return Response.ok(new ServiceLocatorDTO(SecurityConfig.keycloakURL).toJson()).build();
    }
	
    @POST
    @Path("/post/keycloakServiceLocation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeKeycloakLocation(@HeaderParam("Token") String token, ServiceLocatorDTO serviceLocator) {
    	try {
    		CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(SecurityConfig.realmName,serviceLocator.getLocation(), SecurityConfig.storePassword , SecurityConfig.storePassword);
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
			String newLocation = serviceLocator.getLocation();
			SecurityConfig.keycloakURL = newLocation;
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
        return Response.ok(new ServiceLocatorDTO(SecurityConfig.keycloakURL).toJson()).build();
    }
}
