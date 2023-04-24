package org.example.cfg;

import java.util.ArrayList;
import java.util.List;

public class InsnBlock {
    // 文字信息
    public List<String> lines = new ArrayList<>();

    // 关联关系
    public List<InsnBlock> nextBlockList = new ArrayList<>();
    public List<InsnBlock> jumpBlockList = new ArrayList<>();

    public void addLines(List<String> list) {
        lines.addAll(list);
    }

    public void addNext(InsnBlock item) {
        nextBlockList.add(item);
    }

    public void addJump(InsnBlock item) {
        jumpBlockList.add(item);
    }

}