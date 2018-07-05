package com.example.eda.distancemeasure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private ImageView imageView;
    private String filePath;
    private Bitmap bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        ImageView imageview = (ImageView)view.findViewById(R.id.stereoView);
        imageview.setImageBitmap(onImage(bitmap));
    }

    public Bitmap onImage(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        Mat gray = Mat.zeros(mat.width(),mat.height(),CV_8U);

        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Core.normalize(gray, gray, 0, 255, Core.NORM_MINMAX);

        Mat outputFrame = onStereo(gray,gray);

        Bitmap mbitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(outputFrame,mbitmap);
        return mbitmap;
    }

    public Mat ImageLoad(String name) {
        Bitmap map = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier( name, "drawable", getPackageName()));
        Mat mat = new Mat();
        Utils.bitmapToMat(map, mat);
        return mat;
    }

    public Mat onStereo(Mat right,Mat left) {

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
        Imgproc.resize(undistortright, undistortright,sz,0.4,0.4,INTER_AREA);
        Imgproc.resize(undistortleft, undistortleft, undistortright.size());
        Photo.fastNlMeansDenoising(undistortright,undistortright);
        Photo.fastNlMeansDenoising(undistortleft,undistortleft);
        Imgproc.resize(mdisparity, mdisparity, undistortright.size());
        stereo.compute(undistortleft, undistortright, mdisparity);

        Core.normalize(mdisparity,undistortright,0,255, Core.NORM_MINMAX,CV_8UC1);
    // display the result
        Imgproc.cvtColor(undistortright, undistortleft, Imgproc.COLOR_GRAY2BGRA, 4);
        Imgproc.resize(undistortleft, undistortright, undistortright.size());
        return undistortright;
}

}
