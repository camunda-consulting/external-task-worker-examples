package com.camunda.consulting.external_task_worker_spring_boot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ExternalWorker {
  
  public static final String WORKER_ID = "springBootFulfillmentWorker";
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalWorker.class);

  @Scheduled(fixedRate = 30000)
  public void fetchExternalTask() throws IOException, InterruptedException {
    LOGGER.info("Hello again");

    final ObjectMapper mapper = new ObjectMapper();

    final ObjectNode topic = mapper.createObjectNode();
    topic.put("topicName", "example-topic");
    topic.put("lockDuration", 300_000);
    
    final ArrayNode topics = mapper.createArrayNode();
    topics.add(topic);

    final ObjectNode root = mapper.createObjectNode();
    root.put("workerId", WORKER_ID);
    root.put("maxTasks", 3);
    root.set("topics", topics);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

    // fetch and lock 3 tasks for 5 minutes
    HttpEntity<String> entity = new HttpEntity<String>(//
        mapper.writeValueAsString(root), // here is the JSON body
        headers);
    String result = new RestTemplate().postForObject("http://localhost:8080/engine-rest/external-task/fetchAndLock",
        entity, String.class);

    JsonNode answer = mapper.readTree(result);

    for (JsonNode lockedTask : answer) {
      String taskId = lockedTask.get("id").asText();
      // Do some work
      double random = Math.random();
      long duration = (long) (random * 5000);
      
      LOGGER.info("work on task {} for {} milliseconds", taskId, duration);
      Thread.sleep(duration);

      LOGGER.info("task {} finished calculation", taskId);
      if (duration % 5 == 0) {
        // task could not be completed, release it
        LOGGER.info("duration is {}, release the task {}", duration, taskId);
        entity = new HttpEntity<String>("", headers);
        ResponseEntity<Object> unlockResponse = new RestTemplate()
            .postForEntity("http://localhost:8080/engine-rest/external-task/" + taskId + "/unlock", entity, null);
        LOGGER.info("status code for unlock: {}", unlockResponse.getStatusCodeValue());
      } else if (duration % 7 == 0) {
        // task failed, mark it with incident
        LOGGER.info("duration is {}, create incident for task {}", duration, taskId);
        ObjectNode failureRoot = mapper.createObjectNode();
        failureRoot.put("workerId", WORKER_ID);
        failureRoot.put("errorMessage", "duration % 7 == 0: " + duration);
        failureRoot.put("errorDetails", duration);
        failureRoot.put("retries", 0);
        failureRoot.put("retryTimeout", 0);
        
        entity = new HttpEntity<String>(mapper.writeValueAsString(failureRoot), headers);
        ResponseEntity<Object> failureResponse = new RestTemplate().postForEntity("http://localhost:8080/engine-rest/external-task/" + taskId + "/failure", entity, null);
        LOGGER.info("status code for failure: {}", failureResponse.getStatusCodeValue());
      } else {
        // complete the task
        LOGGER.info("complete task {}", taskId);
        ObjectNode resultVariable = mapper.createObjectNode();
        resultVariable.put("value", duration);
        ObjectNode variables = mapper.createObjectNode();
        variables.set("calculated value", resultVariable);
        ObjectNode completeRoot = mapper.createObjectNode();
        completeRoot.put("workerId", WORKER_ID);
        completeRoot.set("variables", variables);
        
        entity = new HttpEntity<String>(mapper.writeValueAsString(completeRoot), headers);
        ResponseEntity<Object> completeResponse = new RestTemplate()
            .postForEntity("http://localhost:8080/engine-rest/external-task/" + taskId + "/complete", entity, null);
        LOGGER.info("status code for completion: {}", completeResponse.getStatusCodeValue());
      }
    }
  }

} 