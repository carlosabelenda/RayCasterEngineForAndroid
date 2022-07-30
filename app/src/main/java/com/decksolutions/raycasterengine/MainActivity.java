package com.decksolutions.raycasterengine;


import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//SurfaceViewCrystal c = new SurfaceViewCrystal(this);
		//c.init(c.getWidth(),c.getHeight());
		SurfaceViewRayCasting c = new SurfaceViewRayCasting(this.getApplicationContext());
		setContentView(c);
		try {
			c.prepareScreenView();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("TEST","OnCreate");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

}
