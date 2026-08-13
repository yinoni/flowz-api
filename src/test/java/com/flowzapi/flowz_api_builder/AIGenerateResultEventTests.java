package com.flowzapi.flowz_api_builder;

import com.flowzapi.flowz_api_builder.model.ai.AIGenerateResultEvent;
import com.flowzapi.flowz_api_builder.model.enums.Method;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


public class AIGenerateResultEventTests {
    @Test
    void deserializesSuccessContract() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/java/com/flowzapi/flowz_api_builder/contracts/generation_result.example.json");

        AIGenerateResultEvent event = mapper.readValue(file, AIGenerateResultEvent.class);

        assertThat(event.getVersion()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(AIGenerateResultEvent.Status.success);
        assertThat(event.getError()).isNull();
        assertThat(event.getFlow().getFlowName()).isEqualTo("Login and fetch user posts");
        assertThat(event.getFlow().getSteps()).hasSize(2);

        var login = event.getFlow().getSteps().get(0);
        assertThat(login.getHttpMethod()).isEqualTo(Method.POST);
        assertThat(login.getExtract()).containsEntry("jwtToken", "accessToken");

        var posts = event.getFlow().getSteps().get(1);
        assertThat(posts.getBody()).isNull();
        assertThat(posts.getPath()).isEqualTo("/users/{{userId}}/posts");
    }

    @Test
    void deserializesFailureContract() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/java/com/flowzapi/flowz_api_builder/contracts/generation_result.failed.example.json");

        AIGenerateResultEvent event = mapper.readValue(file, AIGenerateResultEvent.class);

        assertThat(event.getStatus()).isEqualTo(AIGenerateResultEvent.Status.failed);
        assertThat(event.getFlow()).isNull();
        assertThat(event.getError()).isNotNull();
    }
}
