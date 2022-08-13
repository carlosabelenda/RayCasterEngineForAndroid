package com.decksolutions.raycasterengine

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.decksolutions.raycasterengine.R
import com.decksolutions.raycasterengine.SurfaceViewRayCasting

class MainActivityKT : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val c = SurfaceViewRayCasting(this.applicationContext)
        setContentView(c)
        try {
            c.prepareScreenView()
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        Log.d("TEST", "OnCreate")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}