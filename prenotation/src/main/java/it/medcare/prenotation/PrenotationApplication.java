package it.medcare.prenotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PrenotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrenotationApplication.class, args);
	}

}
