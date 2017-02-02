package work.foodrecognition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    protected static final String TAG = null;

    Intent cameraIntent;// = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    private TextView welcomeDisplay;
    private Button startButton;
    private Button quit;
    //private ;
    Uri fileUri;
    public static String finalURI;
    private static String file;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    /*This class instantiation is done for the uses of openCV*/
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /*This function is used to store the important variables of a paticular instance and restore them*/
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("fileUri", file);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }*/

        /*Loading openCV liberaries*/
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback))
        {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }

        /*Instantiating the text display on the landing page*/
        welcomeDisplay = (TextView)findViewById(R.id.title);
        welcomeDisplay.setTextSize(50);


        /*Instantiating cameraIntent to capture the image using the camera*/
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(savedInstanceState == null){
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        }

        /*Loading the intent with URI so the image can be stored at the path*/
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        /*Instantiating the start button. This calls the camera to take the picture and later calls startActivityForResult
        * to decide what needs to be done.*/
        startButton = (Button)findViewById(R.id.btnStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                //fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                System.out.println("Print fileUri "+fileUri);

                startActivityForResult(cameraIntent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

                /*Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);*/
            }
        });

        /*Instantiating Exit button. Exists the application when clicked.*/
        quit = (Button) findViewById(R.id.btnQuit);
        quit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
                System.exit(0);return;
            }
        });

    }

    /*Called from onclick activity of the start button. Based on the result code it decides what needs to be done*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE){
            if(resultCode == RESULT_OK){

                /*I wanted to segment the image first and then pass the segmented image to calculate the histogram.
                * but I was only able to produce a black and white segmented image, so I am not using this class.
                * I am figuring out a way of doing it.*/

                /*SegmentationCV seg = new SegmentationCV();
                seg.generateMat(file);

                /*resImage = (ImageView) findViewById(R.id.resultImage);
                resImage.buildDrawingCache();
                Bitmap image = resImage.getDrawingCache();*/

                /*Creating a Bundle to put the file pathe of the stored image and pass it to another activity using intent*/
                Bundle extras = new Bundle();
                extras.putString("path",file);

                /*Creating an Intent and calling ImageResults class for more calculations*/
                Intent intent = new Intent(MainActivity.this, ImageResult.class);
                /*Intent intent1 = seg.generateMat(file);*/

                intent.putExtras(extras);
                startActivity(intent);
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Image Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static Uri getOutputMediaFileUri(int type){
        System.out.println("Entered Function getOutputMediaFileUri");
        return Uri.fromFile(getOutputMediaFile(type));
    }


    /*This function is basically written to derive the path to save the image when clicked.*/
    private static File getOutputMediaFile(int type){

        System.out.println("Entered Function getOutputMediaFile");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "FoodRecognition");
        if (! mediaStorageDir.exists()){
            /*Creating a directory with name FoodRecognition if it doesn't exists.*/
            if (! mediaStorageDir.mkdirs()){
                Log.d("FoodRecognition", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;

        /*Creating a file path to where the image needs to be saved.*/
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        finalURI = mediaStorageDir.getPath() + File.separator + "HIST_IMG_"+ timeStamp + ".jpg";

        System.out.println("Returning from file URL with: "+mediaFile);
        file = mediaFile.toString();
        return mediaFile;
    }

}
