package com.aldi.austin.handler.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.aldi.austin.common.domain.AnchorInfo;
import com.aldi.austin.common.domain.TaskInfo;
import com.aldi.austin.common.enums.AnchorState;
import com.aldi.austin.common.pipeline.BusinessProcess;
import com.aldi.austin.common.pipeline.ProcessContext;
import com.aldi.austin.support.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * 丢弃消息
 * 一般将需要丢弃的模板id写在分布式配置中心
 *
 * @author 3y
 */
@Service
public class DiscardAction implements BusinessProcess<TaskInfo> {
    @Autowired
    private LogUtils logUtils;
    @Value("${austin.discard-msg-ids:[]}")
    private String discardMsgIds;

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        TaskInfo taskInfo = context.getProcessModel();
        // 配置示例:	["1","2"]
        JSONArray array = JSON.parseArray(discardMsgIds);
        if (array.contains(String.valueOf(taskInfo.getMessageTemplateId()))) {
            logUtils.print(AnchorInfo.builder().bizId(taskInfo.getBizId()).messageId(taskInfo.getMessageId()).businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).state(AnchorState.DISCARD.getCode()).build());
            context.setNeedBreak(true);
        }

    }
}
