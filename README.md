# Collection of external task worker examples
A collection of external-task-workers in different languages and different concepts.

The external task pattern is very useful. It can be easily used with the REST Api or the Java API. But the most difficult question: how can it be invoked?

Here is a collection of task workers with different implementations

## [Task worker as Spring-Boot application](external-task-worker-spring-boot)
Use a simple spring-boot application to implement a worker as a scheduled bean. It talks to the process engine by Rest API.

## [Task worker as a Service Task](external-task-worker-scheduled-by-process)
It sounds a bit odd, but a customer asked for this.

They have a problem, that the thread pool and async service task is too fast for their backend system. Using an external task is one option to throttle the throughput beyond finetuning the thead pool for job execution.

And they don't want additional operational effort to check if the worker is still running. And the worker should be invoked regularily.

So you can us a process with a timer start event and a single service tasks, that fetches external services, call the service in a loop, one by one and completes or mark the external task as failed if some problem arouse.


