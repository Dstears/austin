package com.aldi.austin.handler.handler;

import com.aldi.austin.common.domain.RecallTaskInfo;
import com.aldi.austin.common.domain.TaskInfo;

/**
 * @author 3y
 * 消息处理器
 */
public interface Handler {

    /**
     * 处理器
     *
     * @param taskInfo
     */
    void doHandler(TaskInfo taskInfo);

    /**
     * 撤回消息
     *
     * @param recallTaskInfo
     * @return
     */
    void recall(RecallTaskInfo recallTaskInfo);

}
