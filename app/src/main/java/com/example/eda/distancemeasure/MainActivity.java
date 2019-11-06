package com.example.eda.distancemeasure;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("opencv_java3");
    }

    private Context mContext;
    private final static int REQUEST_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;
    private ImageView imageView;
    private Uri cameraUri;
    private String filePath;
    private  String Key1 = "Bitmap1";
    private  String Key2 = "Bitmap2";
    private  Bitmap color1_img;
    private  Bitmap color2_img;
    private  boolean stereoflag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);

        //stereoボタンを押すとステレオマッチング画面に切り替わります
        Button stereo_button = findViewById(R.id.stereo_button);
            stereo_button.setOnClickListener(mOnClickStereoButtonr);
        //calibrationボタンを押すとキャリブレーション画面に切り替わります
            Button caliba_button = findViewById(R.id.calibration_button);
            caliba_button.setOnClickListener(mClickCalibButton);

        //photoボタンを押すと撮影画面に切り替わります
        Button photo_button = findViewById(R.id.photo_button);
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    cameraIntent();
            }
        });
    }

    View.OnClickListener mOnClickStereoButtonr =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            stereoflag = false;
            StereoMatchingMode stereofragment = new StereoMatchingMode();
            Bundle args = new Bundle();
            args.putParcelable(Key1,color1_img);
            args.putParcelable(Key2,color2_img);
            stereofragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, stereofragment)
                    .commit();
        }
    };

    View.OnClickListener mClickCalibButton =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stereoflag = false;
            CalibrationMode calibrationfragment = new CalibrationMode();
            Bundle args = new Bundle();
            args.putParcelable(Key1,color1_img);
            calibrationfragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, calibrationfragment)
                    .commit();
        }
    };

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
                if (stereoflag ==false){
                    color1_img =  (Bitmap) intent.getExtras().get("data");
                    imageView.setImageBitmap(color1_img);
                    stereoflag = true;
                }else{
                    color2_img =  (Bitmap) intent.getExtras().get("data");
                    imageView.setImageBitmap(color2_img);
                    Toast.makeText(mContext,"StereoMatching",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
