package com.example.multimodule.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.example.multimodule")
@RestController
public class JavaTorrentCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaTorrentCenterApplication.class, args);
	}
}
