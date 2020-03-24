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
    public int size, speed, sense, breed, movement;
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
    public int energy = 150;
    public boolean hasCollided = false;
    private int reboundCounter = 0;
    public int isRebounding = 0;
    //public boolean isRebounding = false;
    private int prevX = Integer.MAX_VALUE, prevY = Integer.MAX_VALUE, stuckCounter;
    public RectF rect;
    private float prevRectRot = orientation;
    private boolean isColliding = false;


    public Species(Resources res, int size, int speed, int sense, int breed, float screenRatioX, float screenRatioY, int color){
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
        x = random.nextInt(GameView.screenX);
        y = random.nextInt(GameView.screenY);

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

    //updates position on canvas to draw even size critter
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
            //isIntersectingBefore = checkCollisions(GameView.critters); //check if was intersecting before moving

            //if they would go past the last point, they simply go to the last point instead
            if (placeOnPath > path.size() - 1) {
                placeOnPath = path.size() - 1;
            }

            x = path.get(placeOnPath).x;
            y = path.get(placeOnPath).y;


            if(x > GameView.screenX){
                x = GameView.screenX;
            }
            else if(x < 0){
                x = 0;
            }
            if(y > GameView.screenY){
                y = GameView.screenY;
            }
            else if(y < 0){
                y = 0;
            }
            //when they finish their path, a new goal is set and if there is food, they eat it
            if(placeOnPath == path.size() -1){
                checkIfFoodEaten();
                seesFood = false;
                isTurning = true;
                //isRebounding = 0;

                //after colliding, a random goal is set to prevent clusters of critters
                if(hasCollided){
                    goalX = random.nextInt(GameView.screenX );
                    goalY = random.nextInt(GameView.screenY );

                    getPath();
                    getRotationGoal();
                    //movement = 2 * speed + 2;
                    hasCollided = false;
                }
                else{
                    getGoal();
                }

            }
            //checkIfStuck();
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
            goalX = random.nextInt(GameView.screenX);
            goalY = random.nextInt(GameView.screenY );
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
            //RectF critterRect = critter.getCritterRect();
            if (critter == this) {
                continue;
            }
            //check if there is a collision
            if (this.rect.intersect(critter.getCritterRect())) {
                getReboundGoal(critter, 10000); //for decoupling
                //System.out.print("decoupled");
                getReboundGoal(critter, 80);//for actual rebound distance

                hasCollided = true;

                //if this is larger than critter, the other critter will be set on a rebound path
                /*
                if (this.size > critter.size) {
                    critter.hasCollided = true;

                    //upper right
                    if (this.x >= critter.x && this.y <= critter.y) {
                        critter.goalX = critter.x - (int) (hyp * Math.cos(angle));
                        critter.goalY = critter.y + (int) (hyp * Math.sin(angle));

                    }
                    //lower right
                    else if (this.x >= critter.x && this.y >= critter.y) {
                        critter.goalX = critter.x - (int) (hyp * Math.cos((angle)));
                        critter.goalY = critter.y - (int) (hyp * Math.sin(angle));

                    }
                    //lower left
                    else if (this.x <= critter.x && this.y >= critter.y) {
                        critter.goalX = critter.x + (int) (hyp * Math.cos(angle));
                        critter.goalY = critter.y - (int) (hyp * Math.sin(angle));

                    } else if (this.x < critter.x && this.y <= critter.y) {
                        critter.goalX = critter.x + (int) (hyp * Math.cos(angle));
                        critter.goalY = critter.y + (int) (hyp * Math.sin(angle));

                    }
                    keepGoalInPlayArea();
                    //critter.isTurning = false;
                    critter.getPath();

                    while (this.getCritterRect().intersect(critter.getCritterRect())) {
                        critter.placeOnPath += 1;
                        critter.x = critter.path.get(critter.placeOnPath).x;
                        critter.y = critter.path.get(critter.placeOnPath).y;
                    }

                    //critter.hasCollided = true;
                    //critter.isRebounding = true;
                    //critter.isRebounding = this.speed;

                    //critter.seesFood = false;
                }*/
                //the species will rebound because it is either smaller or got hit
                /*
                else {
                    this.hasCollided = true;

                    if (critter.x >= this.x && critter.y <= this.y) {
                        goalX = x - (int) (hyp * Math.cos(angle));
                        goalY = y + (int) (hyp * Math.sin(angle));

                    } else if (critter.x >= this.x && critter.y >= this.y) {
                        goalX = x - (int) (hyp * Math.cos(angle));
                        goalY = y - (int) (hyp * Math.sin(angle));

                    } else if (critter.x <= this.x && critter.y >= this.y) {
                        goalX = x + (int) (hyp * Math.cos(angle));
                        goalY = y - (int) (hyp * Math.sin(angle));

                    } else if (critter.x <= this.x && critter.y <= this.y) {
                        goalX = x - (int) (hyp * Math.cos(angle));
                        goalY = y + (int) (hyp * Math.sin(angle));

                    }
                    //isTurning = false;
                    keepGoalInPlayArea();
                    getPath();
                    //TODO figure out why this loop goes infinite

                    while (this.getCritterRect().intersect(critter.getCritterRect())) {
                        placeOnPath += 1;
                        x = path.get(placeOnPath).x;
                        y = path.get(placeOnPath).y;
                    }

                    //hasCollided = false;
                    //isRebounding = true;
                    //isRebounding = critter.speed;
                    //seesFood = false;
                }*/

            }

            //isColliding = false;
        }
            //return collision;
    }

    public void getReboundGoal(Species critter, int hyp){
        //if this is larger than critter, the other critter will be set on a rebound path
        float angle = 45;

        if (this.size > critter.size) {
            //critter.hasCollided = true;
            critter.setCollided();

            //upper right
            if (this.x >= critter.x && this.y <= critter.y) {
                //critter.goalX = critter.x - (int) (hyp * Math.cos(angle));
                //critter.goalY = critter.y + (int) (hyp * Math.sin(angle));
                critter.setGoalX(critter.x - (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y + (int) (hyp * Math.sin(angle)));

            }
            //lower right
            else if (this.x >= critter.x && this.y >= critter.y) {
                //critter.goalX = critter.x - (int) (hyp * Math.cos((angle)));
                //critter.goalY = critter.y - (int) (hyp * Math.sin(angle));
                critter.setGoalX(critter.x - (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y - (int) (hyp * Math.sin(angle)));

            }
            //lower left
            else if (this.x <= critter.x && this.y >= critter.y) {
                //critter.goalX = critter.x + (int) (hyp * Math.cos(angle));
                //critter.goalY = critter.y - (int) (hyp * Math.sin(angle));
                critter.setGoalX(critter.x + (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y - (int) (hyp * Math.sin(angle)));

            } else if (this.x < critter.x && this.y <= critter.y) {
                //critter.goalX = critter.x + (int) (hyp * Math.cos(angle));
                //critter.goalY = critter.y + (int) (hyp * Math.sin(angle));
                critter.setGoalX(critter.x + (int) (hyp * Math.cos(angle)));
                critter.setGoalY(critter.y + (int) (hyp * Math.sin(angle)));

            }
            //keepGoalInPlayArea();
            //critter.isTurning = false;
            critter.getPath();

            while (this.getCritterRect().intersect(critter.getCritterRect())) {
                critter.incPlaceOnPath();
                critter.setX(critter.path.get(critter.placeOnPath).x);
                critter.setY(critter.path.get(critter.placeOnPath).y);
            }
        }

        else {
            this.hasCollided = true;

            if (critter.x >= this.x && critter.y <= this.y) {
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y + (int) (hyp * Math.sin(angle));

            } else if (critter.x >= this.x && critter.y >= this.y) {
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y - (int) (hyp * Math.sin(angle));

            } else if (critter.x <= this.x && critter.y >= this.y) {
                goalX = x + (int) (hyp * Math.cos(angle));
                goalY = y - (int) (hyp * Math.sin(angle));

            }
            else if (critter.x <= this.x && critter.y <= this.y) {
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y + (int) (hyp * Math.sin(angle));

            }

            //keepGoalInPlayArea();
            //critter.isTurning = false;
            getPath();

            while (this.getCritterRect().intersect(critter.getCritterRect())) {
                placeOnPath += 1;
                x = path.get(placeOnPath).x;
                y = path.get(placeOnPath).y;
            }
        }

    }
    public void setCollisionPosition(Species current, Species other){

        int v1 = current.movement;
        int m1 = current.size;
        int v2 = other.movement;
        int m2 = other.size;

        int firstHalf = ((m1 - m2)/(m1 + m2))*v1;
        int secondHalf = ((2 * m2)/(m1 + m2))*v2;
        current.movement = firstHalf + secondHalf;

        firstHalf = ((2 * m1)/(m1 + m2))*v1;
        secondHalf = ((m2 - m1)/(m1 + m2))*v2;
        other.movement = firstHalf + secondHalf;

        float theta = 180 - current.orientation;
        current.goalX = (int)(current.movement * Math.sin(theta));
        current.goalY = (int)(current.movement * Math.cos(theta));

        theta = 180 - other.orientation;
        other.goalX = (int)(current.movement * Math.sin(theta));
        other.goalY = (int)(current.movement * Math.cos(theta));


        current.getPath();
        other.getPath();
        current.hasCollided = true;
        other.hasCollided = true;

    }
/*
    private void rebound(Species critter){

        int hyp = 60;
        int angle = 45;

        if(this.size > critter.size){
            critter.hasCollided = true;

            //upper right
            if(this.x >= critter.x && this.y <= critter.y){
                critter.goalX = critter.x - (int) (hyp * Math.cos(angle));
                critter.goalY = critter.y + (int) (hyp * Math.sin(angle));

            }
            //lower right
            else if(this.x >= critter.x && this.y >= critter.y){
                critter.goalX = critter.x - (int) (hyp * Math.cos((angle)));
                critter.goalY = critter.y - (int) (hyp * Math.sin(angle));

            }
            //
            else if(this.x <= critter.x && this.y >= critter.y){
                critter.goalX = critter.x + (int) (hyp * Math.cos(angle));
                critter.goalY = critter.y + (int) (hyp * Math.sin(angle));

            }
            else if(this.x < critter.x && this.y <= critter.y){
                critter.goalX = critter.x + (int) (hyp * Math.cos(angle));
                critter.goalY = critter.y - (int) (hyp * Math.sin(angle));

            }
            keepGoalInPlayArea();
            //critter.isTurning = false;
            critter.getPath();
            //critter.hasCollided = false;
            //critter.isRebounding = true;
            critter.isRebounding = this.speed;

            //critter.seesFood = false;
        }
        //the species will rebound because it is either smaller or got hit
        else {
            this.hasCollided = true;

            if(critter.x >= this.x && critter.y <= this.y){
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y + (int) (hyp * Math.sin(angle));

            }
            else if(critter.x >= this.x && critter.y >= this.y){
                goalX = x - (int) (hyp * Math.cos(angle));
                goalY = y - (int) (hyp * Math.sin(angle));

            }
            else if(critter.x <= this.x && critter.y >= this.y){
                goalX = x + (int) (hyp * Math.cos(angle));
                goalY = y + (int) (hyp * Math.sin(angle));

            }
            else if(critter.x <= this.x && critter.y <= this.y){
                goalX = x + (int) (hyp * Math.cos(angle));
                goalY = y - (int) (hyp * Math.sin(angle));

            }
            //isTurning = false;
            keepGoalInPlayArea();
            getPath();
            //hasCollided = false;
            //isRebounding = true;
            isRebounding = critter.speed;
            //seesFood = false;
        }

    }*/

    public void setPosition(List<Species> critters){
        x = random.nextInt(GameView.screenX);
        y = random.nextInt(GameView.screenY);

        for(Species critter : critters){
            if(critter.getCritterRect().contains(x , y)){
                setPosition(critters);
            }
        }
    }

    //if critters get stuck, they will respawn somewhere new
    private void checkIfStuck(){
        if(prevX == x && prevY == y){
            stuckCounter++;
        }
        if(stuckCounter > 60 * 5){
            setPosition(GameView.critters);
            stuckCounter = 0;
        }

        prevX = x;
        prevY = y;

    }

    private void keepGoalInPlayArea(){
        if(goalX > GameView.screenX){
            goalX = GameView.screenX;
        }
        else if(goalX < 0){
            goalX = 0;
        }
        if(goalY > GameView.screenY){
            goalY = GameView.screenY;
        }
        else if(goalY < 0){
            goalY = 0;
        }
    }

    //setter methods for collisions
    public void setX(int x){ this.x = x;}
    public void setY(int y){this.y = y;}
    public void setGoalX(int x){this.goalX = x;}
    public void setGoalY(int y){this.goalY = y;}
    public void incPlaceOnPath(){this.placeOnPath++;}
    public void setCollided(){this.hasCollided = true;}

}
