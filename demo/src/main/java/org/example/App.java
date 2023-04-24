package org.example;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.example.cfg.ControlFlowEdgeAnalyzer;
import org.example.cfg.InsnBlock;
import org.example.utils.*;

/**
 * Hello world!
 *
 */
public class App {
    public static Map<Integer, MethodNode> methodMap = new HashMap<>();
    public static Path pathUtil = new Path();
    
    public static InsnBlock[] getBlocksOfMethod(String className, MethodNode methodNode) {
        ControlFlowEdgeAnalyzer<BasicValue> analyzer = new ControlFlowEdgeAnalyzer<>(new BasicInterpreter());
        try {
            analyzer.analyze(className, methodNode);
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
        InsnBlock[] blocks = analyzer.getBlocks();
        return blocks;
    }
    public static MethodNode findMethodFromAthoterClass(String classPath, String methodName) throws Exception {
        FileInputStream a = new FileInputStream(new File(classPath));
        byte[] bytes = a.readAllBytes();

        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();

        int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
        cr.accept(cn, parsingOptions);
    
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(methodName)) {
                return mn;
            }
        }
        return null;
    }
    
    public static void display(InsnBlock[] blocks) {
        for (InsnBlock block : blocks) {
            System.out.println(block.lines);
        }
    }
    public static boolean isSerialable(String classPath, String className) {
        try {
            URL classUrl = new URL("file://" + classPath); 
        
            URL[] classUrls = { classUrl };
            
            URLClassLoader ucl = new URLClassLoader(classUrls);
            
            Class clazz = ucl.loadClass(className); 
            ucl.close();
            
            return Serializable.class.isInstance(clazz.newInstance());

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }
        
    }
 
    public static  List<InsnBlock> dfsMethodNode(String classPath, String methodName, InsnBlock[] block) throws Exception {
        InsnBlock[] blocks;
        if (block == null) {
            FileInputStream a = new FileInputStream(new File(classPath));
            byte[] bytes = a.readAllBytes();
    
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode();
    
            int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
            cr.accept(cn, parsingOptions);
            MethodNode methodNode = null;
            for (MethodNode mn : cn.methods) {
                // System.out.println(mn.name);
                if (mn.name.equals(methodName)) {
                    methodNode =  mn;
                }
            }
            if (methodNode == null) {
                System.out.println("Can not find method: " + methodNode);
                return null;
            }
            blocks = getBlocksOfMethod(cn.name, methodNode);
        } else {
            blocks = block;
        }
        
        List<InsnBlock> blockslist = splitBlocks(blocks);
        List<InsnBlock> res = new ArrayList<>();
        
        for (int j = 0; j < blockslist.size(); j++) {
            for (int i = 0; i < blockslist.get(j).lines.size(); i++) {
                String instructing = blockslist.get(j).lines.get(i);
                if (instructing.contains("invokevirtual")) {
                    String tmp = instructing.split(" ")[1];
                    String method_ = tmp.split("\\.")[1];
                    String class_ = tmp.split("\\.")[0];

                    if(!pathUtil.pathClassMap.containsKey(class_)) {
                        continue;
                    }
                    MethodNode mn = findMethodFromAthoterClass(pathUtil.pathClassMap.get(class_), method_);
                    // System.out.println(instructing);
                    // res.add(getBlocksOfMethod(class_, mn)[0]);
                    blockslist.get(j).lines = getBlocksOfMethod(class_, mn)[0].lines;
                    dfsMethodNode(pathUtil.pathClassMap.get(class_), method_, blockslist.toArray(new InsnBlock[0]));
                    
                }
            }
            // if (flag) {
                // blockslist.add(insnBlock);
            // }
        }
        // for (InsnBlock insnBlock : res) {
        //     System.out.println(insnBlock.lines);
        // }
        return blockslist;
    }
    public static List<List<String>> splitBlock(InsnBlock block) {
        // [L0, aload_0, invokevirtual Employee.foo]
        List<String> left = new ArrayList<>();
        List<String> right = new ArrayList<>();
        boolean flag = true;
        for (int i = 0; i < block.lines.size(); i++) {
            String line = block.lines.get(i);
            if(line.contains("invokevirtual") && flag) {
                flag = false;
                continue;
                // System.out.println(i);
            }
            if (flag) {
                left.add(line);
            } else {
                right.add(line);
            }
        }
        // System.out.println(left);
        // System.out.println(right);

        List<List<String>> res = new ArrayList<>();
        res.add(left);
        res.add(right);

        return res;
    }
    public static List<InsnBlock> splitBlocks(InsnBlock[] blocks) {
        List<InsnBlock> blocksList = new ArrayList<>();
        for (int i = 0; i < blocks.length; i++) {
            InsnBlock tmpBlock = new InsnBlock();
            
            for (int j = 0; j < blocks[i].lines.size(); j++) {
                String instructing = blocks[i].lines.get(j);
                if (instructing.contains("invokevirtual")) {
                    String tmp = instructing.split(" ")[1];
                    String class_ = tmp.split("\\.")[0];
                    if (pathUtil.pathClassMap.containsKey(class_)) {
                        blocksList.add(tmpBlock);
                        tmpBlock = new InsnBlock();
                        tmpBlock.lines.add(instructing);
                        blocksList.add(tmpBlock);
                        tmpBlock = new InsnBlock();
                        continue;
                    }
                }
                tmpBlock.lines.add(instructing);
            }
            if (tmpBlock.lines.size() > 0){
                blocksList.add(tmpBlock);
            }
        }
        // for (InsnBlock insnBlock : blocksList) {
        //     System.out.println(insnBlock.lines);
        // }
        
        return blocksList;
    }
    

    public static void main( String[] args ) throws Exception{
        // testCfg();
        ArrayList<String> a = pathUtil.getAllClassPath("/Users/fengruizhi/Desktop/论文/毕业设计/code/demo/target/classes/com/example");
        for (String s : a) {
            String packageName = pathUtil.getPackageNameFromClass(s);
            String className = s.split("/")[s.split("/").length - 1];
            if (isSerialable(s, packageName + "." + className.replaceAll(".class", ""))) {
                List<InsnBlock> b = dfsMethodNode(s, "readObject", null);
                if (b == null) {
                    continue;
                }
                for (InsnBlock block : b) {
                    System.out.println(block.lines);
                }
            }
            
        }

    }
}
