package org.pulp.fastapi.life;

/**
 * 实现了该接口的类可观察Activity或Fragment的onDestory
 * 务必在实现了此接口的Activity或Fragment的onDestory中使用
 * DestoryHelper.notify(this);
 * 去通知所有注册在此类上的监听器,不然会造成Activity或Fragment泄漏!!!!!!
 * Created by xinjun on 2019/12/12 14:42
 */
public interface DestoryWatcher {

    /**
     * Activity或Fragment的onDestory监听器
     * Created by xinjun on 2019/12/12 14:43
     */
    public interface DestoryListener {
        void onDestory();
    }
}
