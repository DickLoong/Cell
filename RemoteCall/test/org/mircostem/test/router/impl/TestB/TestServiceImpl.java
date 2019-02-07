package org.mircostem.test.router.impl.TestB;

import org.mircostem.test.router.service.TestB.ITestService;

public class TestServiceImpl implements ITestService {
    @Override
    public void echoTest() {
        System.out.println(this.getClass().getName());
        try {
            Class.forName("org.mircostem.test.router.impl.TestA.TestServiceImpl");
        }catch (ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
        org.mircostem.test.router.service.TestA.ITestService testService = new org.mircostem.test.router.impl.TestA.TestServiceImpl();
        testService.echoTest();
    }
}
