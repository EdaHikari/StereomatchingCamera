package com.example.eda.distancemeasure;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.INTER_AREA;

public class StereoMatchingMode extends Fragment {

    static {
        System.loadLibrary("opencv_java3");
    }

    private  String Key1 = "Bitmap1";
    private  String Key2 = "Bitmap2";
    private  Bitmap color1_img;
    private  Bitmap color2_img;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            color1_img = getArguments().getParcelable(Key1);
            color2_img = getArguments().getParcelable(Key2);
            if (color1_img != null && color2_img != null) System.out.println("color1_img != null && color2_img != null");
            else System.out.println("color1_img == null || color2_img == null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stereo_matching_mode, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        ImageView imageview = (ImageView)view.findViewById(R.id.stereoView);
        Button stereo_button = (Button)view.findViewById(R.id.return_button);
        stereo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        imageview.setImageBitmap(onImage(color1_img,color2_img));
    }

    public Bitmap onImage(Bitmap bmp1,Bitmap bmp2) {
        Mat mat1 = new Mat();
        Mat mat2 = new Mat();
        Utils.bitmapToMat(bmp1, mat1);
        Utils.bitmapToMat(bmp2, mat2);
        Mat gray1 = Mat.zeros(mat1.width(),mat1.height(),CV_8U);
        Mat gray2 = Mat.zeros(mat2.width(),mat2.height(),CV_8U);

        Imgproc.cvtColor(mat1, gray1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mat2, gray2, Imgproc.COLOR_BGR2GRAY);
        Core.normalize(gray1, gray1, 0, 255, Core.NORM_MINMAX);
        Core.normalize(gray2, gray2, 0, 255, Core.NORM_MINMAX);

        Mat outputFrame = onStereo(gray1,gray2);

        Bitmap mbitmap = Bitmap.createBitmap(outputFrame.width(), outputFrame.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(outputFrame,mbitmap);
        return mbitmap;
    }



    public Mat onStereo(Mat left,Mat right) {

    Mat mdisparity = Mat.zeros(right.width(),right.height(),CV_8U);

    Mat undistortright = new Mat();
    Mat undistortleft = new Mat();
    undistortright = right;
    undistortleft = left;

    StereoSGBM stereo = StereoSGBM.create(

            0,
            96,
            3,
            480,
            240,
            1,
            0,
            0,
            1,
            1,
            StereoSGBM.MODE_SGBM
    );

    Size sz = new Size();
        //Imgproc.resize(undistortright, undistortright,sz,0.4,0.6,INTER_AREA);
        Imgproc.resize(undistortleft, undistortleft, undistortright.size());
        //Photo.fastNlMeansDenoising(undistortright,undistortright);
        //Photo.fastNlMeansDenoising(undistortleft,undistortleft);
        Imgproc.resize(mdisparity, mdisparity, undistortright.size());
        stereo.compute(undistortleft, undistortright, mdisparity);

        Core.normalize(mdisparity,undistortright,0,255, Core.NORM_MINMAX,CV_8UC1);
        //Imgproc.cvtColor(undistortright, undistortleft, Imgproc.COLOR_GRAY2BGRA, 4);
        //Imgproc.resize(undistortleft, undistortright, undistortright.size());
        //System.out.println("OpenCV Mat data:\n" + undistortleft.dump());
      return undistortright;
    }
}
