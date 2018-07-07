package com.example.eda.distancemeasure;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
    }

    private final static int REQUEST_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;
    private ImageView imageView;
    private Uri cameraUri;
    private String filePath;
    private  String Key1 = "Bitmap1";
    private  String Ket2 = "Bitmap2";
    private  Bitmap color_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);

        //stereoボタンを押すとステレオマッチング画面に切り替わります
        Button stereo_button = findViewById(R.id.stereo_button);
            stereo_button.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {
                                                     StereoMatchingMode stereofragment = new StereoMatchingMode();
                                                     /*
                                                     Intent intent = new Intent(this,StereoMatchingMode.);
                                                     Bundle bundle = new Bundle();
                                                     bundle.putParcelable(Key1, color_img);
                                                     intent.putExtras(bundle);
                                                     startActivity(intent);
                                                     */
                                                     Bundle args = new Bundle();
                                                     args.putParcelable(Key1,color_img);
                                                     stereofragment.setArguments(args);
                                                     getSupportFragmentManager().beginTransaction()
                                                             .replace(R.id.container, stereofragment)
                                                             .commit();
                                                 }
            });

        //calibrationボタンを押すとキャリブレーション画面に切り替わります
            Button caliba_button = findViewById(R.id.calibration_button);
            caliba_button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    CalibrationMode calibrationfragment = new CalibrationMode();
                                                    Bundle args = new Bundle();
                                                    args.putParcelable(Key1,color_img);
                                                    calibrationfragment.setArguments(args);
                                                    getSupportFragmentManager().beginTransaction()
                                                            .replace(R.id.container, calibrationfragment)
                                                            .commit();
                                                }
            });

        //photoボタンを押すと撮影画面に切り替わります
        Button photo_button = findViewById(R.id.photo_button);
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    cameraIntent();
            }
        });
    }

    private void cameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CAMERA) {
            if(intent.getExtras() != null){
                color_img =  (Bitmap) intent.getExtras().get("data");
                imageView.setImageBitmap(color_img);
            }
        }
    }

}
