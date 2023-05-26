package security;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		responseContext.getHeaders().add("Content-Security-Policy", "default-src 'self'");
		responseContext.getHeaders().add("X-Content-Type-Options", "nosniff");
		CacheControl cacheControl = new CacheControl();
	    cacheControl.setMaxAge(86400); 
	    cacheControl.setPrivate(true);
	    responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, cacheControl.toString());
		responseContext.getHeaders().add("X-Frame-Options", "SAMEORIGIN");
		responseContext.getHeaders().add("Strict-Transport-Security", "max-age=82600; includeSubDomains");
	}
}
