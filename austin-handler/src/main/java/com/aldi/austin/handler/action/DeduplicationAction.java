package com.aldi.austin.handler.action;

import cn.hutool.core.collection.CollUtil;
import com.aldi.austin.common.domain.TaskInfo;
import com.aldi.austin.common.enums.DeduplicationType;
import com.aldi.austin.common.enums.EnumUtil;
import com.aldi.austin.common.pipeline.BusinessProcess;
import com.aldi.austin.common.pipeline.ProcessContext;
import com.aldi.austin.handler.deduplication.DeduplicationHolder;
import com.aldi.austin.handler.deduplication.DeduplicationParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * 去重服务
 * 1. 根据相同内容N分钟去重（SlideWindowLimitService）
 * 2. 相同的渠道一天内频次去重（SimpleLimitService）
 *
 * @author 3y
 */
@Service
public class DeduplicationAction implements BusinessProcess<TaskInfo> {

    @Autowired
    private DeduplicationHolder deduplicationHolder;

    // 配置样例{"deduplication_10":{"num":1,"time":300},"deduplication_20":{"num":5}}
    @Value("${austin.deduplication-rule:{}}")
    private String deduplicationConfig;

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        TaskInfo taskInfo = context.getProcessModel();


        // 去重
        List<Integer> deduplicationList = EnumUtil.getCodeList(DeduplicationType.class);
        for (Integer deduplicationType : deduplicationList) {
            DeduplicationParam deduplicationParam = deduplicationHolder.selectBuilder(deduplicationType).build(deduplicationConfig, taskInfo);
            if (Objects.nonNull(deduplicationParam)) {
                deduplicationHolder.selectService(deduplicationType).deduplication(deduplicationParam);
            }
        }

        if (CollUtil.isEmpty(taskInfo.getReceiver())) {
            context.setNeedBreak(true);
        }
    }
}
