package com.example.StudentsApplication;


import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/*
*
* Clase main, inicializa el programa, iniciando la aplicación Spring ("SpringApplication.run(StudentsApplication.class, args);")
*
* *NUEVO* Hace Enabel Caché para activar el uso e inicializacion de la caché
*
*/

@EnableCaching //@EnableCaching activa la abstracción de caché de Spring y permite usar anotaciones de cache
@SpringBootApplication
public class StudentsApplication {
	public static void main(String[] args) {
		SpringApplication.run(StudentsApplication.class, args);
	}
}
