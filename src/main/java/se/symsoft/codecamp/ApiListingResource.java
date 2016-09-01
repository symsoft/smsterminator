/*
 * Copyright Symsoft AB 1996-2015. All Rights Reserved.
 */
package se.symsoft.codecamp;

import io.swagger.annotations.ApiOperation;
import io.swagger.config.FilterFactory;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.config.DefaultReaderConfig;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/api")
public class ApiListingResource {
    static boolean initialized = false;

    @Context
    ServletContext servletContext;

    protected synchronized Swagger scan (SmsTerminatorService app, ServletConfig sc) {
        Swagger swagger = app.getSwagger();
        JaxrsScanner scanner = new DefaultJaxrsScanner();

        SwaggerSerializers.setPrettyPrint(scanner.getPrettyPrint());

        Set<Class<?>> classes = scanner.classesFromContext(app, sc);
        if (classes != null) {
            final DefaultReaderConfig rc = new DefaultReaderConfig();
            rc.setScanAllResources(true);

            Reader reader = new Reader(swagger, rc);
            swagger = reader.read(classes);
            /* todo
            SwaggerConfig configurator = new WebXMLReader(sc);
            configurator.configure(swagger);
            */
            app.setSwagger(swagger);
        }
        initialized = true;
        return swagger;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("swagger.json")
    @ApiOperation(value = "The swagger definition in JSON", hidden = true)
    public Response getListingJson(
            @Context Application app,
            @Context ServletConfig sc,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        Swagger swagger = ((SmsTerminatorService)app).getSwagger();
        if (!initialized) {
            swagger = scan((SmsTerminatorService)app, sc);
        }
        if (swagger != null) {
            SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
            if (filterImpl != null) {
                SpecFilter f = new SpecFilter();
                swagger = f.filter(swagger,
                        filterImpl,
                        getQueryParams(uriInfo.getQueryParameters()),
                        getCookies(headers),
                        getHeaders(headers));
            }
            // Note: this assumes we have a swagger->json BodyWriter
            return Response.ok().entity(swagger).build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Produces("application/yaml")
    @Path("/swagger.yaml")
    @ApiOperation(value = "The swagger definition in YAML", hidden = true)
    public Response getListingYaml(
            @Context Application app,
            @Context ServletConfig sc,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo) {
        Swagger swagger = ((SmsTerminatorService)app).getSwagger();
        if (!initialized) {
            swagger = scan((SmsTerminatorService)app, sc);
        }
        try {
            if (swagger != null) {
                SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
                if(filterImpl != null) {
                    SpecFilter f = new SpecFilter();
                    swagger = f.filter(swagger,
                            filterImpl,
                            getQueryParams(uriInfo.getQueryParameters()),
                            getCookies(headers),
                            getHeaders(headers));
                }

                String yaml = Yaml.mapper().writeValueAsString(swagger);
                String[] parts = yaml.split("\n");
                StringBuilder b = new StringBuilder();
                for (String part : parts) {
                    b.append(part);
                    b.append("\n");
                }
                return Response.ok().entity(b.toString()).type("text/plain").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(404).build();
    }

    protected Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
        Map<String, List<String>> output = new HashMap<>();
        if (params != null) {
            for (String key: params.keySet()) {
                List<String> values = params.get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    protected Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap<>();
        if (headers != null) {
            for (String key: headers.getCookies().keySet()) {
                Cookie cookie = headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            }
        }
        return output;
    }


    protected Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap<>();
        if (headers != null) {
            for (String key: headers.getRequestHeaders().keySet()) {
                List<String> values = headers.getRequestHeaders().get(key);
                output.put(key, values);
            }
        }
        return output;
    }
}
