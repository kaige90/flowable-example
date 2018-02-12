package org.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;


/**
 * {@code CallExternalSystemDelegate} is a Java delegate representig the
 * external system.
 * <p/>
 *
 * @since 7/24/17
 */
@SuppressWarnings({"squid:S106"})
public class CallExternalSystemDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        //Retrieves a process variable, 'employee', and prints it out in the console.
        System.out.println("Calling the external system for employee "
                + execution.getVariable("employee"));
    }
}
