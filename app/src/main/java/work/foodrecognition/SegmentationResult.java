package work.foodrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SegmentationResult extends AppCompatActivity {

    /*I wanted to segment the image first and then pass the segmented image to calculate the histogram.
     * but I was only able to produce a black and white segmented image, so I am not using this class.
     * I am figuring out a way of doing it.*/
    protected static final String TAG = null;
    static Mat result;
    static Bitmap bmp;
    ImageView imagesview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segmentation_result);

        /*Creating a bundle to retrieve the passed values through Intent.*/
        Bundle extras = getIntent().getExtras();
        String path = extras.getString("path");

        System.out.println("In SegmentationResult.onCreate with URL: "+path);

        /*Instantiating Image view to display the manipulated Image*/
        imagesview = (ImageView) findViewById(R.id.imageSView);
        String picturePath = path;
        System.out.println("Printing Image URI"+picturePath);
        /*Assigning the Image clicked to Bitmap object bit for further manipulations*/
        bmp= BitmapFactory.decodeFile(picturePath);
        Log.i(TAG, picturePath);
        /*Creating a Mat image object with the same image for manipulations*/
        Mat img= Imgcodecs.imread(picturePath);


        /*The final result is stored in result. Calling steptowatershed to get the output*/
        result = new Mat();
        result=steptowatershed(img);

        /*Assigning the result to bit anf displaying it to the user.*/
        Utils.matToBitmap(result, bmp, true);
        Log.i(TAG, "all okay");
        imagesview.setImageBitmap(bmp);

        /* Call ImageResults class with the output of the segmentation algorithm.*/
        /*Intent intent = new Intent(SegmentationResult.this,ImageResult.class);
        Bitmap image = imagesview.getDrawingCache();
        Bundle extrasMain = new Bundle();
        extras.putParcelable("imageBitmap",image);
        intent.putExtras(extrasMain);
        startActivity(intent);*/

        System.out.println("Returning from SegmentationResult.onCreate");
        //return bmp;
    }

    public Mat steptowatershed(Mat img)
    {
        System.out.println("In SegmentationResult.steptowatershed");
        Mat threeChannel = new Mat();

        /*In android an image is retrieved in BGR format. This needs to be converted into other colour formats to handle the manipulation.*/
        Imgproc.cvtColor(img, threeChannel, Imgproc.COLOR_BGR2GRAY);
        /*Creating a threshold of that image*/
        Imgproc.threshold(threeChannel, threeChannel, 100, 255, Imgproc.THRESH_BINARY);

        /*Creating foreground and background images and creating the threshold of that image*/
        Mat fg = new Mat(img.size(), CvType.CV_8U);
        Imgproc.erode(threeChannel,fg,new Mat());

        Mat bg = new Mat(img.size(),CvType.CV_8U);
        Imgproc.dilate(threeChannel,bg,new Mat());
        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);

        /*Creating markers image and assigning it to markers variable of WatershedSegmenter class*/
        Mat markers = new Mat(img.size(),CvType.CV_8U, new Scalar(0));
        Core.add(fg, bg, markers);
        Mat result1 = new Mat();
        WatershedSegmenter segmenter = new WatershedSegmenter();
        segmenter.setMarkers(markers);
        /* Calling process of WatershedSegmenter to process watershed algorithm.*/
        result1 = segmenter.process(img);
        System.out.println("Returning SegmentationResult.steptowatershed");
        return result1;
    }

    public class WatershedSegmenter
    {

        public Mat markers=new Mat();

        public void setMarkers(Mat markerImage)
        {
            System.out.println("In setMarkers CameraActivity");
            markerImage.convertTo(markers, CvType.CV_32SC1);
        }

        public Mat process(Mat image)
        {
            System.out.println("In process CameraActivity");
            /*Calling watershed function to manipulate the image.*/
            Imgproc.watershed(image,markers);
            markers.convertTo(markers,CvType.CV_8U);
            return markers;
        }
    }

}
