package org.flowable;

import com.alibaba.fastjson.JSON;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * {@code HolidayRequest} is responsible for the creation of Flowable
 * Process Engine and for loading a simple HolidayRequest process. .
 * <p/>
 *
 * @since 7/24/17
 */
@SuppressWarnings({"squid:S106", "squid:S3776"})
public class ProgramRequest{

    private Logger logger = LoggerFactory.getLogger(getClass());

    //creates the Process Engine using a memory-based h2 embedded database
    private ProcessEngineConfiguration cfg =
            new StandaloneProcessEngineConfiguration()
                    .setJdbcUrl("jdbc:mysql://mysql.nxey.com:3306/flowable?characterEncoding=UTF-8")
                    .setJdbcUsername("flowable")
                    .setJdbcPassword("flowable")
                    .setJdbcDriver("com.mysql.jdbc.Driver")
                    .setDatabaseSchemaUpdate(
                            ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

    private ProcessInstance processInstance;

    /**
     * 添加申请
     */
    public void addProgram(String programName){
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println(
                "ProcessEngine [" + pName + "] Version: [" + ver + "]");

        RepositoryService
                repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("program_request.bpmn20.xml")
                .deploy();

        ProcessDefinition
                processDefinition =
                repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId()).singleResult();
        System.out.println(
                "Found process definition ["
                        + processDefinition.getName() + "] with id ["
                        + processDefinition.getId() + "]");

        logger.info("需要审核的节目名称是{}",programName);
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<>();
        variables.put("programName", programName);
        processInstance =
                runtimeService.startProcessInstanceByKey("programRequest",
                        variables);
    }

    /**
     * 学校审核
     */
    public void schoolAdmin(String taskId, boolean approved){
        TaskService taskService = cfg.buildProcessEngine().getTaskService();
        System.out.println("Which task would you like to complete?");
        Map<String, Object> processVariables =
                taskService.getVariables(taskId);
        System.out.println(processVariables.get("programName") + " 节目需要审核 "+"，批准吗?");

        Map<String, Object> variables = new HashMap<>();
        variables.put("approval", approved);

        taskService.complete(taskId, variables);
    }

    public void listTasks(String taskName){
        TaskService taskService = cfg.buildProcessEngine().getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(taskName).list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            logger.info((i + 1) + ") {} : {}", task.getName(),task.getId());
        }
    }

    /**
     * 公司审核
     */
    public void companyAdmin(String taskId, boolean approved){
        System.out.println("Which task would you like to complete?");
        TaskService taskService = cfg.buildProcessEngine().getTaskService();
        Map<String, Object> processVariables = taskService.getVariables(taskId);
        System.out.println(processVariables.get("programName") + " 节目需要审核 "+"，批准吗?");


        Map<String, Object> variables = new HashMap<>();
        variables.put("approval", approved);

        taskService.complete(taskId, variables);
    }

    public void getHistory(){
        HistoryService historyService = cfg.buildProcessEngine().getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println("activity:"+ activity.getActivityName()+":"+activity.getActivityId());
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds");
        }

    }

}
