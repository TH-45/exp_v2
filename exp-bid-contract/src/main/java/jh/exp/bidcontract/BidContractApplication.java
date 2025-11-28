package jh.exp.bidcontract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BidContractApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidContractApplication.class, args);
    }
}


