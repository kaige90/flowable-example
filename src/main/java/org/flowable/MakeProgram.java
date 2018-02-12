package org.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chenkai on 2018/2/12.
 */
public class MakeProgram implements JavaDelegate{
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("恭喜你，节目{}审核通过～",execution.getVariable("programName"));
    }
}
