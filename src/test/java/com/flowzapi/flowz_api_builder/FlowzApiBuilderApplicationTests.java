package com.flowzapi.flowz_api_builder;

import com.flowzapi.flowz_api_builder.service.FlowExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlowzApiBuilderApplicationTests {
    @Autowired
    private FlowExecutionService flowExecutionService;

	@Test
	void contextLoads() {
	}

    @Test
    void test_execute_steps_with_fallbacks(){
    }

}
