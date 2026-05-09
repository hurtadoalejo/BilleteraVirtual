package com.proyectofinal.billeteravirtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BilleteravirtualApplication {

	public static void main(String[] args) {
		SpringApplication.run(BilleteravirtualApplication.class, args);
	}

}