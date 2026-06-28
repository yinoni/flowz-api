package com.flowzapi.flowz_api_builder;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.flowzapi.flowz_api_builder.controller.FlowController;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.repos.ProjectRepository;
import com.flowzapi.flowz_api_builder.service.FlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FlowServiceTest {

    @Mock
    private FlowRepository flowRepository;

    @InjectMocks
    private FlowService flowService;

    private String targetProjectId;

    @BeforeEach
    void setUp() {

        targetProjectId = UUID.randomUUID().toString();
    }

    @Test
    void createMockupFlow_ShouldCreateAndSaveValidFlow() {
        when(flowRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Flow savedFlow = flowService.createMockupFlow(targetProjectId, "123456");

        assertNotNull(savedFlow, "The saved flow should not be null");

        assertEquals("LOGIN-DEMO-FLOW", savedFlow.getFlowName());
        assertEquals(targetProjectId, savedFlow.getProjectId());
        assertEquals("https://dummyjson.com", savedFlow.getGlobalURL());

        assertEquals(3, savedFlow.getSteps().size());

        Step firstStep = savedFlow.getSteps().get(0);
        assertEquals("LOGIN-DEMO-STEP", firstStep.getTitle());
        assertEquals(80, firstStep.getPosition().getX());
        assertEquals(80, firstStep.getPosition().getY());

        Step secondStep = savedFlow.getSteps().get(1);
        assertEquals("ADD-POST-DEMO-STEP", secondStep.getTitle());
        assertEquals(520, secondStep.getPosition().getX());

        Step thirdStep = savedFlow.getSteps().get(2);
        assertEquals("GET-USER-POSTS-DEMO-STEP", thirdStep.getTitle());
        assertEquals(960, thirdStep.getPosition().getX());

        verify(flowRepository, times(1)).save(any(Flow.class));
    }
}
