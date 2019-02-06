package org.mircostem.bootstrap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.mircostem.loader.ModuleClassLoader;
import org.mircostem.loader.RouterClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BootstrapManager {
    private static RouterClassLoader routerClassLoader = new RouterClassLoader();
    private static Map<String, ModuleClassLoader> moduleClassLoaderMap = new ConcurrentHashMap<>();
    private static Map<String,Object> serviceMap = new ConcurrentHashMap<>();
    private static Map<String,Long> newInstanceLock = new ConcurrentHashMap<>();
    private static Set<String> controllingServiceImplPackageSet = new HashSet<>();
    static{
        String routerServiceImplPackageName = System.getProperty("RouterServiceImplPackageName");
        routerClassLoader.setRouterServiceImplPackage(routerServiceImplPackageName);
        String controllingServiceImplPackageSetJsonString = System.getProperty("RouterServiceImplPackageName");
        JSONArray objects = JSONObject.parseArray(controllingServiceImplPackageSetJsonString);
        List<String> strings = objects.toJavaList(String.class);
        Set<String> collect = strings.stream().collect(Collectors.toSet());
        controllingServiceImplPackageSet.addAll(collect);
    }

    public static <T> T getService(Class<T> serviceClass){
        String clazzName = serviceClass.getName();
        if(!serviceMap.containsKey(clazzName)) {
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
        return (T)o;
    }
}
