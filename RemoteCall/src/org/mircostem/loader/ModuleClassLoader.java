package org.mircostem.loader;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleClassLoader extends ClassLoader {

    private static final String CLASS_FILE_SUFFIX = ".class";
    private String routerServiceImplPackage = "";
    private String controllingServiceImplPackage = "";


    protected final ClassLoader parent;

    protected final Map<String, ResourceEntry> resourceEntries =
            new ConcurrentHashMap<>();

    protected boolean delegate = false;



    public ModuleClassLoader(ClassLoader parent) {

        super( parent);

        ClassLoader p = getParent();
        if (p == null) {
            p = getSystemClassLoader();
        }
        this.parent = p;
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(!StringUtils.contains(name,routerServiceImplPackage)){
            Class<?> aClass = Class.forName(name);
            return aClass;
        }
        synchronized (getClassLoadingLock(name)) {
            System.out.println("loadClass(" + name + ", " + resolve + ")");
            Class<?> clazz = null;
            clazz = findLoadedClass0(name);
            if (clazz != null) {
                System.out.println("  Returning class from cache");
                if (resolve)
                    resolveClass(clazz);
                return clazz;
            }
            clazz = findLoadedClass(name);
            if (clazz != null) {
                System.out.println("  Returning class from cache");
                if (resolve)
                    resolveClass(clazz);
                return clazz;
            }

            // 默认搜索
            {
                System.out.println("  Delegating to parent classloader at end: " + parent);
                try {
                    clazz = Class.forName(name, false, parent);
                    if (clazz != null) {
                        System.out.println("  Loading class from parent");
                        if (resolve)
                            resolveClass(clazz);
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore
                }
            }
            //找自己
            {
                try {
                    clazz =  findClass(name);
                    if (clazz != null) {
                        System.out.println("  Loading class from self|" + this);
                        if (resolve)
                            resolveClass(clazz);
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore
                }
            }
        }

        throw new ClassNotFoundException(name);
    }

    protected Class<?> findLoadedClass0(String name) {

        String path = binaryNameToPath(name, true);

        ResourceEntry entry = resourceEntries.get(path);
        if (entry != null) {
            return entry.loadedClass;
        }
        return null;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if(StringUtils.contains(name,routerServiceImplPackage)){
            if(!StringUtils.contains(name,controllingServiceImplPackage)){
                return null;
            }
        }
        System.out.println("    findClass(" + name + ")");
        Class<?> clazz;
        try {
            System.out.println("      findClassInternal(" + name + ")");
            try {
                clazz = findClassInternal(name);
            } catch (AccessControlException ace) {
                System.out.println(("webappClassLoader.securityException|" + name + "|" + ace.getMessage()));
                ace.printStackTrace();
                throw new ClassNotFoundException(name, ace);
            } catch (RuntimeException e) {
                System.out.println("      -->RuntimeException Rethrown");
                e.printStackTrace();
                throw e;
            }
            if ((clazz == null)) {
                try {
                    clazz = super.findClass(name);
                } catch (AccessControlException ace) {
                    System.out.println(("webappClassLoader.securityException|" + name + "|" + ace.getMessage()));
                    ace.printStackTrace();
                    throw new ClassNotFoundException(name, ace);
                } catch (RuntimeException e) {
                    System.out.println("      -->RuntimeException Rethrown");
                    e.printStackTrace();
                    throw e;
                }
            }
            if (clazz == null) {
                System.out.println("    --> Returning ClassNotFoundException");
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("    --> Passing on ClassNotFoundException");
            throw e;
        }

        System.out.println("      Returning class " + clazz);

        {
            ClassLoader cl;
            cl = clazz.getClassLoader();
            System.out.println("      Loaded by " + cl.toString());
        }
        return clazz;

    }

    protected Class<?> findClassInternal(String name) {


        if (name == null) {
            return null;
        }
        String path = binaryNameToPath(name, true);

        ResourceEntry entry = resourceEntries.get(path);

        if (entry == null) {

            entry = new ResourceEntry();
            entry.lastModified = 1;
            synchronized (resourceEntries) {
                ResourceEntry entry2 = resourceEntries.get(path);
                if (entry2 == null) {
                    resourceEntries.put(path, entry);
                } else {
                    entry = entry2;
                }
            }
        }

        Class<?> clazz = entry.loadedClass;
        if (clazz != null)
            return clazz;

        //自己搜索目录
        {
            String locationPath = this.getClass().getResource("/").getPath();
            String replacingClassName = StringUtils.replace(name,".", "/");
            String classFileLocationPath = locationPath + replacingClassName + CLASS_FILE_SUFFIX;
            byte[] cLassBytes = null;
            try (FileInputStream in =new FileInputStream(new File(classFileLocationPath));){
                //当文件没有结束时，每次读取一个字节显示
                cLassBytes=new byte[in.available()];
                in.read(cLassBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            clazz = defineClass(name, cLassBytes, 0, cLassBytes.length);
            if(clazz != null){
                entry.loadedClass = clazz;
            }
        }
        synchronized (getClassLoadingLock(name)) {
            clazz = entry.loadedClass;
            if (clazz != null)
                return clazz;

            entry.loadedClass = clazz;
        }

        return clazz;
    }

    private String binaryNameToPath(String binaryName, boolean withLeadingSlash) {
        // 1 for leading '/', 6 for ".class"
        StringBuilder path = new StringBuilder(7 + binaryName.length());
        if (withLeadingSlash) {
            path.append('/');
        }
        path.append(binaryName.replace('.', '/'));
        path.append(CLASS_FILE_SUFFIX);
        return path.toString();
    }



    public boolean getDelegate() {
        return this.delegate;
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
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
