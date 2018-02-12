package org.flowable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.alibaba.fastjson.JSON;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;

/**
 * {@code HolidayRequest} is responsible for the creation of Flowable
 * Process Engine and for loading a simple HolidayRequest process. .
 * <p/>
 *
 * @since 7/24/17
 */
@SuppressWarnings({"squid:S106", "squid:S3776"})
public class HolidayRequest {

    public static void main(String[] args) {
        //creates the Process Engine using a memory-based h2 embedded database
        ProcessEngineConfiguration cfg =
                new StandaloneProcessEngineConfiguration()
                        .setJdbcUrl("jdbc:mysql://mysql.nxey.com:3306/flowable?characterEncoding=UTF-8")
                        .setJdbcUsername("flowable")
                        .setJdbcPassword("flowable")
                        .setJdbcDriver("com.mysql.jdbc.Driver")
                        .setDatabaseSchemaUpdate(
                                ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();

        //displays the Process Engine configuration and Flowable version.
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println(
                "ProcessEngine [" + pName + "] Version: [" + ver + "]");

        //loads the supplied holiday-request.bpmn20.xml BPMN model and deploys
        // it to Activiti Process Engine.
        RepositoryService
                repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();

        //retrieves the deployed model, verififying the process definition is
        //known to the process engine. This is done by creating a new ProcessDefinitionQuery
        // object through the RepositoryService.
        ProcessDefinition
                processDefinition =
                repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId()).singleResult();
        System.out.println(
                "Found process definition ["
                        + processDefinition.getName() + "] with id ["
                        + processDefinition.getId() + "]");


        //To start the process instance, it requires initial process variables.
        // Typically, the information is collected through a form.
        // In this example, the java.util.Scanner class intakes data
        // from the command line:
        Scanner scanner = new Scanner(System.in);

        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();

        //A process instance is started through the RuntimeService.
        // The collected data is passed as a java.util.Map instance,
        // where the key is the identifier that will be used to retrieve the variables.
        // The process instance is started using a key, 'holidayRequest'.
        // This key matches the id attribute that is set in
        // the BPMN 2.0 XML file, 'holiday-request.bpmn20.xml'.
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);

        //starts an instance of the 'holidayRequest' process.
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest",
                        variables);

        //Once the process instance is started, an execution is created and
        // put in the start event. The execution follows the sequence flow
        // to the user task for the manager approval and executes the user
        // task behavior. This behavior will create a task in the database
        // that can be found using queries later on. A user task is in a wait
        // state and the engine will stop executing anything further,
        // returning the API call.

        //To get the actual task list, create a TaskQuery through the
        // TaskService and configure the query to only return the tasks
        // for the managers group:
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(
                "managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ") " + tasks.get(i).getName());
        }

        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.parseInt(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables =
                taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get(
                        "nrOfHolidays") + " of holidays. Do you approve this?");


        boolean approved = scanner.nextLine().equalsIgnoreCase("y");
        variables = new HashMap<>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);

        //Query Historical data (audit data) to detect how the organization works,
        //or detect bottlenecks etc.
        //query the HistoricService from the ProcessEngine for historical activities
        //for one particular process instance and only finished activities.
        //results are also sorted by end time, i.e., the order of execution
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println("activity:"+ JSON.toJSONString(activity));
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds");
        }

        scanner.close();
    }
}
