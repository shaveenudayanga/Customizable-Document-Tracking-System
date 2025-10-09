// Application entrypoint for the Document Service Spring Boot app
package com.docutrace.document_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.docutrace.document_service.config.StorageProperties;
import com.docutrace.document_service.config.IntegrationProperties;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({StorageProperties.class, IntegrationProperties.class})
public class DocumentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentServiceApplication.class, args);
	}

}
