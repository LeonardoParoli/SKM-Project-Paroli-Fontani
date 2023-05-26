package endpoint;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.gson.JsonArray;

import businessLogic.dto.UnitDTO;
import domainModel.Unit;
import security.CocoaKeycloakAdapter;
import security.SecurityConfig;
import service.UnitDAO;

@RequestScoped
@Path("unit")
public class UnitEndpoint {
	private CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(SecurityConfig.realmName,
			SecurityConfig.keycloakURL, SecurityConfig.storePassword, SecurityConfig.storePassword);
	@Inject
	UnitDAO unitDAO;

	@GET
	@Path("/ping")
	public Response ping(@HeaderParam("Token") String token) {
		try {
			adapter.verify(token);
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
		return Response.ok().entity("Service online").build();
	}

	@GET
	@Path("/get/allUnits")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUnits(@HeaderParam("Token") String token) {
		JsonArray jsonArray = new JsonArray();
		try {
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
			for (Unit unit : unitDAO.getAll()) {
				jsonArray.add(new UnitDTO(unit).toJSON());
			}
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
		return Response.ok(jsonArray).build();
	}

	@GET
	@Path("/get/unitById/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUnitById(@HeaderParam("Token") String token, @PathParam("id") Long id) {
		try {
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
		Unit unit = unitDAO.getUnit(id);
		if (unit == null) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
		return Response.ok(new UnitDTO(unit).toJSON()).build();
	}

	@GET
	@Path("/get/unitByName/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUnitByName(@HeaderParam("Token") String token, @PathParam("name") String name) {
		try {
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
			name = name.replace("_", " ");
			Unit unit = unitDAO.getUnit(name);
			if (unit == null) {
				Response.ResponseBuilder responseBuilder = Response.status(500)
						.entity("<html><body>Error Page</body></html>");
				return responseBuilder.build();
			}
			return Response.ok(new UnitDTO(unit).toJSON()).build();
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
	}

	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUnit(@HeaderParam("Token") String token, UnitDTO unitDTO) {
		Unit unit;
		try {
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
			unit = new Unit(unitDTO.getName());
			if (unitDAO.getUnit(unit.getName()) == null) {
				unitDAO.save(unit);
			} else {
				throw new Exception("unit already present");
			}
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
		return Response.ok(new UnitDTO(unit).toJSON()).build();
	}

	@PUT
	@Path("/put/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putUnit(@HeaderParam("Token") String token, UnitDTO unitDTO, @PathParam("id") Long id) {
		try {
			adapter.verify(token);
			adapter.verifyRole(token, "manager");
			Unit unit = unitDAO.getUnit(id);
			if (unit != null) {
				unitDAO.update(id, unitDTO.getName());
				return Response.ok(new UnitDTO(unit).toJSON()).build();
			}
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
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
			adapter.verifyRole(token, "manager");
			Unit unit = unitDAO.getUnit(id);
			if (unit != null) {
				unitDAO.delete(id);
				return Response.ok(new UnitDTO(unit).toJSON()).build();
			}
		} catch (Exception e) {
			Response.ResponseBuilder responseBuilder = Response.status(500)
					.entity("<html><body>Error Page</body></html>");
			return responseBuilder.build();
		}
		Response.ResponseBuilder responseBuilder = Response.status(500).entity("<html><body>Error Page</body></html>");
		return responseBuilder.build();
	}
}
