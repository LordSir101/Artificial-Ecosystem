package net.ontariotechu.automata;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

public class Button {

    int width;
    int height;
    Bitmap bg;
    int x, y;

    public Button(float width, float height, Bitmap bg)
    {
        this.width = (int) (width * GameView.screenRatioX * 0.1);
        this.height = (int) (height * GameView.screenRatioY * 0.1);
        this.bg = bg;


        this.bg = Bitmap.createScaledBitmap(this.bg, this.width, this.height, false);


    }

    public RectF getRect(){
        return new RectF(x, y, x + this.width, y + this.height);
    }
    public Bitmap getBitmap() {
        return bg;
    }
}
