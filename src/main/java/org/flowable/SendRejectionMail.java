package org.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * Created by chenkai on 2018/2/11.
 */
public class SendRejectionMail implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        System.out.println(execution.getVariable("programName")+"节目审核被拒绝！");
    }
}
