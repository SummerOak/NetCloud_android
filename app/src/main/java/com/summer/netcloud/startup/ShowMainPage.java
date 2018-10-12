package com.summer.netcloud.startup;

import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.window.MainWindow;

/**
 * Created by summer on 13/06/2018.
 */

public class ShowMainPage extends Starter.Task {

    @Override
    protected int start() {

        MsgDispatcher.get().dispatchSync(Messege.PUSH_WINDOW, new MainWindow(ContextMgr.getContext()));

        return 0;
    }


}
