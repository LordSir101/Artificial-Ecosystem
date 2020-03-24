package net.ontariotechu.automata;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Food {

    int x, y, energy, width, height;
    Bitmap food;
    Random random = new Random();
    public boolean isEaten = false;

    public Food(Resources res){
        x = random.nextInt(GameView.screenX);
        y = random.nextInt(GameView.screenY);
        energy = 150;

        food = BitmapFactory.decodeResource(res, R.drawable.food);
        width = food.getWidth();
        height = food.getHeight();

        width = (int) (width * GameView.screenRatioX * 0.1);
        height = (int) (height * GameView.screenRatioY * 0.1);

        food = Bitmap.createScaledBitmap(food, width, height, false);
    }

    public RectF getFoodRect(){
        return new RectF(x, y, x + this.width, y + this.height);
    }
}
