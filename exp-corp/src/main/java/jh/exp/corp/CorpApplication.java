package jh.exp.corp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CorpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorpApplication.class, args);
    }
}


