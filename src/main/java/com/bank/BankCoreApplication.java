package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.bank",
		"com.bank.deposit",
		"com.bank.external"
})
public class BankCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankCoreApplication.class, args);
	}

}
