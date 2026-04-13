package com.example.service;

import com.example.entity.CharTable;
import com.example.entity.Selection;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class SelectionService {
    @Resource
    private CharTableService charTableService;

    public Selection getSelection() {
        List<CharTable> charTableList;
        // 循环获取直到列表中至少有一个有效的OBC
        do {
            charTableList = charTableService.selectRandomList();
        } while (hasNoValidOBC(charTableList));

        // 收集所有有效OBC的索引
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < charTableList.size(); i++) {
            CharTable item = charTableList.get(i);
            if (item.getOBC() != null && !item.getOBC().isEmpty()) {
                validIndices.add(i);
            }
        }

        // 随机选择一个有效索引作为答案
        int ansIndex = validIndices.get((int)(Math.random() * validIndices.size()));

        // 构建选项列表
        List<String> options = new ArrayList<>();
        String[] prefix = {"A.", "B.", "C.", "D."};
        for (int i = 0; i < 4; i++) {
            options.add(prefix[i] + charTableList.get(i).getsWord());
        }

        // 构造返回对象
        Selection selection = new Selection();
        selection.setAns(ansIndex);
        selection.setOptions(options);
        selection.setImgUrl("https://jgwlxr.obs.cn-south-1.myhuaweicloud.com/" + charTableList.get(ansIndex).getOBC());

        return selection;
    }

    // 检查列表是否全部OBC都为空
    private boolean hasNoValidOBC(List<CharTable> list) {
        return list.stream().noneMatch(item -> item.getOBC() != null && !item.getOBC().isEmpty());
    }
}