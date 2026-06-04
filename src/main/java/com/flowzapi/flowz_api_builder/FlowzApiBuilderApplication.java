package com.flowzapi.flowz_api_builder;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlowzApiBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowzApiBuilderApplication.class, args);
	}


}
