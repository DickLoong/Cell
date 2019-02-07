package org.mircostem.test.router;

import com.alibaba.fastjson.JSONObject;
import org.mircostem.bootstrap.BootstrapManager;
import org.mircostem.test.router.impl.TestA.TestServiceImpl;
import org.mircostem.test.router.service.TestA.ITestService;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RouterTest {
    public static void main(String... args){
        Set<String> controllingRouterServiceImplPackageNameSet = new HashSet<>();
        controllingRouterServiceImplPackageNameSet.add("org.mircostem.test.router.impl.TestA");
        controllingRouterServiceImplPackageNameSet.add("org.mircostem.test.router.impl.TestB");
        BootstrapManager.init("org.mircostem.test.router.impl",controllingRouterServiceImplPackageNameSet);
        ITestService serviceA = (ITestService)BootstrapManager.getService(TestServiceImpl.class);
        serviceA.echoTest();
        org.mircostem.test.router.service.TestB.ITestService serviceB = (org.mircostem.test.router.service.TestB.ITestService)BootstrapManager.getService(org.mircostem.test.router.impl.TestB.TestServiceImpl.class);
        serviceB.echoTest();
    }
}
