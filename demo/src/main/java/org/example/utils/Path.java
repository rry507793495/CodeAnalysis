package org.example.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;;

public class Path {
    public ArrayList<String> filePath = new ArrayList<>();
    public Map<String, String> pathClassMap = new HashMap<>();

    public ArrayList<String> getAllClassPath(String dirPath) {
        File file = new File(dirPath);
        for (String filename : file.list()) {
            String t = dirPath + "/" + filename;
            File tmp = new File(t);
            if (tmp.isDirectory()) {
                getAllClassPath(t);
            } else {
                if (t.contains(".class")) {
                    filePath.add(t);
                    pathClassMap.put(filename.split("\\.")[0], t);
                }
            }
        }
        // System.out.println(filePath);
        return filePath;
    }

    public String getPackageNameFromClass(String classPath) {
        ClassParser classParser = new ClassParser(classPath);

        JavaClass javaClass;
        try {
            javaClass = classParser.parse();
            String packageName = javaClass.getPackageName();
            return packageName;
        } catch (Exception e) {
            System.out.println(classPath);
            return null;
        }
    }
}
