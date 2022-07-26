package com.decksolutions.raycasterengine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class SurfaceThread extends Thread
{
	private SurfaceHolder surfaceHolder;
	private SurfaceViewCrystal cristal;
	private boolean run;
	private Context contexto;
	private Canvas canvas;
	
	//Retardo en ms
	private int DELAY = 10000;

	private int positionX=1;
	private int positionY=1;
	
	public SurfaceThread(SurfaceHolder sh, SurfaceViewCrystal cristal, Context contexto)
	{
		this.surfaceHolder = sh;
		this.cristal = cristal;
		this.contexto = contexto;
		run = false;
		
		
		Utils.getInstance().loadSoundLibrary(contexto);

		//definimos nulo el area en donde pintar
		Log.d("SURFACETHREAD","INSTANCIADO");

	}
	
	
	public void init() 
	{

	}
	
	
	public void setRunning(boolean run)
	{
		this.run = run;
	}
	
	public void run()
	{

		
		while(run)
		{
			long timeIni = System.currentTimeMillis();



			try
			{


				//nos aseguramos que ningun otro hilo use esto
				synchronized (surfaceHolder)
				{
					Log.d("SURFACETHREAD-RUN","init synchro");
					/*canvas = surfaceHolder.lockCanvas(null);
					//actualizamos el estado de la app
					update(canvas.getWidth(),canvas.getHeight());

					//update();
					//findingWalls();
					//calculateDistances();
					//Decimos que ejecute el ondraw y canvas para pintar
					//Si no lo hacemos en otro sitio
					cristal.draw(canvas);
					surfaceHolder.unlockCanvasAndPost(canvas);*/
					Log.d("SURFACETHREAD-RUN","fin synchro");
				}
				

			}
			catch(Exception e)
			{
				Log.e("EXCEPTION SURFACE",e.toString());
				//si hay error liberamos el canvas
				if (canvas != null)
				{
					//surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			finally {
				if (canvas != null)
				{
					//surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}


			
			try {
				long timeEnd = System.currentTimeMillis();
				if (timeEnd-timeIni < DELAY)
					this.sleep(DELAY-(timeEnd-timeIni));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Actualizamos estado de la app
	 */
	private void update(int canvasWidth, int canvasHeight)
	{
		positionX +=10;
		positionY +=10;
		cristal.updateInfo(canvasWidth,canvasHeight);//positionX,positionY,canvasWidth, canvasHeight);
	}
	   
	

	   


}
