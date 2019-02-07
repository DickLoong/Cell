package org.mircostem.bootstrap;

import org.apache.commons.lang3.StringUtils;
import org.mircostem.loader.ModuleClassLoader;
import org.mircostem.loader.RouterClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BootstrapManager {
    private static RouterClassLoader routerClassLoader = new RouterClassLoader();
    private static Map<String, ModuleClassLoader> moduleClassLoaderMap = new ConcurrentHashMap<>();
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static Map<String, Long> newInstanceLock = new ConcurrentHashMap<>();
    private static Set<String> controllingServiceImplPackageSet = new HashSet<>();

    public static void init(String routerServiceImplPackageName, Set<String> ControllingRouterServiceImplPackageName) {
        try {
            routerClassLoader.setRouterServiceImplPackage(routerServiceImplPackageName);
            Set<String> collect = ControllingRouterServiceImplPackageName;
            controllingServiceImplPackageSet.addAll(collect);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static <T> T getService(Class<T> serviceClass) {
        String clazzName = serviceClass.getName();
        if (!serviceMap.containsKey(clazzName)) {
            ModuleClassLoader moduleClassLoader = null;
            String routerServiceImplPackage = routerClassLoader.getRouterServiceImplPackage();
            if (StringUtils.contains(clazzName, routerServiceImplPackage)) {
                for (String controllingServiceImplPackage : controllingServiceImplPackageSet) {
                    if (StringUtils.contains(clazzName, controllingServiceImplPackage)) {
                        if (!moduleClassLoaderMap.containsKey(controllingServiceImplPackage)) {
                            moduleClassLoader = new ModuleClassLoader(routerClassLoader);
                            moduleClassLoader.setRouterServiceImplPackage(routerServiceImplPackage);
                            moduleClassLoader.setControllingServiceImplPackage(controllingServiceImplPackage);
                            moduleClassLoaderMap.put(controllingServiceImplPackage, moduleClassLoader);
                        }
                        moduleClassLoader = moduleClassLoaderMap.get(controllingServiceImplPackage);
                        break;
                    }
                }
                if(moduleClassLoader == null){
                    return null;
                }
                try {
                    if (!newInstanceLock.containsKey(clazzName)) {
                        long lock = ThreadLocalRandom.current().nextLong();
                        newInstanceLock.put(clazzName, lock);
                        Long injectingLock = newInstanceLock.get(clazzName);
                        if (lock == injectingLock) {
                            Class<?> aClass = moduleClassLoader.loadClass(clazzName);
                            Object o = aClass.getDeclaredConstructor().newInstance();
                            serviceMap.put(clazzName, o);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        Object o = serviceMap.get(clazzName);
        return (T) o;
    }
}
