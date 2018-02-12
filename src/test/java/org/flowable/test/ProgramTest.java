package org.flowable.test;

import org.flowable.ProgramRequest;
import org.junit.Test;

/**
 * Created by chenkai on 2018/2/12.
 */
public class ProgramTest {
    private ProgramRequest request = new ProgramRequest();

    @Test
    public void addTest(){
        request.addProgram("笑傲江湖");
    }

    @Test
    public void listTasks(){
        request.listTasks("companyManager");
    }

    @Test
    public void schoolApproved(){
        request.schoolAdmin("45010",false);
    }

    @Test
    public void companyApproved(){
        request.companyAdmin("40004",true);
    }

}
