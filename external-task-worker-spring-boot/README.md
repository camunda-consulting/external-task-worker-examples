# External task worker scheduled with spring-boot

This is an example for an external task worker running in a spring-boot application.

The [application](src/main/java/com/camunda/consulting/external_task_worker_spring_boot/ExternalWorkerApplication.java) is annotated with `@EnableScheduling`.

It contains [a bean](src/main/java/com/camunda/consulting/external_task_worker_spring_boot/ExternalWorker.java), annotated with `@Scheduled` to [fetch](https://docs.camunda.org/manual/7.8/reference/rest/external-task/fetch/) regularily for new external tasks to lock them.

## How it works
The process instances with the external task run on Camunda BPM Platform on the localhost and are requested by REST api. The rest calls are made with the help of [Spring-Boot's RestTemplate](https://spring.io/guides/gs/consuming-rest/).

The worker simulates some work in waiting for a random aount of time (between 0 and 5000 milliseconds) and inspecting the random number. If it's modulo 5 equals 0, the tasks [is unlocked](https://docs.camunda.org/manual/7.8/reference/rest/external-task/post-unlock/), to be picked up later.

If the random number modulo 7 equals 0, the task is [marked with an incident](https://docs.camunda.org/manual/7.8/reference/rest/external-task/post-failure/).

In all other cases the task is [completed](https://docs.camunda.org/manual/7.8/reference/rest/external-task/post-complete/) with the random number as variable.

## How to test
Use the test process from the project [external-task-worker-scheduled-by-process](../external-task-worker-scheduled-by-process/src/test/resources/external-task-test-process.bpmn). Deploy it and start instances with the Rest API.

Check the cockpit of the platform to inspect the results.