package com.docutrace.tracking_service.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiRedirectConfig {

    @GetMapping({"/swagger-ui", "/swagger"})
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/")
    public String redirectRootToDocs() {
        return "redirect:/swagger-ui.html";
    }
}
