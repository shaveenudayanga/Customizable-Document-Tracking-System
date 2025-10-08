package com.docutrace.workflow_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"document-service.base-url=http://localhost:8082"
})
class WorkflowServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
