package com.decksolutions.raycasterengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.HashMap;



/*
 * MOTOR DE RAYCASTING
 *
 * Clase principal del motor de RayCasting.
 *
 * Utilizaremos el siguiente sistema de coordenadas
 *
 *         Y                                                                        90º
 *   X--------------------> la x aumenta en esta direccion                           |
 *         |                                                              180º----------------0º
 *         |                                                                         |
 *         |                                                                         |
 *       la y aumenta en esta direccion                                             270º
 *
 */

/*
 *
 * Tiempo medio que tarda: 47 milisegundos
 */
public class SurfaceViewRayCasting extends SurfaceView implements
            SurfaceHolder.Callback {
        private DrawThread drawThread;
        private Point location;
        SurfaceHolder holder;
        private boolean surfaceCreated = false;

        private int DELAY;
        private final boolean DEBUGMODE = true;


        private HashMap<String, Bitmap> bitmaps;

        private Context contexto;

        private boolean imagesLoaded = false;

        private Paint paint;
        public int mode;


        private byte[][] mapa ;

        //datos del plano de proyeccion
        public int planeWidth;
        public int planeHeight;
        public int planeCenterX;
        public int planeCenterY;


        //almacena para cada pared, las coordenadas de las 4 esquinas
        //consideramos un m�ximo de 20 paredes, se puede ajustar
        //almacena tambien el rayo inicial y final de la pared
        //asi como el color
        private int[][]          walls;
        private static int   []  sCount;
        private static int [][]  vTable;
        private int              step  = 1;

        //guarda los datos de colision de cada rayo
        //0: tileY
        //1: tileX
        //2: si choco con fila o columna: 0=fila, 1=columna
        //3: distancia a la que choco
        //4: angulo con el que choco
        private long[][] dataRay;
        //guarda el numero maximo de muros
        private int maxWalls;

        //campo de vision en grados
        public double FOV;

        //alto del player y de los bloques del escenario
        public static final int player_high=32;
        public static final int block_height=64;


        //distancia al plano de proyeccion: medio ancho del plano / tan(FOV/2)
        //tangente = cateto opuesto / cateto adyacente
        public double planeDistance;
        //angulo entre columnas= FOV / ancho pantalla (habra que utilizar coma fija)
        public double angleColumns;

        //posicion del player (con casillas de 64 x 64)
        public int playerX;
        public int playerY;
        //angulo de vista en relacion al mundo
        public double playerAngle;

        //angulo actual
        public double actualAngle;

        //avance del personaje
        public static double movement = 10;
        public boolean down        = false;
        public boolean up          = false;
        public boolean right       = false;
        public boolean left        = false;

        public double  FPS;

        public SurfaceViewRayCasting(Context context) {
            super(context);

        }

        public SurfaceViewRayCasting(Context context, AttributeSet attrs) {
            super(context, attrs);

        }

        public SurfaceViewRayCasting(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);

        }

        private void initialize(Canvas canvas) {



            paint = new Paint();

            mapa = new byte[][]{
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3,3},
                    {1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                    {1,0,0,3,3,3,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,3,3,3,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,2,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,2,2,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,1},
                    {1,0,0,0,0,0,0,0,0,0,5,5,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,5,5,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}

            };


           /* dataRay[i][5] = Color.RED;
            break;
            case 1:
            dataRay[i][5] = Color.BLUE;
            break;
            case 2:
            dataRay[i][5] = Color.GREEN;
            break;
            case 3:
            dataRay[i][5] = Color.GRAY;
            break;
            case 4:
            dataRay[i][5] = Color.WHITE;
            break;
            case 5:
            dataRay[i][5] = Color.MAGENTA;
            break;
            default:
            dataRay[i][5] = Color.BLACK;*/

                    FOV = 60.0;
                    //datos del plano de proyeccion
                    planeWidth = canvas.getWidth();
                    planeHeight = canvas.getHeight();
                    planeCenterX = planeWidth / 2;
                    planeCenterY = planeHeight / 2;


                    //almacena para cada pared, las coordenadas de las 4 esquinas
                    //consideramos un m�ximo de 20 paredes, se puede ajustar
                    //almacena tambien el rayo inicial y final de la pared
                    //asi como el color
                    walls = new int[50][7];
                    step = 1;

                    //guarda los datos de colision de cada rayo
                    //0: tileY
                    //1: tileX
                    //2: si choco con fila o columna: 0=fila, 1=columna
                    //3: distancia a la que choco
                    //4: angulo con el que choco
                    dataRay = new long[planeWidth][6];

                    //distancia al plano de proyeccion: medio ancho del plano / tan(FOV/2)
                    //tangente = cateto opuesto / cateto adyacente
                    planeDistance = (((planeWidth / 2) * Math.cos(Math.toRadians(FOV / 2))) / Math.sin(Math.toRadians(FOV / 2)));

                    //angulo entre columnas= FOV / ancho pantalla (habra que utilizar coma fija)
                    angleColumns = FOV / planeWidth;

                     //posicion del player (con casillas de 64 x 64)
                    playerX = (10*block_height)+(block_height/2);
                    playerY = (12*block_height)+(block_height/2);
                    //angulo de vista en relacion al mundo
                    playerAngle = 45;

                    //angulo actual
                    actualAngle = 45;

                    //Retardo en ms
                    DELAY = 200;


        }

        public void prepareScreenView() {
            holder = this.getHolder();
            holder.addCallback(this);
            setFocusable(true);
            setWillNotDraw(true);


        }

        public void stopThread() {
            drawThread.setRunning(false);
            drawThread.stop();
        }

        public void update() {

            int auxX,auxY;

            //Reviso si me puedo mover hacia esas áreas para avanzar
            //Log.d("MOTION","down, up, right, left:"+down+","+up+","+right+","+left+" PLAYER:"+playerX+","+playerY);
            if (down)
            {
                auxX = playerX - (int)((movement*Math.cos(Math.toRadians(playerAngle))));
                auxY = playerY + (int)((movement*Math.sin(Math.toRadians(playerAngle))));

                if (mapa[auxY/block_height][playerX/block_height] == 0) playerY = auxY;
                if (mapa[playerY/block_height][auxX/block_height] == 0) playerX = auxX;

                down=false;
            }
            else if (up)
            {
                auxX = playerX + (int)((movement*Math.cos(Math.toRadians(playerAngle))));
                auxY = playerY - (int)((movement*Math.sin(Math.toRadians(playerAngle))));
                if (mapa[auxY/block_height][playerX/block_height] == 0) playerY = auxY;
                if (mapa[playerY/block_height][auxX/block_height] == 0) playerX = auxX;
                up = false;
            }

            if (right)
            {
                playerAngle-=5;
                if (playerAngle<0) playerAngle=360+playerAngle;
                right = false;
            }
            else if (left) {
                playerAngle = (playerAngle + 5) % 360;
                left = false;
            }


            long iniFPS = System.currentTimeMillis();
            findingWalls();
            calculateDistances();
            long finFPS = System.currentTimeMillis();

            FPS = (finFPS-iniFPS);
        }

        public void onDraw(Canvas canvas) {

            paint.setColor(0xFFFFFF00);
            canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),paint);

            paint.setColor(0xFFFF0000);

            for (int i=0;i<maxWalls-1;i++)
            {
                int color = walls[i][6];


                float rectX[] = new float[4];
                float rectY[] = new float[4];
                rectX[0]      = walls[i][4];
                rectY[0]      = walls[i][0];
                rectX[1]      = walls[i][4];
                rectY[1]      = walls[i][0]+walls[i][1];
                rectX[2]      = walls[i][5];
                rectY[2]      = walls[i][2]+walls[i][3];
                rectX[3]      = walls[i][5];
                rectY[3]      = walls[i][2];



                //canvas.drawdrawRect(50,80,200,300,paint);
                Paint wallpaint = new Paint();
                wallpaint.setColor(color);

                //canvas.drawLine(rectX[0],rectY[0],rectX[3],rectY[3],wallpaint);

                wallpaint.setStyle(Paint.Style.FILL);
                Path wallpath = new Path();
                wallpath.reset();
                // only needed when reusing this path for a new build
                wallpath.moveTo(rectX[0], rectY[0]);
                // used for first point wallpath.lineTo(x[1], y[1]);
                 wallpath.lineTo(rectX[3], rectY[3]);
                 wallpath.lineTo(rectX[2], rectY[2]);
                 wallpath.lineTo(rectX[1], rectY[1]);
                // there is a setLastPoint action but i found it not to work as expected
                canvas.drawPath(wallpath, wallpaint);


            }



            if (DEBUGMODE)
            {
               /* for (int i=0;i<10;i++)
                {
                    paint.setColor(Color.RED);
                    canvas.drawLine(i*100,0,i*100,canvas.getHeight(),paint);
                }*/

                paint.setColor(Color.LTGRAY);
                canvas.drawRect((float)canvas.getWidth()-300,0,(float)canvas.getWidth(),300,paint);
                paint.setColor(Color.BLACK);
                paint.setTextSize(50);
                canvas.drawText("ANGLE:"+playerAngle,(float)canvas.getWidth()-300,50,paint);
                canvas.drawText("TILE(X,Y):"+(playerX/block_height)+","+(playerY/block_height),(float)canvas.getWidth()-300,100,paint);
                canvas.drawText("FPS:"+FPS,(float)canvas.getWidth()-300,150,paint);

/*               painter.setColor(0x00ffff);
                painter.drawString(""+timetotal,5,5,Graphics.TOP|Graphics.LEFT);

                painter.setColor(0xff0000);
                painter.drawLine(Canvas.halfWidth,0,Canvas.halfWidth,Canvas.height);
*/
            }


            invalidate();
            //TODO if (imagesLoaded){}

            //canvas.restore();*/
        }

        class DrawThread extends Thread {
            private SurfaceHolder surfaceHolder;
            SurfaceViewRayCasting mySurfaceView;
            private boolean run = false;
            private boolean initialized = false;

            public DrawThread(SurfaceHolder surfaceHolder,
                              SurfaceViewRayCasting mySurfaceView) {
                this.surfaceHolder = surfaceHolder;
                this.mySurfaceView = mySurfaceView;
                run = false;
            }

            public void setRunning(boolean run) {
                this.run = run;
            }

            @Override
            public void run() {
                Canvas canvas = null;
                while (run) {
                    long timeIni = System.currentTimeMillis();
                    try {


                        synchronized (surfaceHolder) {
                            //Log.d("RUN","ANTES DE ONDRAW"+surfaceHolder);
                            canvas = surfaceHolder.lockCanvas();
                            if (!initialized) {
                                mySurfaceView.initialize(canvas);
                                initialized = true;
                            }
                            mySurfaceView.onDraw(canvas);
                            mySurfaceView.update();
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
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


        }


    public void findingWalls()
    {
        //inicializamos el pintado del muro
        for (int i=walls.length-1;i>=0;i--)
        {
            for (int j=walls[0].length-1;j>=0;j--)
                walls[i][j] = 0;
        }

        //distancias entre el player y los muros
        double rowsDistance=0,colsDistance=0;
        double wallDistance=0;

        //altura proyectada del muro
        int proyectedWallHeight;

        actualAngle=(((playerAngle+(FOV/2))%360));
        //System.out.println(" actualAngle: "+actualAngle);
        for (int i=0;i<planeWidth;i++)
        {
            //System.out.println("RAYO: "+i);
            int rowsPoints[]=new int[2];
            int colsPoints[]=new int[2];

            rowsDistance=0;
            colsDistance=0;
            wallDistance=0;
            //para ver si colisiona con un muro har� dos pasadas a lo largo de
            //la rejilla, una para ver con qu� fila de la rejilla colisiona
            //y otra para ver con qu� columna. La distancia que sea menor de las
            //dos ser� la elegida.

            double auxAngle=(actualAngle);
            //System.out.println(" auxAngle: "+auxAngle);
            if (auxAngle!=0 && auxAngle!=180)
            {
                rowsPoints=findRows();
                //if (rowsPoints != null) System.out.println("rows: "+rowsPoints[0]+" "+rowsPoints[1]);
            }
            else
                rowsPoints=null;

            //Una vez que lo hemos hecho para las filas, tenemos que hacer
            //lo mismo para las columnas
            if (auxAngle!=90 || auxAngle!=270)
            {
                colsPoints=findCols();
                // if (colsPoints != null) System.out.println("cols: "+colsPoints[0]+" "+colsPoints[1]);
            }
            else
                colsPoints=null;

            //De las dos distancias, tenemos que comprobar cual de las dos es la
            //menor
            if (rowsPoints!=null) {
                rowsDistance = Math.sqrt(((playerX - rowsPoints[1]) * (playerX - rowsPoints[1])) + ((playerY - rowsPoints[0]) * (playerY - rowsPoints[0])));
                //Log.d("TILE(0) "+i+"--> angulo " + auxAngle + "º, pX:"+playerX+", pY:"+playerY , " rows:" + rowsDistance + " cols:" + colsDistance + " -->  pX:" + playerX + ",cP1:" + rowsPoints[1] + ",pY:" + playerY + ",cP0:" + rowsPoints[0]);
            }

            if (colsPoints!=null) {
                colsDistance = Math.sqrt(((playerX - colsPoints[1]) * (playerX - colsPoints[1])) + ((playerY - colsPoints[0]) * (playerY - colsPoints[0])));
                //Log.d("TILE(0) "+i+"--> angulo " + auxAngle + "º, pX:"+playerX+", pY:"+playerY , " rows:" + rowsDistance + " cols:" + colsDistance + " -->  pX:" + playerX + ",cP1:" + colsPoints[1] + ",pY:" + playerY + ",cP0:" + colsPoints[0]);
            }

            //System.out.println(" rowsDistance: "+rowsDistance+" colsDistance: "+colsDistance);
            //  System.out.println("rowsDistance: "+rowsDistance+" colsDistance: "+colsDistance);
            if (rowsPoints!=null || colsPoints!=null)
            {
                //Obtenemos la zona del mapa en donde esta
                int typeBlock;

                //cogemos la menor de las distancias que no sea 0
                if ((rowsDistance<colsDistance && rowsDistance>0) ||
                    (colsDistance == 0 && rowsDistance != 0))
                {
                        //TODO cambio esto wallDistance  = FXPMath.toFixedPoint(rowsDistance);
                        wallDistance  = (rowsDistance);

                        dataRay[i][0] = rowsPoints[0]/block_height;
                        dataRay[i][1] = rowsPoints[1]/block_height;
                        dataRay[i][2] = 0;
                        dataRay[i][3] = (long)wallDistance;
                        dataRay[i][4] = (long)actualAngle;

                        typeBlock = mapa[rowsPoints[0]/block_height][rowsPoints[1]/block_height];

//Log.d("TILE(1):"+i+"rowsdistance:"+rowsDistance+" "+colsDistance,""+rowsPoints[0]/block_height+" - "+rowsPoints[1]/block_height+" - "+typeBlock);
                        //En funci�n del tipo de bloque, elige el color adecuado
                        switch(typeBlock)
                        {
                            case 0:
                                dataRay[i][5] = Color.RED;
                                break;
                            case 1:
                                dataRay[i][5] = Color.BLUE;
                                break;
                            case 2:
                                dataRay[i][5] = Color.GREEN;
                                break;
                            case 3:
                                dataRay[i][5] = Color.GRAY;
                                break;
                            case 4:
                                dataRay[i][5] = Color.WHITE;
                                break;
                            case 5:
                                dataRay[i][5] = Color.MAGENTA;
                                break;
                            default:
                                dataRay[i][5] = Color.BLACK;
                                break;
                        }


                        // System.out.println("choca con filas1: "+(rowsPoints[0]/block_height)+" , "+(rowsPoints[1]/block_height));
                }
                else
                if (rowsDistance>colsDistance || rowsDistance==0)
                {
                        wallDistance  = (colsDistance);
                        dataRay[i][0] = colsPoints[0]/block_height;
                        dataRay[i][1] = colsPoints[1]/block_height;
                        dataRay[i][2] = 1;
                        dataRay[i][3] = (long)wallDistance;
                        dataRay[i][4] = (long)actualAngle;

                        typeBlock     = mapa[colsPoints[0]/block_height][colsPoints[1]/block_height];

                        //En funci�n del tipo de bloque, elige el color adecuado
                        switch(typeBlock)
                        {
                            case 0:
                                dataRay[i][5] = Color.RED;
                                break;
                            case 1:
                                dataRay[i][5] = Color.BLUE;
                                break;
                            case 2:
                                dataRay[i][5] = Color.GREEN;
                                break;
                            case 3:
                                dataRay[i][5] = Color.GRAY;
                                break;
                            case 4:
                                dataRay[i][5] = Color.WHITE;
                                break;
                            case 5:
                                dataRay[i][5] = Color.MAGENTA;
                                break;
                            default:
                                dataRay[i][5] = Color.BLACK;
                                break;
                        }

//Log.d("TILE(2):"+i+"rowsdistance:"+rowsDistance+" "+colsDistance,""+colsPoints[0]/block_height+" - "+colsPoints[1]/block_height+" - "+typeBlock);
                        //System.out.println("choca con columnas1: "+(colsPoints[0]/block_height)+" , "+(colsPoints[1]/block_height));
                }
/*else
                {
                    Log.d("TILE(3):"+i,"NOOOOOOOOOOOOOOOOOOOOOOOO");
                }*/
                    //  System.out.println("wallDistance:"+wallDistance+" "+dataRay[i][0]+" "+dataRay[i][1]+" "+dataRay[i][2]+" "+dataRay[i][3]+" "+dataRay[i][4]);



                /*TODO esto deberia sobrarif (rowsDistance!=0)
                {
                    wallDistance  = (rowsDistance);
                    dataRay[i][0] = rowsPoints[0]/block_height;
                    dataRay[i][1] = rowsPoints[1]/block_height;
                    dataRay[i][2] = 0;
                    dataRay[i][3] = (long)wallDistance;
                    dataRay[i][4] = (long)actualAngle;

                    typeBlock     = mapa[rowsPoints[0]/block_height][rowsPoints[1]/block_height];

                    switch(typeBlock)
                    {
                        case 0:
                            dataRay[i][5] = Color.RED;
                            break;
                        case 1:
                            dataRay[i][5] = Color.BLUE;
                            break;
                        case 2:
                            dataRay[i][5] = Color.GREEN;
                            break;
                        case 3:
                            dataRay[i][5] = Color.GRAY;
                            break;
                        case 4:
                            dataRay[i][5] = Color.WHITE;
                            break;
                        case 5:
                            dataRay[i][5] = Color.MAGENTA;
                            break;
                        default:
                            dataRay[i][5] = Color.BLACK;
                            break;
                    }

                    Log.d("TILE(3):"+i+"rowsdistance:"+rowsDistance+" "+colsDistance,""+rowsPoints[0]/block_height+" - "+rowsPoints[1]/block_height+" - "+typeBlock);
                    // System.out.println("choca con filas2 "+(rowsPoints[0]/block_height)+" , "+(rowsPoints[1]/block_height));
                }
                else
                if (colsDistance!=0)
                {
                    wallDistance = (colsDistance);

                    dataRay[i][0] = colsPoints[0]/block_height;
                    dataRay[i][1] = colsPoints[1]/block_height;
                    dataRay[i][2] = 1;
                    dataRay[i][3] = (long)wallDistance;
                    dataRay[i][4] = (long)actualAngle;

                    typeBlock     = mapa[colsPoints[0]/block_height][colsPoints[1]/block_height];

                    switch(typeBlock)
                    {
                        case 0:
                            dataRay[i][5] = Color.RED;
                            break;
                        case 1:
                            dataRay[i][5] = Color.BLUE;
                            break;
                        case 2:
                            dataRay[i][5] = Color.GREEN;
                            break;
                        case 3:
                            dataRay[i][5] = Color.GRAY;
                            break;
                        case 4:
                            dataRay[i][5] = Color.WHITE;
                            break;
                        case 5:
                            dataRay[i][5] = Color.MAGENTA;
                            break;
                        default:
                            dataRay[i][5] = Color.BLACK;
                            break;
                    }

                    Log.d("TILE(4):"+i+"rowsdistance:"+rowsDistance+" "+colsDistance,""+colsPoints[0]/block_height+" - "+colsPoints[1]/block_height+" - "+typeBlock);
//Log.d("TILE(4):"+i,""+colsPoints[0]/block_height+" - "+colsPoints[1]/block_height);
                    // System.out.println("choca con columnas2 "+(colsPoints[0]/block_height)+" , "+(colsPoints[1]/block_height));



                }*/

                //TODO esto se deberia borrar else   System.out.println("ESTO NO DEBERIA PASARRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR:"+wallDistance);
            }


            actualAngle=actualAngle-angleColumns;
            //System.out.println("ActualANGLE:"+actualAngle);
        }
    }


    //Calcula las distancias entre paredes
    public void calculateDistances()
    {
        //Para el rayo 0 insertamos los datos de inicio de la pared
        double  correctedAngle;
        double realDistance;
        int  proyectedWallHeight;

        //corregimos la distancia para no tener el efecto pecera
        correctedAngle = (((int)dataRay[0][4])-playerAngle);
        if (correctedAngle!=90 && correctedAngle!=270)
        {
            realDistance = dataRay[0][3] * Math.cos(Math.toRadians(correctedAngle));
        }
        else
            realDistance = dataRay[0][3];

        //TODO para qué es esta linea?        realDistance = realDistance>>16;
        //System.out.println("---------planeDistance: "+planeDistance+" realDistance:"+realDistance+" dataRay[0][3]:"+dataRay[0][3]+" correctedAngle:"+correctedAngle);
        //obtenemos la altura proyectada
        double aux    = ((planeDistance*block_height)/realDistance);
        proyectedWallHeight=(int)aux;

        //ya tenemos la altura que tendra el muro dentro del mundo en funcion
        //de la distancia a la que estamos.
        //El punto medio de la altura de ese muro estar� en el centro de
        //la pantalla. Con eso, sabemos cuanto ocupar� ese muro
        int inicioY = planeCenterY-(proyectedWallHeight>>1);

        //asignamos los puntos a la pared
        walls[0][0] = inicioY;
        walls[0][1] = proyectedWallHeight;
        walls[0][4] = 0;
        walls[0][6] = (int)dataRay[0][5];

        //el indice que apunta a la pared sobre la que estamos calculando actualmente
        int indexWalls = 0;

        int i = 1;

        while (i<planeWidth)
        {

            //guarda los datos de colision de cada rayo
            //0: tileY
            //1: tileX
            //2: si choco con fila o columna: 0=fila, 1=columna
            //3: distancia a la que choco
            //4: angulo con el que choco
            while (i<planeWidth - 1                 &&
                    dataRay[i][0] == dataRay[i-1][0] &&
                    dataRay[i][1] == dataRay[i-1][1] &&
                    dataRay[i][2] == dataRay[i-1][2])
            {
                i++;
            }

            correctedAngle       = (((double)dataRay[i-1][4])-playerAngle);

            if (correctedAngle!=90 && correctedAngle!=270)
            {
                realDistance    = dataRay[i-1][3] * Math.cos(Math.toRadians(correctedAngle));
            }
            else
                realDistance     = dataRay[i-1][3];


            //TODO para qué es esta linea?        realDistance = realDistance>>16;
            //System.out.println("---------planeDistance: "+planeDistance+" realDistance:"+realDistance+" dataRay[0][3]:"+dataRay[0][3]+" correctedAngle:"+correctedAngle);

            aux                  = ((planeDistance*block_height)/realDistance);
            proyectedWallHeight  = (int)aux;

            inicioY              = planeCenterY-(proyectedWallHeight>>1);

            //ponemos los valores finales de la pared
            walls[indexWalls][2] = inicioY;
            walls[indexWalls][3] = proyectedWallHeight;
            walls[indexWalls][5] = i-1;

            //ponemos los valores de la nueva pared
            if (indexWalls < walls.length-1)
            {
                indexWalls ++;
                correctedAngle       = (((int)dataRay[i][4])-playerAngle);

                if (Math.abs(correctedAngle)!=90 && Math.abs(correctedAngle)!=270)
                {
                    realDistance    = dataRay[i][3] * Math.cos(Math.toRadians(correctedAngle));
                }
                else
                {
                    realDistance    = dataRay[i][3];
                }
                //TODO para qué es esta linea?        realDistance = realDistance>>16;
                //System.out.println("---------planeDistance: "+planeDistance+" realDistance:"+realDistance+" dataRay[0][3]:"+dataRay[0][3]+" correctedAngle:"+correctedAngle);

                aux                  = ((planeDistance*block_height)/realDistance);
                proyectedWallHeight  = (int)aux;

                inicioY              = planeCenterY-(proyectedWallHeight>>1);
                //almacena para cada pared, las coordenadas de las 4 esquinas
                //consideramos un m�ximo de 20 paredes, se puede ajustar
                //almacena tambien el rayo inicial y final de la pared
                //asi como el color
                walls[indexWalls][0] = inicioY;
                walls[indexWalls][1] = proyectedWallHeight;
                walls[indexWalls][4] = i;
                walls[indexWalls][6] = (int)dataRay[i][5];

            }
            i++;
        }
        maxWalls = indexWalls + 1;

    }

    //busqueda por filas hasta que sobrepase los limites o encuentre muro
    //1. Encontramos la coordenada de la primera intersecci�n
    //2. Encontramos yPoint. (Nota: yPoint es el alto de la celda;
    //   sin embargo,si el rayo mira hacia arriba, Ya sera negativo y
    //   si el rayo mira hacia abajo, yPoint ser� positivo)
    //3. Encontrar xPoint usando la ecuacion:
    //      A.x = Px + (Py-A.y)/tan(ALPHA);
    //4. Comprobar el punto de intersecci�n. Si hay muro,
    //   finalizamos el bucle y calculamos la distancia
    //5. Si no hay muro, buscamos el siguiente punto de intersecci�n.
    //   Hay que darse cuenta que las coordenadas de la siguiente
    //   interseccion Xnew,Ynew son Xnew=Xold+Xa, and Ynew=YOld+Ya.

    public int[] findRows()
    {

        double angulo=(actualAngle);

        //punto en donde hay un muro
        double xPoint,yPoint;
        //incremento hasta encontrar muro
        double xAux,yAux;

        //buscamos los limites
        boolean in_limits=true;
        boolean is_wall=false;
//System.out.println("ANG:"+angulo);
        //COMPROBAMOS EL PRIMER PUNTO
        //si el player mira hacia arriba (0�-180�)
        if (angulo < 180 && angulo > 0) {

            yPoint = ((playerY/block_height)*block_height)-1;
            //columna en la que cruza sera: yPoint/64
        }
        else {

            yPoint = ((playerY/block_height)*block_height) + block_height;
            //columna en la que cruza sera: yPoint/64
        }
        // System.out.println("1-"+ yPoint+" "+playerY+" "+block_height);
        //calculamos el xPoint
        //A(x) = Px + (Py-A.y)/tan(ALPHA);
        //xPoint = playerX + (playerY-yPoint)/((FXPMath.sin(actualAngle)/FXPMath.cos(actualAngle)));

        if (angulo == 0 || angulo == 180)
            xPoint = playerX +
                    ( Math.abs(playerY - yPoint) * Math.cos(Math.toRadians(angulo)));
        else
            xPoint = playerX +
                    (( (playerY - yPoint) * Math.cos(Math.toRadians(angulo))) /
                            (Math.sin(Math.toRadians(angulo))));

        //fila en la que cruza sera: xPoint/64
        //comprobamos si en esa celda hay muro, si lo hay, acabamos
        //si no lo hay, continuamos con el bucle si no ha llegado a los limites
        int fila = (int)yPoint / block_height;
        int columna = (int)xPoint / block_height;
//System.out.println("xPoint:"+xPoint+" "+" yPoint:"+yPoint+" playerX:"+playerX+" "+" playerY:"+playerY+" FXPMath.cos(angulo):"+FXPMath.cos(angulo)+" FXPMath.sin(angulo):"+FXPMath.sin(angulo));
        if (yPoint < 0 || fila >= mapa.length ||
                xPoint < 0 || columna >= mapa[0].length)

            in_limits = false;

        else if (mapa[fila][columna] != 0)

            is_wall = true;



        while (in_limits == true && is_wall == false) {

            //COMPROBAMOS LOS SIGUIENTES
            //si el player mira hacia arriba (0�-180�)
            if (angulo < 180 && angulo > 0)
                yPoint -= block_height;
            else
                yPoint += block_height;

            //calculamos el auxiliar

            if (angulo==0 || angulo==180)
                xAux = ( (block_height) * Math.cos(Math.toRadians(angulo)));
            else
                xAux = ( (block_height) * Math.cos(Math.toRadians(angulo))) /
                        (Math.sin(Math.toRadians(angulo)));

            //le sumamos el desplazamiento relativo al punto anteriormente obtenido
            if (angulo>=0 && angulo<180)
                xPoint += xAux;
            else
                xPoint -= xAux;

            //obtenemos el nuevo punto
            fila = (int)yPoint / block_height;
            columna = (int)xPoint / block_height;
            //System.out.println("fila: "+ fila+" columna: "+columna+ " ypoint:"+yPoint+" xpoint:"+xPoint);
            if (yPoint < 0 || fila >= mapa.length ||
                    xPoint < 0 || columna >= mapa[0].length)

                in_limits = false;
            else if (mapa[fila][columna] != 0)

                is_wall = true;
        }

        if (is_wall)
        {
            int point[]=new int[2];
            point[0]=(int)yPoint;
            point[1]=(int)xPoint;
//System.out.println("ROWS point[0]:"+point[0]+" point[1]:"+point[1]);
            return point;
        }
        else
            return null;

    }

    //1. Encontramos la coordenada de la primera interseccion
    //   Si el rayo apunta hacia la derecha del mapa (angulo entre 270� y 90�)
    //   usamos xPoint = redondeando por abajo(PlayerX/block_height) * (block_height) + block_height.
    //   si apunta hacia la izquierda (entre 90� y 270�)
    //   usamos xPoint = redondeando por abajo(PlayerX/block_height) * (block_height) - 1.
    //2. Encontramos xAux. (Nota: xAux es el ancho de la rejilla;
    //   sin embargo, si el rayo mira a la derecha, xAux sera positivo,
    //   y si mira a la izquierda, xAux sera negativo
    //3. Encontramos yAux usando la ecuacion
    //4. Comprobar el punto de intersecci�n. Si hay muro,
    //   finalizamos el bucle y calculamos la distancia
    //5. Si no hay muro, buscamos el siguiente punto de intersecci�n.
    //   Hay que darse cuenta que las coordenadas de la siguiente
    //   interseccion Xnew,Ynew son Xnew=Xold+Xa, and Ynew=YOld+Ya.

    public int[] findCols()
    {

        double angulo=(actualAngle);
        //punto en donde hay un muro
        double xPoint,yPoint;
        //incremento hasta encontrar muro
        double xAux,yAux;

        //buscamos los limites
        boolean in_limits=true;
        boolean is_wall=false;


        //COMPROBAMOS EL PRIMER PUNTO
        //si el player mira hacia derecha (<90 grados o > 270)
        if (angulo < 90 || angulo > 270) {
            xPoint = ((playerX/block_height) * block_height)+block_height;
            //fila en la que cruza sera: xPoint/64
        }
        else {
            xPoint = ((playerX/block_height) * block_height)-1;
            //fila en la que cruza sera: xPoint/64
        }

        //calculamos el xPoint
        //A(y) = Py + (Px-A.x)/tan(ALPHA);
        //yPoint = playerY + (playerX-xPoint)/((FXPMath.sin(playerAngle)/FXPMath.cos(playerAngle)));

        //si mira hacia arriba
        if (angulo < 180 && angulo > 0)
        {
            if (angulo==90 || angulo == 270)
                yPoint = playerY -
                        Math.abs((playerX - xPoint) * Math.sin(Math.toRadians(angulo)));
            else
                yPoint = playerY -
                        Math.abs(((playerX - xPoint) * (Math.sin(Math.toRadians(angulo)))) /
                                (Math.cos(Math.toRadians(angulo))));

        }
        else
        {
            if (angulo==270)
                yPoint = playerY +
                        Math.abs((playerX - xPoint) * Math.sin(Math.toRadians(angulo)));
            else
                yPoint = playerY +
                        Math.abs(((playerX - xPoint) * Math.sin(Math.toRadians(angulo))) /
                                (Math.cos(Math.toRadians(angulo))));
        }


        //columna en la que cruza sera: yPoint/64
        //comprobamos si en esa celda hay muro, si lo hay, acabamos
        //si no lo hay, continuamos con el bucle si no ha llegado a los limites
        int fila = (int)yPoint / block_height;
        int columna = (int)xPoint / block_height;

        if (yPoint < 0 || fila >= mapa.length || xPoint < 0 ||
                columna >= mapa[0].length)
            in_limits = false;
        else if (mapa[fila][columna] != 0)
            is_wall = true;

        while (in_limits == true && is_wall == false) {

            //COMPROBAMOS LOS SIGUIENTES
            //le sumamos el desplazamiento relativo al punto anteriormente obtenido
            //si el player mira hacia derecha (<90 grados o > 270)
            if (angulo < 90 || angulo > 270)
                xPoint += block_height;
            else
                xPoint -= block_height;

            //calculamos el auxiliar
            //yAux = block_height / tangente (alpha)
            //le ponemos el valor correcto en funcion del angulo que toma
            if (angulo < 180 && angulo > 0)
            {
                if (angulo==90)
                    yAux = ((block_height) * Math.sin(Math.toRadians(angulo)));
                else
                    yAux = Math.abs(( (block_height) * Math.sin(Math.toRadians(angulo))) /
                            (Math.cos(Math.toRadians(angulo))));
            }
            else
            {
                if (angulo==270)
                    yAux = ((block_height) * Math.sin(Math.toRadians(angulo)));
                else
                {
                    //System.out.println(yPoint+" "+FXPMath.sin(angulo)+" "+FXPMath.cos(angulo));
                    yAux = Math.abs(((block_height) * Math.sin(Math.toRadians(angulo))) /
                            (Math.cos(Math.toRadians(angulo))));
                }
            }

            //le sumamos el desplazamiento relativo al punto anteriormente obtenido
            if (angulo < 180 && angulo > 0)
                yPoint -= yAux;
            else
                yPoint += yAux;


            //obtenemos el nuevo punto
            fila = (int)yPoint / block_height;
            columna = (int)xPoint / block_height;
//System.out.println("COLUMNAS    fila: "+ fila+" columna: "+columna+ " ypoint:"+yPoint+" xpoint:"+xPoint);
            if (yPoint < 0 || fila >= mapa.length || xPoint < 0 ||
                    columna >= mapa[0].length)
                in_limits = false;
            else if (mapa[fila][columna] != 0)
                is_wall = true;

        }

        if (is_wall)
        {
            int point[]=new int[2];
            point[0]=(int)yPoint;
            point[1]=(int)xPoint;
//System.out.println("COLS point[0]:"+point[0]+" point[1]:"+point[1]);
            return point;
        }
        else
            return null;

    }



        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {

            this.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            //Log.d("MOTION","UP");
                            //right=left=up=down=false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            //Log.d("MOTION","MOVE"+event.getRawX()+","+event.getRawY()+","+event.getX()+","+event.getY());

                            break;
                        case MotionEvent.ACTION_DOWN:

                            if (event.getX() > v.getWidth()-200) {
                                right = true;
                                Log.d("MOTION", "RIGHT");
                            }
                            else
                            if (event.getX() < 200) {
                                left = true;
                                Log.d("MOTION", "LEFT");
                            }
                            else
                            if (event.getY() < 200) {
                                up = true;
                                Log.d("MOTION", "UP");
                            }
                            else
                            if (event.getY() > v.getHeight()-200){
                                down = true;
                                Log.d("MOTION", "DOWN");
                            }

                            break;

                        default:
                            break;
                    }
                    return true;
                }
            });

            drawThread = new DrawThread(holder, this);
            drawThread.setRunning(true);
            drawThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            //Log.d("SURFACE","DESTROYED");

            boolean retry = true;
            //paramos hilo
            drawThread.setRunning(false);
            while(retry)
            {
                try
                {
                    drawThread.join();
                    retry = false;
                }catch(InterruptedException e)
                {

                }
            }
        }



    }

