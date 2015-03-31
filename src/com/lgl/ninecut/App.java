package com.lgl.ninecut;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Util.initImageLoader(this);
	}
}
