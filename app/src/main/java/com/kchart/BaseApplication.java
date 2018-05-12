package com.kchart;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2018/5/9.
 */

public class BaseApplication  extends Application{
    private static   BaseApplication instance;
    public static synchronized BaseApplication getInstance(){
        if (instance==null){
            instance=new BaseApplication();
        }
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }
}
