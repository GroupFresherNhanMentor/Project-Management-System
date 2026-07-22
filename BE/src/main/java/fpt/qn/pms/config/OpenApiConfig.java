package fpt.qn.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Project Management System API").version("1.0.0")
                .description("Backend API documentation for Jira-like Project Management System"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi sprintApiGroup() {
        return GroupedOpenApi.builder()
                .group("sprints")
                .pathsToMatch("/api/projects/{projectId}/sprints/**")
                .packagesToScan("fpt.qn.pms.sprint")
                .build();
    }

    @Bean
    public GroupedOpenApi taskApiGroup() {
        return GroupedOpenApi.builder()
                .group("tasks")
                .pathsToMatch("/api/tasks/**")
                .packagesToScan("fpt.qn.pms.task")
                .build();
    }

    @Bean
    public GroupedOpenApi activityApiGroup() {
        return GroupedOpenApi.builder()
                .group("activities")
                .pathsToMatch("/api/tasks/{taskId}/activities/**", "/api/tasks/{taskId}/activities")
                .packagesToScan("fpt.qn.pms.activity")
                .build();
    }

    @Bean
    public GroupedOpenApi dashboardApiGroup() {
        return GroupedOpenApi.builder()
                .group("dashboard")
                .pathsToMatch("/api/dashboard/**")
                .packagesToScan("fpt.qn.pms.dashboard")
                .build();
    }
}
