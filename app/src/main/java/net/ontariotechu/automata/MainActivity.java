package net.ontariotechu.automata;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    int speed = 0, sense = 0, breed = 0, size = 0;
    Bitmap breedPart, speedPart, sensePart;
    Bitmap critter;
    private int width, height;
    ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preview = findViewById(R.id.preview);

        //increase speed
        findViewById(R.id.speedInc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the species is at max size, do not change any stats
                if(size < 9){
                    speed++;
                    size++;
                }

                TextView speedPts = (TextView) findViewById(R.id.speedPts);
                speedPts.setText(speed + "");
                drawPreview();
            }
        });
        //decrease speed
        findViewById(R.id.speedDec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(speed > 0){
                    speed--;
                    size--;
                }

                TextView speedPts = (TextView) findViewById(R.id.speedPts);
                speedPts.setText(speed + "");
                drawPreview();
            }
        });
        //increase sense
        findViewById(R.id.senseInc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(size < 9){
                    sense++;
                    size++;
                }

                TextView speedPts = (TextView) findViewById(R.id.sensePts);
                speedPts.setText(sense + "");
                drawPreview();
            }
        });
        //decrease sense
        findViewById(R.id.senseDec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sense > 0){
                    sense--;
                    size--;
                }

                TextView speedPts = (TextView) findViewById(R.id.sensePts);
                speedPts.setText(sense + "");
                drawPreview();
            }
        });
        //increase breed
        findViewById(R.id.breedInc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(size < 9){
                    breed++;
                    size++;
                    drawPreview();
                }

                TextView speedPts = (TextView) findViewById(R.id.breedPts);
                speedPts.setText(breed + "");
            }
        });
        //decrease breed
        findViewById(R.id.breedDec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(breed > 0){
                    breed--;
                    size--;
                    drawPreview();
                }

                TextView speedPts = (TextView) findViewById(R.id.breedPts);
                speedPts.setText(breed + "");
            }
        });

        findViewById(R.id.submitBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(size == 0){
                    return;
                }
                Intent startIntent = new Intent(getApplicationContext(), GameActivity.class);
                startIntent.putExtra("speed", speed);
                startIntent.putExtra("sense", sense);
                startIntent.putExtra("breed", breed);
                startIntent.putExtra("size", size);
                startActivity(startIntent);
            }
        });
    }

    private void drawPreview(){
        speedPart = BitmapFactory.decodeResource(getResources(), R.drawable.speed);
        sensePart = BitmapFactory.decodeResource(getResources(), R.drawable.sense);
        breedPart = BitmapFactory.decodeResource(getResources(), R.drawable.breed);

        critter = createPreview();

        width = critter.getWidth();
        System.out.println(width);
        height = critter.getHeight();
        System.out.println(height);

        width = (int) (width  * 0.33f );
        height = (int) (height * 0.33f);
        System.out.println(width);
        System.out.println(height);

        critter = Bitmap.createScaledBitmap(critter, width, height, false);

        preview.setImageBitmap(critter);

    }

    private Bitmap createPreview(){
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

    //updates position on canvas to draw odd size critter
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


}
