package com.aldi.austin.service.api.impl.action.recall;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Throwables;
import com.aldi.austin.common.domain.RecallTaskInfo;
import com.aldi.austin.common.enums.RespStatusEnum;
import com.aldi.austin.common.pipeline.BusinessProcess;
import com.aldi.austin.common.pipeline.ProcessContext;
import com.aldi.austin.common.vo.BasicResultVO;
import com.aldi.austin.service.api.impl.domain.RecallTaskModel;
import com.aldi.austin.support.mq.SendMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author 3y
 * 将撤回消息发送到MQ
 */
@Slf4j
@Service
public class RecallMqAction implements BusinessProcess<RecallTaskModel> {
    @Autowired
    private SendMqService sendMqService;

    @Value("${austin.business.recall.topic.name}")
    private String austinRecall;

    @Value("${austin.mq.pipeline}")
    private String mqPipeline;

    @Override
    public void process(ProcessContext<RecallTaskModel> context) {
        RecallTaskInfo recallTaskInfo = context.getProcessModel().getRecallTaskInfo();
        try {
            String message = JSON.toJSONString(recallTaskInfo, SerializerFeature.WriteClassName);
            sendMqService.send(austinRecall, message);
        } catch (Exception e) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
            log.error("send {} fail! e:{},params:{}", mqPipeline, Throwables.getStackTraceAsString(e)
                    , JSON.toJSONString(recallTaskInfo));
        }
    }

}
