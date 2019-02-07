package org.mircostem.test.router.impl.TestA;

import org.mircostem.test.router.service.TestA.ITestService;

public class TestServiceImpl implements ITestService {
    @Override
    public void echoTest() {
        System.out.println(this.getClass().getName());
    }
}
