package com.bhavesh.learn.cachepurge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CachePurgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CachePurgeApplication.class, args);
	}

}
