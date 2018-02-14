package ru.mdimension.wrs.lib.configuration.swagger;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import io.swagger.annotations.Authorization;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Header;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Date;

import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Created by vkokurin on 18.02.2015.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

    private static final String DEFAULT_URL_PREFIX = "/.*";

    private RelaxedPropertyResolver propertyResolver;


    @Override
    public void setEnvironment(Environment environment) {
        this.propertyResolver = new RelaxedPropertyResolver(environment, "swagger.");
    }

    @Bean
    public Docket swaggerSpringfoxDocket() {
        StopWatch watch = new StopWatch();
        watch.start();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
						.groupName("swagger")
						.pathMapping("/")
            .apiInfo(apiInfo())
            .genericModelSubstitutes(ResponseEntity.class)
            .forCodeGeneration(true)
            .genericModelSubstitutes(ResponseEntity.class)
            .directModelSubstitute(org.joda.time.LocalDate.class, String.class)
            .directModelSubstitute(org.joda.time.DateTime.class, Date.class)
						.securitySchemes(Lists.newArrayList(apiKey()))
						.securityContexts(Lists.newArrayList(securityContext()))
            .select()
						.apis(not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
						.apis(not(RequestHandlerSelectors.basePackage("org.springframework.cloud")))
						.apis(not(RequestHandlerSelectors.basePackage("org.springframework.data.rest.webmvc")))
						.paths(not(regex("/debug/.*")))
						.paths(regex(DEFAULT_URL_PREFIX))
            .build();

        watch.stop();
        log.info("Swagger configured in {} ms", watch.getTotalTimeMillis());

        return docket;
    }

		private SecurityContext securityContext() {
				return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(regex("/reports")).build();
		}

		private List<SecurityReference> defaultAuth() {
				AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessNothing");
				AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
				authorizationScopes[0] = authorizationScope;
				return Lists.newArrayList(new SecurityReference("mykey", authorizationScopes));
		}

		private ApiKey apiKey (){
    		return new ApiKey("token", "x-auth-token", "header");
		}

    private ApiInfo apiInfo() {
        return new ApiInfo(propertyResolver.getProperty("title"), propertyResolver.getProperty("description"), null, null, null, null, null);
    }
}
