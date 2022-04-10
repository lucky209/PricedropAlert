package com.pricedrop.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PricedropAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(PricedropAlertApplication.class, args);
	}

}
