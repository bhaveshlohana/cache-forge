package com.bhavesh.learn.cacheforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CacheForgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CacheForgeApplication.class, args);
	}

}
