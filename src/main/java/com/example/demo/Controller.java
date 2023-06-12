package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Value("${region}")
    String region;

    @Value("${env}")
    String env;

    @GetMapping("/")
    String index() {
        return "Hello World; region: " + region + "; env :" + env;
    }
}
