package org.pulp.fastapi.extension;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * 可以让一段代码运行在rxjava当前订阅的io线程中
 * Created by xinjun on 2020/1/4 10:25
 */
public class IOObservable extends Observable {

    private Observable upstream;


    public void setListener(IORun listener) {
        this.listener = listener;
    }

    private IORun listener;

    public IOObservable(Observable upstream) {
        this.upstream = upstream;
    }


    @Override
    protected void subscribeActual(Observer observer) {
        if (listener != null)
            listener.runInIO();
        //noinspection unchecked
        upstream.subscribe(observer);
    }

    /**
     * io run
     * Created by xinjun on 2020/1/4 11:13
     */
    public interface IORun {
        /**
         * 此方法运行在io线程中
         */
        void runInIO();
    }
}
