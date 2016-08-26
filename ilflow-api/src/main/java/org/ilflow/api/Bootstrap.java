package org.ilflow.api;

import io.swagger.config.ScannerFactory;
import io.swagger.models.*;
import io.swagger.jaxrs.config.ReflectiveJaxrsScanner;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.auth.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Bootstrap extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage("org.ilflow.api.handler");
        ScannerFactory.setScanner(scanner);
        Info info = new Info()
                .title("IlFlow Java Project")
                .version("1.0.0")
                .description("")
                .termsOfService("http://ilflow.org/terms/")
                .contact(new Contact()
                        .email("apiteam@ilflow.org"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        Swagger swagger = new Swagger()
                .info(info)
                .host("")
                .basePath("/api/v1");
        swagger.securityDefinition("api_key", new ApiKeyAuthDefinition("api_key", In.HEADER));
        swagger.tag(new Tag()
                .name("repository")
                .description("Repository operations"));
        swagger.tag(new Tag()
                .name("server")
                .description("Server operations"));

        new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
    }
}
