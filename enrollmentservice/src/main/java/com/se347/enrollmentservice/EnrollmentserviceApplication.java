package com.se347.enrollmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import java.util.TimeZone;

@EnableScheduling
@EnableRabbit
@SpringBootApplication
public class EnrollmentserviceApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(EnrollmentserviceApplication.class, args);
	}

}
