package com.aldi.austin.handler.flowcontrol.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.aldi.austin.common.domain.TaskInfo;
import com.aldi.austin.handler.enums.RateLimitStrategy;
import com.aldi.austin.handler.flowcontrol.FlowControlParam;
import com.aldi.austin.handler.flowcontrol.FlowControlService;
import com.aldi.austin.handler.flowcontrol.annotations.LocalRateLimit;

/**
 * Created by TOM
 * On 2022/7/21 17:05
 *
 * @author TOM
 */
@LocalRateLimit(rateLimitStrategy = RateLimitStrategy.REQUEST_RATE_LIMIT)
public class RequestRateLimitServiceImpl implements FlowControlService {

    /**
     * 根据渠道进行流量控制
     *
     * @param taskInfo
     * @param flowControlParam
     */
    @Override
    public Double flowControl(TaskInfo taskInfo, FlowControlParam flowControlParam) {
        RateLimiter rateLimiter = flowControlParam.getRateLimiter();
        return rateLimiter.acquire(1);
    }
}
