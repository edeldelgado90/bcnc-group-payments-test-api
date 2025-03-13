package com.bcnc.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PaymentsTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentsTestApplication.class, args);
  }
}
