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


public class GameView extends SurfaceView implements Runnable {

    private final int startPop = 7;
    private int startFood = 50;
    private boolean isPlaying;
    private Thread thread;
    public static float screenRatioX, screenRatioY;
    private Species player;
    public static int screenX, screenY;
    private Paint paint;
    private List<Species> critters;
    public static List<Food> food;
    private Random random;
    private Button highlightPlayerBtn;
    private boolean highlightPlayer = false;
    private int timePassed = 0;
    //private int counter = 0;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.screenX = screenX;
        this.screenY = screenY;
        food = new ArrayList<>();

        //adjust size of critters based on scrren size using an arbitrary size as a baseline
        screenRatioX = screenX / 1920f;
        screenRatioY = screenY / 1080f ;

        random = new Random();
        critters = new ArrayList<>();

        //create player species
        for(int i = 0; i < startPop; i++){
            player = new Species(getResources(), activity.size,
                    activity.speed, activity.sense, activity.breed,
                    screenRatioX, screenRatioY, 1);

            player.x = random.nextInt(screenX);
            player.y = random.nextInt(screenY);
            critters.add(player);
        }


        //spawn food
        for(int i = 0; i < startFood; i++){
            Food pellet = new Food(getResources());
            food.add(pellet);
        }

        //create player highlight toggle
        Bitmap btnImg = BitmapFactory.decodeResource(getResources(), R.drawable.exclamation);
        highlightPlayerBtn = new Button(btnImg.getWidth(), btnImg.getHeight(), btnImg);
        highlightPlayerBtn.x = screenX - highlightPlayerBtn.getBitmap().getWidth() -10;
        highlightPlayerBtn.y = 10;

        paint = new Paint();

        generateRandomSpecies(3);
    }

    private void generateRandomSpecies(int numSpecies) {

        int color = 2;
        for(int i = 0; i < numSpecies; i++){

            //random starting stats.  Maximum of 9 stats total
            //each stat is guaranteed a chance at having a value of 2
            int speed = random.nextInt(5);
            int sense = random.nextInt(7 - speed);
            int breed = random.nextInt(9 - speed - sense);
            int size = speed + sense + breed;


            for(int j = 0; j < startPop; j++){
                Species species = new Species(getResources(), size,
                        speed, sense, breed,
                        screenRatioX, screenRatioY, color);

                species.x = random.nextInt(screenX);
                species.y = random.nextInt(screenY);
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
        //move critters
        for(int i = critters.size() -1; i >= 0; i--){
            critters.get(i).move();

            if(timePassed % 60 == 0){
                critters.get(i).reduceEnergy();

            }
            if(critters.get(i).energy <= 0){
                critters.remove(i);
            }
        }

        //remove eaten food
        for(int i = food.size() -1; i >= 0; i--){
            if(food.get(i).isEaten){
                food.remove(i);
            }
        }

        //add 30 food every 5 seconds
        if(timePassed > 60 * 2){
            for(int i = 0; i < 15; i++){
                Food pellet = new Food(getResources());
                food.add(pellet);
            }
            timePassed = 0;
        }

        timePassed++;
    }

    public void draw(){
        if(getHolder().getSurface().isValid()){
            Canvas canvas = getHolder().lockCanvas(); //returns canvas being displayed on screen

            paint.setColor(Color.BLACK);
            //paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.FILL);

            //draw background
            canvas.drawRect(0, 0, screenX, screenY, paint);

            for(Food food: food){
                canvas.drawBitmap(food.food, food.x, food.y, paint);
            }

            for(Species critter : critters){
                if(highlightPlayer){
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

                    //draw border around player species
                    canvas.save();
                    canvas.rotate(critter.orientation, critter.x + critter.width/2, critter.y + critter.height/2);
                    canvas.drawRect(critter.x -1, critter.y -1, critter.x + critter.width + 1, critter.y + critter.height + 1, paint);
                    canvas.restore();
                }

                //rotate and draw critter
                canvas.save();
                canvas.rotate(critter.orientation, critter.x + critter.width/2, critter.y + critter.height/2);
                canvas.drawBitmap(critter.critter, critter.x, critter.y, paint);
                canvas.restore();
                /*
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(critter.x + critter.width/2, critter.y + critter.height/2, (critter.sense ) * 20 + 70f, paint);
                paint.setColor(Color.WHITE);*/
            }

            canvas.drawBitmap(highlightPlayerBtn.getBitmap(), highlightPlayerBtn.x, highlightPlayerBtn.y, paint);

            //display critters left
            String text = "Critters Alive: " + (critters.size() - 1);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            drawText(text, screenX - 50, screenY - 50, canvas);

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

        float x = event.getX();
        float y = event.getY();
        if (highlightPlayerBtn.getRect().contains(x, y) && event.getAction() == MotionEvent.ACTION_UP) //action up makes button less sensitive
        {
            highlightPlayer = !highlightPlayer;
        }
        return true;
    }

    public void drawText(String text, int x, int y, Canvas canvas){
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.height();
        int width = bounds.width();
        canvas.drawText(text, x - width, y - height, paint);

    }
}
