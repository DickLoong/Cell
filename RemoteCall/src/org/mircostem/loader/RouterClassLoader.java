package org.mircostem.loader;

import org.apache.commons.lang3.StringUtils;

public class RouterClassLoader  extends ClassLoader{
    private String routerServiceImplPackage = "";

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if(StringUtils.contains(className,routerServiceImplPackage)) {
            throw new ClassNotFoundException();
        }else{
            return super.findClass(className);
        }
    }


    public RouterClassLoader() {
        super(ClassLoader.getSystemClassLoader());
    }


    public String getRouterServiceImplPackage() {
        return routerServiceImplPackage;
    }

    public void setRouterServiceImplPackage(String routerServiceImplPackage) {
        this.routerServiceImplPackage = routerServiceImplPackage;
    }
}
