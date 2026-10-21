package cl.grupo3.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de Spring Boot. Escanea todas las capas bajo cl.grupo3.lab1 (controller, service, repository, ...). */
@SpringBootApplication
public class Lab1Application {

	public static void main(String[] args) {
		SpringApplication.run(Lab1Application.class, args);
	}

}
