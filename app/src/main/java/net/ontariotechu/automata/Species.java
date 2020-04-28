package net.ontariotechu.automata;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
speed is a linear increase
speed has a multiplicative energy cost

sense is an extra 1/3 of the base per point
sense has additive energy cost

size has multiplicative energy cost

breed costs energy per child
 */

public class Species {
    //critter stats
    public int size, speed, sense, breed, movement;
    public int x, y, width, height;
    public int color;
    private Resources res;
    public float orientation = 0;
    private boolean isTurning = true;
    private float prevRot = 0;
    private float finalRotation;
    private float prevRectRot = orientation;
    private int goalX, goalY;
    private List<Point> path;
    private int placeOnPath;
    private boolean seesFood = false;
    public int energy = 150;
    public boolean hasCollided = false;

    //images
    Bitmap breedPart, speedPart, sensePart;
    Bitmap critter;

    private float ratioX, ratioY;
    Random random;
    public RectF rect;


    public Species(Resources res, int size, int speed, int sense, int breed, float screenRatioX, float screenRatioY, int color){
        this.res = res;
        this.size = size;
        this.sense = sense;
        this.breed = breed;
        this.speed = speed;
        this.ratioX = screenRatioX;
        this.ratioY = screenRatioY;
        this.color = color;
        this.movement = speed * 2 + 2;

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
        x = random.nextInt(GameView.borderX);
        y = random.nextInt(GameView.borderY);

        rect = getCritterRect();
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
        if(size==1){
            resW = 1;
            resH = 1;
        }
        else if(size < 3){
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
        position[0] = 0; //x coordinate
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
                result[0] = speedPart.getWidth();
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

    //updates position on canvas to draw odd size critter
    public int[] updateOddPos(int partNum){
        int[] result = new int[2];
        switch(partNum){
            case 2:
                result[0] = speedPart.getWidth();
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
            //Species isIntersecting = null;
            //Species isIntersectingBefore = null; //checks intersection before movement


            if(!hasCollided){getGoal();}
            //the critter moves to the next point on their path multiplied by their speed
            placeOnPath += (movement);

            //if they would go past the last point, they simply go to the last point instead
            if (placeOnPath > path.size() - 1) {
                placeOnPath = path.size() - 1;
            }

            x = path.get(placeOnPath).x;
            y = path.get(placeOnPath).y;


            if(x > GameView.borderX){
                x = GameView.borderX;
            }
            else if(x < 0){
                x = 0;
            }
            if(y > GameView.borderY){
                y = GameView.borderY;
            }
            else if(y < 0){
                y = 0;
            }
            //when they finish their path, a new goal is set and if there is food, they eat it
            if(placeOnPath == path.size() -1) {
                checkIfFoodEaten();
                seesFood = false;
                isTurning = true;

                //after colliding, a random goal is set to prevent clusters of critters
                if (hasCollided) {
                    goalX = random.nextInt(GameView.borderX);
                    goalY = random.nextInt(GameView.borderY);

                    getPath();
                    getRotationGoal();
                    hasCollided = false;
                } else {
                    getGoal();
                }

            }
        }
        else{
            rotate();
        }
    }

    private void checkIfFoodEaten() {

        for(Food food : GameView.food){
            if(getCritterRect().intersect(food.getFoodRect())){
                food.isEaten = true;
                energy += food.energy;
            }
        }
    }

    private void getGoal(){
        //goalX = random.nextInt(GameView.screenX);
        //goalY = random.nextInt(GameView.screenY);

        //A species will constantly look for food.  If they see food, that becomes their goal and they no longer look for food
        //If they do not see food and they stopped moving, a new random goal is set
        if(!seesFood) {
            float closestDistance = Float.MAX_VALUE;
            for (Food food : GameView.food) {
                //can see returns the distance of the food if it is within the critter's sense range
                //otherwise it returns float max
                float currentDistance = canSee(food.x, food.y); //distance to center of food
                if (currentDistance < closestDistance) {
                    closestDistance = currentDistance;
                    goalX = food.x;
                    goalY = food.y;
                    seesFood = true;
                    isTurning = true;
                    getPath();
                    getRotationGoal();
                }
            }
        }
        if(!seesFood && isTurning){ //only get a new goal if it is not moving
            goalX = random.nextInt(GameView.borderX);
            goalY = random.nextInt(GameView.borderY);
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
        //we use distance and radius  squared since we only need relative distances and sqrt is expensive

        float radiusSqrd = (float)Math.pow(((sense) * 50 + 150f), 2);
        int dx = x - this.x;
        int dy = y - this.y;
        float distanceSqrd =  (float) (Math.pow(dx, 2) + Math.pow(dy, 2));
        //float  = (float) Math.sqrt(sum);

        if(distanceSqrd < radiusSqrd){
            return distanceSqrd;
        }
        else{
            return Float.MAX_VALUE;
        }

    }

    public RectF getCritterRect(){

        RectF r2 = new RectF(x - width/2, y - height/2, x + this.width/2, y + this.height/2);

        //Rotation is expensive.  Only rotate if orientation changes
        if(prevRectRot != orientation) {
            Matrix m = new Matrix();
            // point is the point about which to rotate.
            m.setRotate(orientation, x, y);
            m.mapRect(r2);
            prevRectRot = orientation;
        }
        return r2;
        //return new RectF(x - width/2, y - height/2, x + this.width/2, y + this.height/2);

    }

    public void reduceEnergy(){
        //energy -= size * Math.pow((speed + 1), 2) + sense;
        energy -= size * (speed + 1) + sense;
    }

    public void checkCollisions(List<Species> critters) {
        int hyp = 200;
        float angle = 45;//180 - orientation;

        rect = getCritterRect();
        //Species collision = null;
        for (Species critter : critters) {

            if (critter == this) {
                continue;
            }
            //check if there is a collision
            if (this.rect.intersect(critter.getCritterRect())) {
                getReboundGoal(critter, 1000); //this lets the critter move until it no longer intersects the other
                getReboundGoal(critter, 80);//this makes the critter actually rebound

                hasCollided = true;

            }

        }

    }

    public void getReboundGoal(Species critter, int hyp){

        float angle = 45;

        //if this is larger than critter, the other critter will be set on a rebound path
        if (this.size > critter.size) {

            critter.setCollided();

            //upper right
            if (this.x >= critter.x && this.y <= critter.y) {

                critter.setGoalX(critter.x - (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y + (int) (hyp * Math.sin(angle)));

            }
            //lower right
            else if (this.x >= critter.x && this.y >= critter.y) {

                critter.setGoalX(critter.x - (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y - (int) (hyp * Math.sin(angle)));

            }
            //lower left
            else if (this.x <= critter.x && this.y >= critter.y) {

                critter.setGoalX(critter.x + (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y - (int) (hyp * Math.sin(angle)));

            }
            //upper left
            else if (this.x < critter.x && this.y <= critter.y) {

                critter.setGoalX(critter.x + (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y + (int) (hyp * Math.sin(angle)));

            }

            critter.getPath();//this path is the rebound that the critter will make

            //moves the critter back until they no longer intersect so that the sprites do not clip
            while (this.getCritterRect().intersect(critter.getCritterRect())) {
                critter.incPlaceOnPath();
                critter.setX(critter.path.get(critter.placeOnPath).x);
                critter.setY(critter.path.get(critter.placeOnPath).y);
            }
        }

        else {
            this.hasCollided = true;

            //upper right
            if (critter.x >= this.x && critter.y <= this.y) {
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y + (int) (hyp * Math.sin(angle));

            }
            //lower right
            else if (critter.x >= this.x && critter.y >= this.y) {
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y - (int) (hyp * Math.sin(angle));

            }
            //lower left
            else if (critter.x <= this.x && critter.y >= this.y) {
                goalX = x + (int) (hyp * Math.cos(angle));
                goalY = y - (int) (hyp * Math.sin(angle));

            }
            //upper left
            else if (critter.x <= this.x && critter.y <= this.y) {
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y + (int) (hyp * Math.sin(angle));

            }

            getPath();

            while (this.getCritterRect().intersect(critter.getCritterRect())) {
                placeOnPath += 1;
                x = path.get(placeOnPath).x;
                y = path.get(placeOnPath).y;
            }
        }

    }


    public void setPosition(List<Species> critters){
        x = random.nextInt(GameView.borderX);
        y = random.nextInt(GameView.borderY);

        for(Species critter : critters){
            if(critter.getCritterRect().contains(x , y)){
                setPosition(critters);
            }
        }
    }

    public void makeBaby(List<Species> critters){
        Species baby = new Species(this.res, this.size, this.speed, this.sense, this.breed, this.ratioX, this.ratioY, this.color);
        baby.x = this.x;
        baby.y = this.y;
        this.energy -= 150;
        critters.add(baby);
    }

    //setter methods for collisions
    public void setX(int x){ this.x = x;}
    public void setY(int y){this.y = y;}
    public void setGoalX(int x){this.goalX = x;}
    public void setGoalY(int y){this.goalY = y;}
    public void incPlaceOnPath(){this.placeOnPath++;}
    public void setCollided(){this.hasCollided = true;}

}
