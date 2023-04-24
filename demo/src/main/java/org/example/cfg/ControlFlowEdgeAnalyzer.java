package org.example.cfg;
import org.example.cfg.InsnBlock;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControlFlowEdgeAnalyzer<V extends Value> extends Analyzer<V>  {
    private AbstractInsnNode[] nodeArray;
    public InsnBlock[] blocks;

    public ControlFlowEdgeAnalyzer(Interpreter<V> interpreter) {
        super(interpreter);
    }

    @Override
    public Frame<V>[] analyze(String owner, MethodNode method) throws AnalyzerException {
        nodeArray = method.instructions.toArray();
        int length = nodeArray.length;
        blocks = new InsnBlock[length];
        InsnText insnText = new InsnText();
        for (int i = 0; i < length; i++) {
            blocks[i] = getBlock(i);
            AbstractInsnNode node = nodeArray[i];
            List<String> lines = insnText.toLines(node);
            blocks[i].addLines(lines);
        }

        return super.analyze(owner, method);
    }

    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        // 首先，处理自己的代码逻辑
        AbstractInsnNode insnNode = nodeArray[insnIndex];
        int insnOpcode = insnNode.getOpcode();
        int insnType = insnNode.getType();

        if (insnType == AbstractInsnNode.JUMP_INSN) {
            if ((insnIndex + 1) == successorIndex) {
                addNext(insnIndex, successorIndex);
            }
            else {
                addJump(insnIndex, successorIndex);
            }
        }
        else if (insnOpcode == LOOKUPSWITCH) {
            addJump(insnIndex, successorIndex);
        }
        else if (insnOpcode == TABLESWITCH) {
            addJump(insnIndex, successorIndex);
        }
        else if (insnOpcode == RET) {
            addJump(insnIndex, successorIndex);
        }
        else if (insnOpcode == ATHROW || (insnOpcode >= IRETURN && insnOpcode <= RETURN)) {
            assert false : "should not be here";
            removeNextAndJump(insnIndex);
        }
        else {
            addNext(insnIndex, successorIndex);
        }

        // 其次，调用父类的方法实现
        super.newControlFlowEdge(insnIndex, successorIndex);
    }

    private void addNext(int fromIndex, int toIndex) {
        InsnBlock currentBlock = getBlock(fromIndex);
        InsnBlock nextBlock = getBlock(toIndex);
        currentBlock.addNext(nextBlock);
    }

    private void addJump(int fromIndex, int toIndex) {
        InsnBlock currentBlock = getBlock(fromIndex);
        InsnBlock nextBlock = getBlock(toIndex);
        currentBlock.addJump(nextBlock);
    }

    private void removeNextAndJump(int insnIndex) {
        InsnBlock currentBlock = getBlock(insnIndex);
        currentBlock.nextBlockList.clear();
        currentBlock.jumpBlockList.clear();
    }

    private InsnBlock getBlock(int insnIndex) {
        InsnBlock block = blocks[insnIndex];
        if (block == null){
            block = new InsnBlock();
            blocks[insnIndex] = block;
        }
        return block;
    }

    public InsnBlock[] getBlocks() {
        //（2）如果结果为空，则提前返回
        if (blocks == null || blocks.length < 1) {
            return blocks;
        }

        //（3）记录“分隔点”
        Set<InsnBlock> newBlockSet = new HashSet<>();
        int length = blocks.length;
        for (int i = 0; i < length; i++) {
            InsnBlock currentBlock = blocks[i];
            List<InsnBlock> nextBlockList = currentBlock.nextBlockList;
            List<InsnBlock> jumpBlockList = currentBlock.jumpBlockList;

            boolean hasNext = false;
            boolean hasJump = false;

            if (nextBlockList.size() > 0) {
                hasNext = true;
            }

            if (jumpBlockList.size() > 0) {
                hasJump = true;
            }

            if (!hasNext && (i + 1) < length) {
                newBlockSet.add(blocks[i + 1]);
            }

            if (hasJump) {
                newBlockSet.addAll(jumpBlockList);

                if (hasNext) {
                    newBlockSet.add(blocks[i + 1]);
                }
            }
        }

        //（4）利用“分隔点”，合并成不同的分组
        List<InsnBlock> resultList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            InsnBlock currentBlock = blocks[i];

            if (i == 0) {
                resultList.add(currentBlock);
            }
            else if (newBlockSet.contains(currentBlock)) {
                resultList.add(currentBlock);
            }
            else {
                int size = resultList.size();
                InsnBlock lastBlock = resultList.get(size - 1);
                lastBlock.lines.addAll(currentBlock.lines);
                lastBlock.jumpBlockList.clear();
                lastBlock.jumpBlockList.addAll(currentBlock.jumpBlockList);
                lastBlock.nextBlockList.clear();
                lastBlock.nextBlockList.addAll(currentBlock.nextBlockList);
            }
        }

        return resultList.toArray(new InsnBlock[0]);
    }


}
