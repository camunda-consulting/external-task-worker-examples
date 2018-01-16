package com.camunda.consulting.external_task_worker_spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExternalWorkerApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(ExternalWorkerApplication.class, args);
  }
} 