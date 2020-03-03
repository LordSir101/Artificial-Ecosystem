package net.ontariotechu.automata;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Species {
    public int size, speed, sense, breed;
    public int x, y, width, height;
    private float ratioX, ratioY;
    Bitmap breedPart, speedPart, sensePart;
    Bitmap critter;
    Random random;
    private int goalX, goalY;
    private List<Point> path;
    private int placeOnPath;
    public int color;
    //private int isResting = 0;
    public float orientation = 0;
    private boolean isTurning = true;
    private float prevRot = 0;
    private float finalRotation;
    private boolean seesFood = false;
    public int energy = 500;


    public Species(Resources res, int size, int speed, int sense, int breed, float screenRatioX, float screenRatioY, int color){
        this.size = size;
        this.sense = sense;
        this.breed = breed;
        this.speed = speed;
        this.ratioX = screenRatioX;
        this.ratioY = screenRatioY;
        this.color = color;

        //the player has a different border

        speedPart = BitmapFactory.decodeResource(res, R.drawable.speed);
        sensePart = BitmapFactory.decodeResource(res, R.drawable.sense);
        breedPart = BitmapFactory.decodeResource(res, R.drawable.breed);

        critter = createCritter();

        width = critter.getWidth();
        height = critter.getHeight();

        width = (int) (width * ratioX * 0.25f);
        height = (int) (height * ratioY * 0.25f);

        critter = Bitmap.createScaledBitmap(critter, width, height, false);

        random = new Random();

        //random starting position
        x = random.nextInt(GameView.screenX);
        y = random.nextInt(GameView.screenY);

        getGoal();

    }

    //Critter creation---------------------------------------------------------------------------------------------------------------------------------
    private Bitmap createCritter(){
        /*The critters are a maximum of 3x3 squares.  Based on its size and type of parts
        various colored squares are put together to create a single bitmap of a critter.
        Even body and odd body sizes look different so they require different functions
         */

        int resW, resH;
        //find how big the result bitmap needs to be
        if(size < 3){
            resW = 2;
            resH = 1;
        }
        else if(size == 3){
            resW = 3;
            resH = 1;
        }
        else if(size >= 4 && size <= 6){
            resW = 3;
            resH = 2;
        }
        else{
            resW = 3;
            resH = 3;
        }
        Bitmap result = Bitmap.createBitmap(speedPart.getWidth() * resW, speedPart.getHeight() * resH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        //start in middle top of 3x3 grid
        int[] position = new int[2];
        position[0] = speedPart.getWidth(); //x coordinate
        position[1] = 0; //y coordinate
        int partNum = 2; //keeps track of how many parts were drawn

        //draw speed parts
        for (int i = 1; i <= speed; i++) {
            canvas.drawBitmap(speedPart, position[0], position[1], paint);
            if(size % 2 == 0){
                position = updateEvenPos(partNum); //the next position of a square based on even body size
            }
            else{
                position = updateOddPos(partNum);//the next position of a square based on odd body size
            }

            partNum++;
        }

        //draw sense parts
        for (int i = 1; i <= sense; i++) {
            canvas.drawBitmap(sensePart, position[0], position[1], paint);
            if(size % 2 == 0){
                position = updateEvenPos(partNum);
            }
            else{
                position = updateOddPos(partNum);
            }

            partNum++;
        }

        //draw breed parts
        for (int i = 1; i <= breed; i++) {
            canvas.drawBitmap(breedPart, position[0], position[1], paint);
            if(size % 2 == 0){
                position = updateEvenPos(partNum);
            }
            else{
                position = updateOddPos(partNum);
            }

            partNum++;
        }
        return result;
    }

    //updates position on canvas to draw an even size critter
    public int[] updateEvenPos(int partNum){
        int[] result = new int[2];
        switch(partNum){
            case 2:
                result[0] = 0;
                result[1] = 0;
                return result;

            case 3:
                result[0] = speedPart.getWidth() * 2;
                result[1] = 0;
                return result;

            case 4:
                result[0] = speedPart.getWidth();
                result[1] = speedPart.getHeight();
                return result;

            case 5:
                result[0] = speedPart.getWidth() * 2;
                result[1] = speedPart.getHeight();
                return result;

            case 6:
                result[0] = 0;
                result[1] = speedPart.getHeight();
                return result;

            case 7:
                result[0] = 0;
                result[1] = speedPart.getHeight() * 2;
                return result;

            default:
                break;
        }
        result[0] = speedPart.getWidth() * 2;
        result[1] = speedPart.getHeight() * 2;
        return result;
    }

    //updates position on canvas to draw even size critter
    public int[] updateOddPos(int partNum){
        int[] result = new int[2];
        switch(partNum){
            case 2:
                result[0] = 0;
                result[1] = 0;
                return result;

            case 3:
                result[0] = speedPart.getWidth() * 2;
                result[1] = 0;
                return result;

            case 4:
                result[0] = 0;
                result[1] = speedPart.getHeight();
                return result;

            case 5:
                result[0] = speedPart.getWidth() * 2;
                result[1] = speedPart.getHeight();
                return result;

            case 6:
                result[0] = speedPart.getWidth();
                result[1] = speedPart.getHeight();
                return result;

            case 7:
                result[0] = speedPart.getWidth();
                result[1] = speedPart.getHeight() * 2;
                return result;

            case 8:
                result[0] = 0;
                result[1] = speedPart.getHeight() * 2;
                return result;

            default:
                break;

        }

        result[0] = speedPart.getWidth() * 2;
        result[1] = speedPart.getHeight() * 2;
        return result;
    }

    //movement-------------------------------------------------------------------------------------------------------------------------------------
    public void getPath(){

        // Bresenham's line algorithm

        path = new ArrayList<Point>();
        placeOnPath = 0; //reset position on path to 0
        int x0 = x;
        int y0 = y;

        int x1 = goalX;
        int y1 = goalY;

        int sx = 0;
        int sy = 0;

        int dx =  Math.abs(x1-x0);
        sx = x0<x1 ? 1 : -1;
        int dy = -1*Math.abs(y1-y0);
        sy = y0<y1 ? 1 : -1;
        int err = dx+dy;
        int e2; /* error value e_xy */

        //add all points from the line into the path
        for(;;){
            path.add(new Point(x0, y0));
            if (x0==x1 && y0==y1){
                break;
            }
            e2 = 2*err;
            if (e2 >= dy) { err += dy; x0 += sx; } /* e_xy+e_x > 0 */
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    public void move() {

        if(!isTurning) {
            getGoal();

            //the critter moves to the next point on their path multiplied by their speed
            placeOnPath += (speed + 2);

            //if they would go past the last point, they simply go to the last point instead
            if (placeOnPath > path.size() - 1) {
                placeOnPath = path.size() - 1;
            }

            x = path.get(placeOnPath).x;
            y = path.get(placeOnPath).y;

            //when they finish their path, a new goal is set and if there is food, they eat it
            if(placeOnPath == path.size() -1){
                checkIfFoodEaten();
                seesFood = false;
                isTurning = true;
                getGoal();
            }

        }
        else{
            rotate();
        }
    }

    private void checkIfFoodEaten() {

        for(Food food : GameView.food){
            if(getCritterRect().contains(food.getFoodRect())){
                food.isEaten = true;
                energy += food.energy;
            }
        }
    }

    private void getGoal(){
        //goalX = random.nextInt(GameView.screenX);
        //goalY = random.nextInt(GameView.screenY);
        float closestDistance = Float.MAX_VALUE;
        for(Food food: GameView.food){
            //can see returns the distance of the food if it is within the critter's sense range
            //otherwise it returns float max
            float currentDistance = canSee(food.x, food.y); //distance to center of food
            if(currentDistance < closestDistance){
                closestDistance = currentDistance;
                goalX = food.x;
                goalY = food.y;
                seesFood = true;
                isTurning = true;
                getPath();
                getRotationGoal();
            }
        }
        if(!seesFood && isTurning){ //only get a new goal if it is not moving
            goalX = random.nextInt(GameView.screenX);
            goalY = random.nextInt(GameView.screenY);
            getPath();
            getRotationGoal();
        }

    }

    private void getRotationGoal(){

        double dx = goalX - x;
        double dy = (goalY - y) * -1;
        double deg = Math.atan2(dy, dx) * 180 / Math.PI;
        float result;

        //straight up/down
        if(dx < 1 && dx > -1){
            result =  dy < 0 ? orientation * -1 + 180 : orientation * -1;
        }
        //straight side
        else if(dy < 1 && dy > -1){
            result =  dx < 0 ? orientation * -1 - 90 : orientation * -1 + 90;
        }
        else if(dx < 0 && dy < 0){
            result = (float) (orientation * -1 + (90 - deg));
        }
        else if(dy < 0){
            result = (float) (orientation * -1 + 90 + Math.abs(deg));
        }
        else if(dx < 0){
            result = (float) (orientation * -1 + (90 - deg));
        }
        else{
            result = (float) (orientation * -1 + 90 - deg);
        }

        finalRotation = orientation + result;

    }

    private void rotate(){
        //orientation += 10;
        float amountRotated = (finalRotation - prevRot) / 15;
        orientation += amountRotated;

        if(orientation < finalRotation + 10 && orientation > finalRotation - 10 ){
            prevRot = orientation;
            isTurning = false;
        }
    }

    private float canSee(int x, int y){
        float radius = (sense + 1) * 10 + 50f;
        int dx = x - this.x;
        int dy = y - this.y;
        double sum = Math.pow(dx, 2) + Math.pow(dy, 2);
        float distance = (float) Math.sqrt(sum);

        if(distance < radius){
            return distance;
        }
        else{
            return Float.MAX_VALUE;
        }

    }

    public RectF getCritterRect(){
        return new RectF(x, y, x + this.width, y + this.height);
    }

    public void reduceEnergy(){
        energy -= size * Math.pow((speed + 1), 2) + sense;
    }
}
