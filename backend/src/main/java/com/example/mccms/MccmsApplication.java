package com.example.mccms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the MCCMS Backend API.
 */
@SpringBootApplication
@EnableScheduling
public class MccmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MccmsApplication.class, args);
	}

}