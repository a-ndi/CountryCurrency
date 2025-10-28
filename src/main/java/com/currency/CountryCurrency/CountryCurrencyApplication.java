package com.currency.CountryCurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CountryCurrencyApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        System.out.println("Headless mode: " + java.awt.GraphicsEnvironment.isHeadless());
        SpringApplication.run(CountryCurrencyApplication.class, args);
    }

}
