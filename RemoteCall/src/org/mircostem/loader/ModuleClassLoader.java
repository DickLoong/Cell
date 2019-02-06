package org.mircostem.loader;

import org.apache.commons.lang3.StringUtils;

public class ModuleClassLoader extends ClassLoader {
    private String routerServiceImplPackage = "";
    private String controllingServiceImplPackage = "";

    public ModuleClassLoader(RouterClassLoader routerClassLoader) {
        super(routerClassLoader);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (StringUtils.contains(className, routerServiceImplPackage)) {
            if (!StringUtils.contains(className, controllingServiceImplPackage)) {
                throw new ClassNotFoundException();
            }
        }
        return super.findClass(className);
    }

    public String getRouterServiceImplPackage() {
        return routerServiceImplPackage;
    }

    public void setRouterServiceImplPackage(String routerServiceImplPackage) {
        this.routerServiceImplPackage = routerServiceImplPackage;
    }

    public String getControllingServiceImplPackage() {
        return controllingServiceImplPackage;
    }

    public void setControllingServiceImplPackage(String controllingServiceImplPackage) {
        this.controllingServiceImplPackage = controllingServiceImplPackage;
    }
}
