package endpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;

import domainModel.ConversionRatio;
import domainModel.Unit;
import security.CocoaKeycloakAdapter;
import security.SecurityConfig;
import service.ConversionRatioDAO;
import service.UnitDAO;

@WebServlet("/hibernateTest")
public class demoServlet extends HttpServlet {
	
	private CocoaKeycloakAdapter adapter = new CocoaKeycloakAdapter(SecurityConfig.realmName,SecurityConfig.keycloakURL, SecurityConfig.storePassword , SecurityConfig.storePassword);
	private static final long serialVersionUID = 1L;
	static String PAGE_HEADER = "<html><head><title>Hello Hibernate</title></head><body>";
    static String PAGE_FOOTER = "</body></html>";
    
    @Inject
    UnitDAO unitDAO;
    @Inject
    ConversionRatioDAO conversionRatioDAO;
    
    
    public demoServlet() {
        super();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		String token = request.getHeader("Token");
		try {
			adapter.verify(token);
			adapter.verifyRole(token,"manager");
			resp.addHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'self'; form-action 'self'");
	        resp.addHeader("Strict-Transport-Security", "max-age=82600; includeSubDomains");
			resp.addHeader("X-Content-Type-Options", "nosniff");
			CacheControl cacheControl = new CacheControl();
		    cacheControl.setMaxAge(86400); 
		    cacheControl.setPrivate(true);
		    resp.addHeader(HttpHeaders.CACHE_CONTROL, cacheControl.toString());
		    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
			resp.setContentType("text/html");
			resp.getWriter().append("Served at: ").append(request.getContextPath());
	        PrintWriter writer = resp.getWriter();
	        writer.println(PAGE_HEADER);
	        unitDAO.save(new Unit("unit1"));
	        unitDAO.save(new Unit("unit2"));
	        unitDAO.save(new Unit("unit3"));
	        unitDAO.save(new Unit("unit4"));
	        ConversionRatio conversionRatio1= new ConversionRatio(unitDAO.getUnit("unit1"), unitDAO.getUnit("unit2"),3.0);
	        ConversionRatio conversionRatio2= new ConversionRatio(unitDAO.getUnit("unit3"), unitDAO.getUnit("unit4"),6.0);
	        ConversionRatio conversionRatio3= new ConversionRatio(unitDAO.getUnit("unit1"), unitDAO.getUnit("unit4"),4.0);
	        conversionRatioDAO.save(conversionRatio1);
	        conversionRatioDAO.save(conversionRatio2);
	        conversionRatioDAO.save(conversionRatio3);
	        writer.println("<h1>Unit List</h1>");
	        List<Unit> list = unitDAO.getAll();
	        writer.println("<ul>");
	        for (Unit unit:list)
	            writer.println("<li>" +unit.getName()+"</li>");
	        writer.println("</ul>");
	        writer.println(PAGE_FOOTER);
	        writer.close();
		}catch(Exception e){
			resp.addHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'self'; form-action 'self'");
	        resp.addHeader("Strict-Transport-Security", "max-age=82600; includeSubDomains");
			resp.addHeader("X-Content-Type-Options", "nosniff");
			CacheControl cacheControl = new CacheControl();
		    cacheControl.setMaxAge(86400); 
		    cacheControl.setPrivate(true);
		    resp.addHeader(HttpHeaders.CACHE_CONTROL, cacheControl.toString());
		    resp.addHeader("X-Frame-Options", "SAMEORIGIN");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "<html><body>Error Page</body></html>");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}