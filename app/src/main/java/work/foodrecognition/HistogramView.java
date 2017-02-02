package work.foodrecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class HistogramView extends AppCompatActivity {

    ImageView histImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram_view);

        Bundle extras = getIntent().getExtras();
        String filename = extras.getString("filename");

        Mat image = Imgcodecs.imread(filename);

        Bitmap bit =  BitmapFactory.decodeFile(filename);

        Utils.matToBitmap(image,bit);

        histImage = (ImageView) findViewById(R.id.histView);
        histImage.setImageBitmap(bit);
    }

}
