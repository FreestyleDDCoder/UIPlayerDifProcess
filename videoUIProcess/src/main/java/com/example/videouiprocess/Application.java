package com.example.videouiprocess;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MediaServiceManager.getInstance().init(this.getApplicationContext());
    }
}
