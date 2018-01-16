package com.camunda.consulting.external_task_worker_scheduled_by_process;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.variable.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalWorkerDelegate implements JavaDelegate {
  
  private static final String EXTERNAL_WORKER_ID = "externalWorkerDelegate";
  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalWorkerDelegate.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // check for work
    ExternalTaskService externalTaskService = execution.getProcessEngineServices().getExternalTaskService();
    
    List<LockedExternalTask> lockedTasks = externalTaskService.fetchAndLock(5, EXTERNAL_WORKER_ID).topic("example-topic", 300000).execute();
    
    LOGGER.info("{} tasks locked", lockedTasks.size());
    // do the work
    for (LockedExternalTask lockedTask : lockedTasks) {
      // do some random calculation
      double random = Math.random();
      long duration = (long) (random * 5000);
      
      LOGGER.info("work on task {} for {} milliseconds", lockedTask.getId(), duration);
      Thread.sleep(duration);
      if (duration % 5 == 0) {
        // task could not be completed, release it
        LOGGER.info("unlock task {} for number {}", lockedTask.getId(), duration);
        externalTaskService.unlock(lockedTask.getId());
      } else  if (duration % 7 == 0) {
        // task failed, mark it with incident
        LOGGER.info("failed external service for task {}", lockedTask.getId());
        externalTaskService.handleFailure(lockedTask.getId(), 
            EXTERNAL_WORKER_ID, 
            "modulo 7 equals 0", 
            "" + duration + " " + duration % 7, 
            0, 
            0);
      } else {
        // task completed successfully
        LOGGER.info("external service for task {} completed", lockedTask.getId());
        Map<String, Object> variables = Variables.createVariables().putValue("calculated value", duration);
        externalTaskService.complete(lockedTask.getId(), EXTERNAL_WORKER_ID, variables);
      }
    }
  }

}
