package com.aldi.austin.handler.receipt.stater.impl;

import com.aldi.austin.common.constant.CommonConstant;
import com.aldi.austin.common.enums.ChannelType;
import com.aldi.austin.handler.handler.impl.DingDingWorkNoticeHandler;
import com.aldi.austin.handler.receipt.stater.ReceiptMessageStater;
import com.aldi.austin.support.dao.ChannelAccountDao;
import com.aldi.austin.support.domain.ChannelAccount;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 拉取 钉钉工作消息的回执 内容 【未完成】
 *
 * @author 3y
 */
public class DingDingWorkReceiptStaterImpl implements ReceiptMessageStater {

    @Autowired
    private DingDingWorkNoticeHandler workNoticeHandler;

    @Autowired
    private ChannelAccountDao channelAccountDao;

    @Override
    public void start() {
        List<ChannelAccount> accountList = channelAccountDao.findAllByIsDeletedEqualsAndSendChannelEquals(CommonConstant.FALSE, ChannelType.DING_DING_WORK_NOTICE.getCode());
        for (ChannelAccount channelAccount : accountList) {
            workNoticeHandler.pull(channelAccount.getId());
        }
    }
}
