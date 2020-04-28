package net.ontariotechu.automata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GameView extends SurfaceView implements Runnable, GestureDetector.OnGestureListener {

    private final int startPop = 7;
    private int startFood = 200;
    private boolean isPlaying;
    private Thread thread;
    public static float screenRatioX, screenRatioY;
    private Species player;
    public static int screenX, screenY;
    public static int borderX, borderY;
    private Paint paint;
    private Canvas canvas;
    private GestureDetector gd;
    public static List<Species> critters;
    public static List<Food> food;
    private Random random;
    private Button highlightPlayerBtn,senseBtn;
    private boolean highlightPlayer = false, showVision = false;
    long start, finish, timeElapsed, start2, finish2, timeElapsed2;
    int days = 0;
    int playerCrittersAlive, greenCrittersAlive, blueCrittersAlive, yellowCrittersAlive;
    private float cameraX, cameraY;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.screenX = screenX;
        this.screenY = screenY;
        this.borderX = screenX + 1000;
        this.borderY = screenY + 1000;

        //center the camera
        float extraX = borderX - screenX;
        float extraY = borderY - screenY;
        this.cameraX = -extraX/2;
        this.cameraY = -extraY/2;
        food = new ArrayList<>();

        //adjust size of critters based on screen size using an arbitrary size as a baseline
        screenRatioX = screenX / 1920f;
        screenRatioY = screenY / 1080f ;

        random = new Random();
        critters = new ArrayList<>();

        //create player species
        for(int i = 0; i < startPop; i++){
            player = new Species(getResources(), activity.size,
                    activity.speed, activity.sense, activity.breed,
                    screenRatioX, screenRatioY, 1);

            player.setPosition(critters);

            critters.add(player);
        }


        //spawn food
        for(int i = 0; i < startFood; i++){
            Food pellet = new Food(getResources());
            food.add(pellet);
        }

        //create player highlight toggle
        Bitmap btnImg = BitmapFactory.decodeResource(getResources(), R.drawable.exclamation);
        highlightPlayerBtn = new Button(btnImg.getWidth(), btnImg.getHeight(), btnImg, 0.1f);
        highlightPlayerBtn.x = screenX - highlightPlayerBtn.getBitmap().getWidth() -10;
        highlightPlayerBtn.y = 10;

        //create critter view toggle
        Bitmap btnImg2 = BitmapFactory.decodeResource(getResources(), R.drawable.eyball);
        senseBtn = new Button(btnImg2.getWidth(), btnImg2.getHeight(), btnImg2, 0.2f);
        senseBtn.x = screenX - highlightPlayerBtn.getBitmap().getWidth() - 10 - senseBtn.getBitmap().getWidth();
        senseBtn.y = 10;

        paint = new Paint();
        canvas = getHolder().lockCanvas();
        gd = new GestureDetector(this);

        generateRandomSpecies(3);

        start = System.currentTimeMillis();
        start2 = System.currentTimeMillis();
    }

    private void generateRandomSpecies(int numSpecies) {

        int color = 2;
        for(int i = 0; i < numSpecies; i++){

            //random starting stats.  Maximum of 9 stats total
            //each stat is guaranteed a chance at having a value of 2 or more
            //each stat will be at least 1;
            int speed = random.nextInt(5);
            int sense = random.nextInt(7 - speed);
            int breed = random.nextInt(9 - speed - sense);
            int size = speed + sense + breed;
            speed = size == 0 ? 1 : speed;  //if size is 0, default species is one speed


            for(int j = 0; j < startPop; j++){
                Species species = new Species(getResources(), size,
                        speed, sense, breed,
                        screenRatioX, screenRatioY, color);
                species.setPosition(critters);
                //species.x = random.nextInt(screenX);
                //species.y = random.nextInt(screenY);
                critters.add(species);
            }

            color++;
        }
    }

    @Override
    public void run() {
        while(isPlaying){
            update();
            draw();
            sleep();
        }
    }

    public void resume(){

        isPlaying = true;
        thread = new Thread(this);
        thread.start(); //calls run method
    }

    public void pause(){

        try {
            isPlaying = false;
            thread.join();//stops the thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        playerCrittersAlive = 0;
        greenCrittersAlive = 0;
        blueCrittersAlive = 0;
        yellowCrittersAlive = 0;

        finish = System.currentTimeMillis();
        timeElapsed = finish - start;

        finish2 = System.currentTimeMillis();
        timeElapsed2 = finish2 - start2;
        boolean reset = false;

        //move critters
        for(int i = critters.size() -1; i >= 0; i--){
            if(critters.get(i).color == 1){playerCrittersAlive++;}
            else if(critters.get(i).color == 2){greenCrittersAlive++;}
            else if(critters.get(i).color == 3){blueCrittersAlive++;}
            else if(critters.get(i).color == 4){yellowCrittersAlive++;}

            critters.get(i).move();
            critters.get(i).checkCollisions(critters);



            //reduce energy approx every 1 second
            if(timeElapsed2 >= 1000){

                critters.get(i).reduceEnergy();
                reset = true;

            }
            if(critters.get(i).energy <= 0){
                critters.remove(i);
            }
        }

        if(reset){start2 = System.currentTimeMillis();}

        //remove eaten food
        for(int i = food.size() -1; i >= 0; i--){
            if(food.get(i).isEaten){
                food.remove(i);
            }
        }

        //add food every 5 seconds
        if(timeElapsed > 1000 * 5){
            for(int i = 0; i < startPop * 4; i++){
                Food pellet = new Food(getResources());
                food.add(pellet);
            }
            start = System.currentTimeMillis();
            for(int i = critters.size() -1; i >= 0; i--) {
                //each point in breed gives approx 1% extra chance to breed
                int chance = random.nextInt(1000) - (10 * critters.get(i).breed);
                //Log.d("chance", chance+"");

                if (chance < 10) {
                    critters.get(i).makeBaby(critters);
                }
            }
            //Log.d("---", "--------------");
            days++;
        }

    }

    public void draw(){
        if(getHolder().getSurface().isValid()){
            canvas = getHolder().lockCanvas(); //returns canvas being displayed on screen

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            //draw background
            canvas.drawRect(0, 0, screenX, screenY, paint);

            for(Food food: food){
                canvas.drawBitmap(food.food, food.x + cameraX, food.y + cameraY, paint);
            }

            //critters and boxes are drawn with the critter's x and y positions in the center of the sprite
            for(Species critter : critters){
                if(highlightPlayer || showVision){
                    paint.setStyle(Paint.Style.STROKE);

                    switch (critter.color){
                        case 1:
                            paint.setColor(Color.RED);
                            break;
                        case 2:
                            paint.setColor(Color.GREEN);
                            break;
                        case 3:
                            paint.setColor(Color.BLUE);
                            break;
                        case 4:
                            paint.setColor(Color.YELLOW);
                            break;
                    }
                    if(highlightPlayer) {
                        //draw border around player species
                        canvas.save();
                        canvas.rotate(critter.orientation, critter.x + cameraX, critter.y + cameraY);
                        //draw rect based of of center of critter and camera position
                        canvas.drawRect(critter.x - critter.width/2 -1 + cameraX, critter.y - critter.height/2 -1 + cameraY,
                                critter.x + critter.width/2 + 1 + cameraX, critter.y + critter.height/2 + 1 + cameraY, paint);
                        canvas.restore();
                    }
                    if(showVision){
                        canvas.drawCircle(critter.x + cameraX, critter.y + cameraY, (critter.sense) * 50 + 150f, paint);
                    }
                }

                //rotate and draw critter
                canvas.save();
                canvas.rotate(critter.orientation, critter.x + cameraX , critter.y + cameraY);
                canvas.drawBitmap(critter.critter, critter.x - critter.width/2 + cameraX, critter.y - critter.height/2 + cameraY, paint);
                canvas.restore();

            }

            //draw buttons
            canvas.drawBitmap(highlightPlayerBtn.getBitmap(), highlightPlayerBtn.x, highlightPlayerBtn.y, paint);
            canvas.drawBitmap(senseBtn.getBitmap(), senseBtn.x, senseBtn.y, paint);

            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            paint.setAlpha(100);

            //text area
            canvas.drawRect(screenX - 400, screenY - 250,
                    screenX, screenY, paint);

            //display red critters left
            paint.setColor(Color.RED);
            String text = "Critters Alive: " + playerCrittersAlive + " / " + (critters.size());
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int height = bounds.height();
            int width = bounds.width();
            canvas.drawText(text, screenX - width - 10, screenY - height, paint);

            paint.setColor(Color.GREEN);
            text = "Critters Alive: " + greenCrittersAlive + " / " + (critters.size());
            bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            height = bounds.height();
            width = bounds.width();
            canvas.drawText(text, screenX - width - 10, screenY - height - 50, paint);

            paint.setColor(Color.BLUE);
            text = "Critters Alive: " + blueCrittersAlive + " / " + (critters.size());
            bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            height = bounds.height();
            width = bounds.width();
            canvas.drawText(text, screenX - width - 10, screenY - height - 100, paint);

            paint.setColor(Color.YELLOW);
            text = "Critters Alive: " + yellowCrittersAlive + " / " + (critters.size());
            bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            height = bounds.height();
            width = bounds.width();
            canvas.drawText(text, screenX - width - 10, screenY - height - 150, paint);

            //display days
            paint.setColor(Color.WHITE);
            text = "Day: " + days;
            paint.getTextBounds(text, 0, text.length(), bounds);
            height = bounds.height();
            width = bounds.width();
            canvas.drawText(text,  10, screenY - height - 10, paint);

            getHolder().unlockCanvasAndPost(canvas); //display canvas
        }

    }

    public void sleep(){
        //1000 millis / 17 millis = 60fps
        try {
            Thread.sleep(17);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        //highlight players selected
        if (highlightPlayerBtn.getRect().contains(x, y) && event.getAction() == MotionEvent.ACTION_UP) //action up makes button less sensitive
        {
            highlightPlayer = !highlightPlayer;
        }

        //show sight button selected
        if(senseBtn.getRect().contains(x, y) && event.getAction() == MotionEvent.ACTION_UP){
            showVision= !showVision;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distX = e2.getX() - e1.getX();
        float distY = e2.getY() - e1.getY();

        cameraX += distX /20;
        cameraY += distY /20;

        //set camera limits
        if(cameraX > 0){
            cameraX = 0;
        }
        if(cameraX < -(borderX - screenX)){
            cameraX = -(borderX - screenX);
        }
        if(cameraY > 0){
            cameraY = 0;
        }
        if(cameraY < -(borderY - screenY)){
            cameraY = -(borderY - screenY);
        }
        //canvas.translate(distX, distY);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

}
