package org.mircostem.differentiation;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ApoptosisPacker {
    public static void apoptosis(String projectPath,String stemPackage,String holdingPackage,String differentiatedStemLocation) {
        File projectPathLocation = new File(projectPath);
        if(!projectPathLocation.exists()){
            System.out.println("empty project|" + projectPath);
            System.exit(0);
        }
        if(!projectPathLocation.isDirectory()){
            System.out.println("not a directory|" + projectPath);
            System.exit(0);
        }
        File stemFileLocation = new File(stemPackage);
        if(!stemFileLocation.exists()){
            System.out.println("Indicated stem location not exists.|" + stemPackage);
            System.exit(0);
        }
        File holdingPackageFile = new File(holdingPackage);
        if(!holdingPackageFile.exists()){
            System.out.println("Indicated holding location not exists.|" + stemPackage);
            System.exit(0);
        }
        String[] stemFileNames = stemFileLocation.list();
        List<String> stemFileNameList = Arrays.stream(stemFileNames).collect(Collectors.toList());
        boolean existFlag = stemFileNameList.stream().anyMatch(p -> StringUtils.equals(p, holdingPackage));
        if(!existFlag){
            System.out.println("Indicated holding location not exists in indecated stem location.|" + stemPackage + "|" + holdingPackage);
            System.exit(0);
        }
        File holdingFile = null;
        {
            String parent = projectPathLocation.getParent();
            String tempFileName = projectPathLocation.getName() + "_back";
            holdingFile = new File(parent, tempFileName);
            holdingFile.delete();
            try {
                copyDir(projectPathLocation.getPath(), holdingFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        {
            List<File> scanningFileList = new LinkedList<>();
            File[] files = projectPathLocation.listFiles();
            List<File> files1 = Arrays.asList(files);
            scanningFileList.addAll(files1);
            while (!scanningFileList.isEmpty()) {
                List<File> tempHoldingList = new LinkedList<>();
                boolean isScanned = false;
                for (File sourceFile : scanningFileList) {
                    String parentPath = sourceFile.getParent();
                    if(!StringUtils.contains(stemPackage,parentPath)){
                        continue;
                    }
                    if (StringUtils.equals(stemPackage, parentPath)) {
                        isScanned = true;
                        String actualFilePath = sourceFile.getPath();
                        if (!StringUtils.equals(actualFilePath, holdingPackage)) {
                            sourceFile.delete();
                        }
                    }else{
                        File[] innerFiles = sourceFile.listFiles();
                        List<File> innerFileList = Arrays.asList(innerFiles);
                        tempHoldingList.addAll(innerFileList);
                    }
                }
                if(isScanned){
                    break;
                }else{
                    scanningFileList = tempHoldingList;
                }
            }
        }
        {
            try {
                copyDir(projectPath,differentiatedStemLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                String sourceFilePath = projectPathLocation.getPath();
                projectPathLocation.delete();
                holdingFile.renameTo(new File(projectPath));
            }

        }
    }


    public static void copyDir(String sourcePath, String newPath) throws IOException {
        File file = new File(sourcePath);
        String[] filePath = file.list();

        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }

        for (int i = 0; i < filePath.length; i++) {
            if ((new File(sourcePath + file.separator + filePath[i])).isDirectory()) {
                copyDir(sourcePath  + file.separator  + filePath[i], newPath  + file.separator + filePath[i]);
            }

            if (new File(sourcePath  + file.separator + filePath[i]).isFile()) {
                copyFile(sourcePath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
            }

        }
    }

    public static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        File file = new File(newPath);
        FileInputStream in = new FileInputStream(oldFile);
        FileOutputStream out = new FileOutputStream(file);;

        byte[] buffer=new byte[2097152];
        int readByte = 0;
        while((readByte = in.read(buffer)) != -1){
            out.write(buffer, 0, readByte);
        }

        in.close();
        out.close();
    }
}
