package endpoint;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import businessLogic.dto.ConversionRatioDTO;
import domainModel.ConversionRatio;
import domainModel.Unit;
import security.CocoaKeycloakAdapter;
import security.SecurityConfig;
import service.ConversionRatioDAO;
import service.UnitDAO;

@RequestScoped
@Path("conversionRatio")
public class ConversionEndpoint {
	private CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(SecurityConfig.realmName,SecurityConfig.keycloakURL, SecurityConfig.storePassword , SecurityConfig.storePassword);
	
	@Inject
	ConversionRatioDAO conversionRatioDAO;
	
	@Inject
	UnitDAO unitDAO;
	
	@GET
	@Path("/ping")
	public Response ping(@HeaderParam("Token") String token) {
		try {
			adapter.verify(token);
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
		return Response.ok().entity("Service Online").build();
	}
	
	@GET
	@Path("/get/convert/{startUnitName}/{endUnitName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response convert(@HeaderParam("Token") String token, @PathParam("startUnitName") String first, @PathParam("endUnitName") String second) {
		Unit firstUnit;
		Unit secondUnit;
		JsonObject ratioValue;
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			firstUnit = unitDAO.getUnit(first);
			secondUnit = unitDAO.getUnit(second);
			if(firstUnit == null || secondUnit == null) {
			    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
			    return responseBuilder.build();
			}
			ConversionRatio ratio = conversionRatioDAO.getConversionRatio(firstUnit.getId(), secondUnit.getId());
			if(ratio == null) {
			    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
			    return responseBuilder.build();
			}
			ratioValue = new JsonObject();
			ratioValue.addProperty("conversionRatio", ratio.getRatio());
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
		return Response.ok(ratioValue).build();
	}
	
	@GET
	@Path("/get/allConversionRatios")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllConversionRatios(@HeaderParam("Token") String token){
		JsonArray jsonArray; 
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			jsonArray = new JsonArray();
			for(ConversionRatio conversion : conversionRatioDAO.getAll()) {
				jsonArray.add(new ConversionRatioDTO(conversion).toJSON());
			}
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
		return Response.ok(jsonArray).build();
	}
	
	@GET
	@Path("/get/conversionRatioById/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConversionRatio(@HeaderParam("Token") String token, @PathParam("id") Long id) {
		ConversionRatio conversionRatio;
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			conversionRatio = conversionRatioDAO.getConversionRatio(id);
			if (conversionRatio == null) {
			    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
			    return responseBuilder.build();
			}
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
		return Response.ok(new ConversionRatioDTO(conversionRatio).toJSON()).build();
	}
	
	@GET
	@Path("/get/conversionRatioListByUnitStart/{startUnitId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConversionRatiosByStartUnit(@HeaderParam("Token") String token, @PathParam("startUnitId") Long startUnitId){
		JsonArray jsonArray;
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			jsonArray = new JsonArray();
			for (ConversionRatio conversionRatio : conversionRatioDAO.getConversionRatioListByStartUnitId(startUnitId)) {
				jsonArray.add(new ConversionRatioDTO(conversionRatio).toJSON());
			}
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
		return Response.ok(jsonArray).build();
	}
	
	@GET
	@Path("/get/conversionRatioListByUnitEnd/{endUnitId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConversionRatiosByEndUnit(@HeaderParam("Token") String token, @PathParam("endUnitId") Long endUnitId){
		JsonArray jsonArray = new JsonArray();
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			jsonArray = new JsonArray();
			for (ConversionRatio conversionRatio : conversionRatioDAO.getConversionRatioListByEndUnitId(endUnitId)) {
				jsonArray.add(new ConversionRatioDTO(conversionRatio).toJSON());
			}
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
		return Response.ok(jsonArray).build();

	}
	
	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postConversionRatio(@HeaderParam("Token") String token, ConversionRatioDTO conversionRatioDTO) {
		boolean found;
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			found = false;
			Unit unit1 = unitDAO.getUnit(conversionRatioDTO.getStartUnitName());
			Unit unit2 = unitDAO.getUnit(conversionRatioDTO.getEndUnitName());
			if(unit1 != null && unit2 != null) {
				List<ConversionRatio> list = conversionRatioDAO.getConversionRatioListByStartUnitId(unit1.getId());
				for(ConversionRatio conversion : list) {
					if(conversion.getSecond().getName().equals(conversionRatioDTO.getEndUnitName())) {
						found = true;
					}
				}
				if(!found) {
					ConversionRatio conversionRatio = new ConversionRatio(unit1,unit2,Double.valueOf(conversionRatioDTO.getRatio()));
					conversionRatioDAO.save(conversionRatio);
					return Response.ok(new ConversionRatioDTO(conversionRatio).toJSON()).build();
				}
			}
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
	    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
	    return responseBuilder.build();
	}
	
	@DELETE
	@Path("/delete/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUnit(@HeaderParam("Token") String token, @PathParam("id") Long id) {
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			ConversionRatio conversionRatio = conversionRatioDAO.getConversionRatio(id);
			if(conversionRatio != null){
				conversionRatioDAO.delete(id);
				return Response.ok(new ConversionRatioDTO(conversionRatio).toJSON()).build();
			}
		}catch(Exception e){
		    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		    return responseBuilder.build();
		}
	    Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
	    return responseBuilder.build();
	}
}
