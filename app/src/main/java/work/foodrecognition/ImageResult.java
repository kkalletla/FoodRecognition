package work.foodrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class ImageResult extends AppCompatActivity implements TextToSpeech.OnInitListener{

    /*This class is mainly used to create the histogram and derive the RGB values of the food image.
    * Then this value is compared with the predefined values of various food items to determine the food in the container.
    * Once the contents are identified the image is displayed to the user along with the contents and also an audio of the
    * contents are so that visually impaired people can know the contents.*/
    ImageView resMainImage;
    protected static final String TAG = null;
    float offset = 1;
    TextView resText;
    int offsetValue = 2000;
    TextToSpeech text2speech;
    String filename;
    Mat image;
    String value = "";
    Button back;
    Button exit;
    Button histo;


    HashMap<String,Float> foodList = new HashMap<String,Float>();

    /*This function is on the interface TextToSpeech.OnInitListener.
    This function is generally used to initialize TextToSpeech instance.*/
    @Override
    public void onInit(int status) {
        System.out.println("Init being called: "+status);
        if (status != TextToSpeech.ERROR) {
            text2speech.setLanguage(Locale.US);
            speech();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);

        text2speech = new TextToSpeech(this,this);

        System.out.println("In ImageResult.onCreate");

        /*Creating a bundle to retrieve the passed values through Intent.*/
        Bundle extras = getIntent().getExtras();
        String path = extras.getString("path");

        /*Initializing ImageView with the image and display it to the user.*/
        resMainImage = (ImageView) findViewById(R.id.imageSView);
        String picturePath = path;
        System.out.println("Printing Image URI"+picturePath);

        System.out.println("Initializing Hash Map");

        /*Initializing the HashMap with the default food RGB values*/
        foodList.put("Tomato",57599f);
        foodList.put("Banana",67802f);
        foodList.put("Chili",60593f);
        //foodList.put("Bread",61266.5f);
        foodList.put("Pulses",65801f);


        /*Reading the image into image Mat object.*/
        image = Imgcodecs.imread(picturePath);


        /*Declaring and initializing required parameters for calculating histogram*/
        int mHistSizeNum = 256;
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
        Mat hist = new Mat();
        float []mBuff = new float[mHistSizeNum];
        MatOfFloat histogramRanges = new MatOfFloat(0f, 256f);
        Scalar mColorsRGB[] = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        int thickness = (int) (image.width() / (mHistSizeNum+10)/3);
        if(thickness> 3) thickness = 3;
        MatOfInt mChannels[] = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        Size sizeRgba = image.size();
        int offset = (int) ((sizeRgba.width - (3*mHistSizeNum+30)*thickness));
        org.opencv.core.Point mP1 = new org.opencv.core.Point();
        org.opencv.core.Point mP2 = new org.opencv.core.Point();

        int red = 0;
        int green = 0;
        int blue = 0;
        // RGB
        System.out.println("After initializing offset"+ Runtime.getRuntime().maxMemory());
        for(int c=0; c<3; c++) {
            /*Calculating histogram by calling calcHist function*/
            Imgproc.calcHist(Arrays.asList(image), mChannels[c], new Mat(), hist, mHistSize, histogramRanges);
            /*Calculating histogram normalization by calling normalize function*/
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);

            /*Calculating the total value of RGB to determine the contents.*/
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - (int)mBuff[h];
                System.out.print("\t"+mP1.x+"\t"+mP1.y+"\t"+mP2.y+"\n");
                Imgproc.line(image, mP1, mP2, mColorsRGB[c], thickness);
                //System.out.println("\t"+mP1.x+"\t"+mP1.y+"\t"+mP2.y);
                if(c==0)
                    red = red + (int)mBuff[h];
                else if (c==1)
                    green = green + (int)mBuff[h];
                else if(c==2)
                    blue = blue + (int)mBuff[h];
            }
            System.out.println("NEWLINE");
        }

        System.out.println("Red:   "+red);
        System.out.println("Green: "+green);
        System.out.println("Blue:  "+blue);
        System.out.println("Total: "+(red+green+blue));
        System.out.println("After initializing FOR"+ Runtime.getRuntime().maxMemory());

        /*Bitmap bit = BitmapFactory.decodeFile(picturePath);

        System.out.println("After initializing BIT"+ Runtime.getRuntime().maxMemory());

        Utils.matToBitmap(image,bit);*/

        /*Instantiating ImageView and assigning it with the clicked image to display it to the user.*/

        File file = new File(MainActivity.finalURI);
        filename = file.toString();

        Imgcodecs.imwrite(filename,image);

        resMainImage = (ImageView) findViewById(R.id.resultImage);
        Bitmap bitMain = BitmapFactory.decodeFile(picturePath);
        resMainImage.setImageBitmap(bitMain);


        float previous = 0;
        float current = 35000f;
        float totalValue = red + green + blue;
        int counter =0;


        /*Iterating through the HashMap of food items.*/
        for (String key : foodList.keySet()){
            previous = current;
            current = foodList.get(key);


            /*Comparing the derived RGB values with the stored ones to get the contents.
            * If it matches with any of the contents*/
            if((current-offsetValue <= totalValue) && (totalValue <= current+offsetValue)){
                System.out.println("The Key is : " + key);
                value = "The contents are: "+ key;
                break;
            }

        }

        /*If any content is not detected */
        if(value == null || value ==""){
            value = "Contents can not be detected.";
            System.out.println("The Key is : " + value);
        }


        /*TextView used to display the contents to the user.*/
        resText  = (TextView) findViewById(R.id.resultText);
        resText.setText(value);

        System.out.println("Returning ImageResult.onCreate");

        /*Instantiating Back button. goes to previous page when clicked.*/
        back = (Button) findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                value ="";
                onDestroy();
            }
        });

        histo = (Button) findViewById(R.id.btnHist);
        histo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Bundle extras = new Bundle();
                extras.putString("filename",filename);

                System.out.println("In exit.............."+filename);
                /*Creating an Intent and calling ImageResults class for more calculations*/
                Intent intent = new Intent(ImageResult.this, HistogramView.class);
                /*Intent intent1 = seg.generateMat(file);*/

                intent.putExtras(extras);
                startActivity(intent);

            }
        });

        /*Instantiating Exit button. Exists the application when clicked.*/
        exit = (Button) findViewById(R.id.btnExit);
        exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
                System.exit(0);

            }
        });
    }

    /*This function is used to destroy the instance of the TextToSpeech after completion.*/
    @Override
    protected void onDestroy() {
            if(text2speech != null) {

            text2speech.stop();
            text2speech.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
        exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
                System.exit(0);
            }
        });

        histo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Bundle extras = new Bundle();
                extras.putString("filename",filename);

                System.out.println("In exit.............."+filename);
                /*Creating an Intent and calling ImageResults class for more calculations*/
                Intent intent = new Intent(ImageResult.this, HistogramView.class);
                /*Intent intent1 = seg.generateMat(file);*/

                intent.putExtras(extras);
                startActivity(intent);

            }
        });

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                value ="";
                onDestroy();
            }
        });
    }

    /*This function is used to convert the text to speech and recite it to the user.*/
    public void speech(){
        text2speech.speak(value,TextToSpeech.QUEUE_FLUSH,null);
    }



}
