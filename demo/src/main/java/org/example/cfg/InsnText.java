package org.example.cfg;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.SIPUSH;

public class InsnText {
    private static final String INSTRUCTION_FORMAT = "%s %s";

    private final Map<LabelNode, String> labelNames = new HashMap<>();

    public List<String> toLines(AbstractInsnNode[] array) {
        List<String> resultList = new ArrayList<>();
        if (array == null || array.length < 1) {
            return resultList;
        }

        for (AbstractInsnNode node : array) {
            List<String> lines = toLines(node);
            resultList.addAll(lines);
        }
        return resultList;
    }

    public List<String> toLines(AbstractInsnNode node) {
        List<String> resultList = new ArrayList<>();
        if (node instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode currentNode = (TableSwitchInsnNode) node;
            List<String> list = table_switch_node_to_str_list(currentNode);
            resultList.addAll(list);
        }
        else if (node instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode currentNode = (LookupSwitchInsnNode) node;
            List<String> list = lookup_switch_node_to_str_list(currentNode);
            resultList.addAll(list);
        }
        else {
            String item = node_to_str(node);
            resultList.add(item);
        }

        return resultList;
    }

    public String node_to_str(AbstractInsnNode currentNode) {
        if (currentNode instanceof InsnNode) {
            return getOpcodeName(currentNode);
        }
        else if (currentNode instanceof IntInsnNode) {
            int opcode = currentNode.getOpcode();
            String opcodeName = getOpcodeName(currentNode);
            IntInsnNode node = (IntInsnNode) currentNode;
            int operand = node.operand;
            if (opcode == BIPUSH || opcode == SIPUSH) {
                return String.format(INSTRUCTION_FORMAT, opcodeName, operand);
            }
            else {
                final String firstArg;
                switch (operand) {
                    case 4: {
                        firstArg = "4 (boolean)";
                        break;
                    }
                    case 5: {
                        firstArg = "5 (char)";
                        break;
                    }
                    case 6: {
                        firstArg = "6 (float)";
                        break;
                    }
                    case 7: {
                        firstArg = "7 (double)";
                        break;
                    }
                    case 8: {
                        firstArg = "8 (byte)";
                        break;
                    }
                    case 9: {
                        firstArg = "9 (short)";
                        break;
                    }
                    case 10: {
                        firstArg = "10 (int)";
                        break;
                    }
                    case 11: {
                        firstArg = "11 (long)";
                        break;
                    }
                    default:
                        throw new RuntimeException("atype is not supported: " + operand);
                }
                return String.format(INSTRUCTION_FORMAT, opcodeName, firstArg);
            }
        }
        else if (currentNode instanceof VarInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            VarInsnNode node = (VarInsnNode) currentNode;
            int var = node.var;
            if (var >= 0 && var <= 3) {
                return String.format("%s_%d", opcodeName, var);
            }
            else {
                return String.format(INSTRUCTION_FORMAT, opcodeName, var);
            }
        }
        else if (currentNode instanceof TypeInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            TypeInsnNode node = (TypeInsnNode) currentNode;
            String type = getSimpleName(node.desc);
            return String.format(INSTRUCTION_FORMAT, opcodeName, type);
        }
        else if (currentNode instanceof FieldInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            FieldInsnNode node = (FieldInsnNode) currentNode;
            String type = getSimpleName(node.owner);
            return String.format("%s %s.%s", opcodeName, type, node.name);
        }
        else if (currentNode instanceof MethodInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            MethodInsnNode node = (MethodInsnNode) currentNode;
            String type = getSimpleName(node.owner);
            return String.format("%s %s.%s", opcodeName, type, node.name);
        }
        else if (currentNode instanceof InvokeDynamicInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) currentNode;
            Type methodType = Type.getMethodType(node.desc);
            Type returnType = methodType.getReturnType();
            String type = getSimpleName(returnType.getInternalName());
            return String.format("%s %s.%s", opcodeName, type, node.name);
        }
        else if (currentNode instanceof JumpInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            JumpInsnNode node = (JumpInsnNode) currentNode;
            String labelName = getLabelName(node.label);
            return String.format(INSTRUCTION_FORMAT, opcodeName, labelName);
        }
        else if (currentNode instanceof LabelNode) {
            LabelNode node = (LabelNode) currentNode;
            return getLabelName(node);
        }
        else if (currentNode instanceof LdcInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            LdcInsnNode node = (LdcInsnNode) currentNode;
            Object cst = node.cst;
            if (cst instanceof Integer) {
                return String.format("%s %s(int)", opcodeName, cst);
            }
            else if (cst instanceof Float) {
                return String.format("%s %s(float)", opcodeName, cst);
            }
            else if (cst instanceof Long) {
                return String.format("%s %s(long)", opcodeName, cst);
            }
            else if (cst instanceof Double) {
                return String.format("%s %s(double)", opcodeName, cst);
            }
            else if (cst instanceof String) {
                return String.format("%s \"%s\"", opcodeName, cst);
            }
            else if (cst instanceof Class<?>) {
                return String.format("%s %s(class)", opcodeName, cst);
            }
            else {
                return String.format("%s %s", opcodeName, cst);
            }
        }
        else if (currentNode instanceof IincInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            IincInsnNode node = (IincInsnNode) currentNode;
            return String.format("%s %d %d", opcodeName, node.var, node.incr);
        }
        else if (currentNode instanceof MultiANewArrayInsnNode) {
            String opcodeName = getOpcodeName(currentNode);
            MultiANewArrayInsnNode node = (MultiANewArrayInsnNode) currentNode;
            String type = getSimpleName(node.desc);
            return String.format(INSTRUCTION_FORMAT, opcodeName, type);
        }
        else if (currentNode instanceof FrameNode) {
            return "FrameNode";
        }
        else if (currentNode instanceof LineNumberNode) {
            return "LineNumberNode";
        }
        else {
            System.out.println(currentNode.getClass());
        }
        return currentNode.toString();
    }

    public List<String> table_switch_node_to_str_list(TableSwitchInsnNode currentNode) {
        String opcodeName = getOpcodeName(currentNode);
        int min = currentNode.min;
        int max = currentNode.max;

        List<String> list = new ArrayList<>();
        list.add(String.format("%s {", opcodeName));
        for (int i = min; i <= max; i++) {
            LabelNode labelNode = currentNode.labels.get(i - min);
            String labelName = getLabelName(labelNode);
            list.add(String.format("    %d: %s", i, labelName));
        }
        list.add(String.format("    default: %s", getLabelName(currentNode.dflt)));
        list.add("}");
        return list;
    }

    public List<String> lookup_switch_node_to_str_list(LookupSwitchInsnNode currentNode) {
        String opcodeName = getOpcodeName(currentNode);
        List<Integer> keys = currentNode.keys;
        int size = keys.size();

        List<String> list = new ArrayList<>();
        list.add(String.format("%s {", opcodeName));
        for (int i = 0; i < size; i++) {
            int caseValue = keys.get(i);
            LabelNode labelNode = currentNode.labels.get(i);
            String labelName = getLabelName(labelNode);
            list.add(String.format("    %d: %s", caseValue, labelName));
        }
        list.add(String.format("    default: %s", getLabelName(currentNode.dflt)));
        list.add("}");
        return list;
    }

    private String getLabelName(LabelNode labelNode) {
        String labelName = labelNames.get(labelNode);
        if (labelName == null) {
            labelName = "L" + labelNames.size();
            labelNames.put(labelNode, labelName);
        }
        return labelName;
    }

    private static String getOpcodeName(AbstractInsnNode currentNode) {
        int opcode = currentNode.getOpcode();
        return OpcodeConst.getOpcodeName(opcode);
    }

    private static String getSimpleName(String descriptor) {
        int squareIndex = descriptor.lastIndexOf("[");
        String prefix = descriptor.substring(0, squareIndex + 1);

        String simpleName = descriptor.substring(squareIndex + 1);
        if (simpleName.startsWith("L") && simpleName.endsWith(";")) {
            simpleName = simpleName.substring(1, simpleName.length() - 1);
        }

        int slashIndex = simpleName.lastIndexOf("/");
        simpleName = simpleName.substring(slashIndex + 1);

        return prefix + simpleName;
    }
}
