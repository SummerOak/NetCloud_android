package com.summer.netcloud.message;

/**
 * Created by summer on 12/06/2018.
 */

public interface IMsgListener {
    void onMessage(int msgId,Object arg);
    Object onSyncMessage(int msgId,Object arg);
}
