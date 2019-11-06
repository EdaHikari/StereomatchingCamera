package com.example.eda.distancemeasure;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.calib3d.Calib3d.RANSAC;
import static org.opencv.calib3d.Calib3d.calibrateCamera;
import static org.opencv.calib3d.Calib3d.drawChessboardCorners;
import static org.opencv.calib3d.Calib3d.findEssentialMat;
import static org.opencv.calib3d.Calib3d.getOptimalNewCameraMatrix;
import static org.opencv.calib3d.Calib3d.recoverPose;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.features2d.Features2d.drawMatches;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class CalibrationMode extends Fragment {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
    public native String stringFromJNI(int width, int height, byte[] yuv);

    private  String Key1 = "Bitmap1";
    private  String Ket2 = "Bitmap2";
    private  Bitmap color_img;

    private DataRecode dataMethod;
    MatOfPoint2f corners;
    Mat distcofes;
    Mat cameraMatrix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            color_img = getArguments().getParcelable(Key1);
            dataMethod = new DataRecode(getActivity(),"imagePoints");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calibration_mode, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        ImageView imageview = (ImageView)view.findViewById(R.id.calibrateView);
        imageview.setImageBitmap(onImage(color_img));

        Button returnButton = (Button)view.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getActivity(), MainActivity.class);
                //startActivity(intent);
                dataMethod.loadData("mat");
            }
        });

        Button yesButton = (Button)view.findViewById(R.id.yes_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataMethod.saveData("mat",corners);
                //dataMethod.loadData("mat");
                System.out.println("Imgcodecs.imwrite success!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        });
    }


    public Bitmap onImage(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        Mat gray = Mat.zeros(mat.width(),mat.height(),CV_8U);

        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Core.normalize(gray, gray, 0, 255, Core.NORM_MINMAX);

        Mat outputFrame = onCalibrate(gray);

        Bitmap mbitmap = Bitmap.createBitmap(outputFrame.width(), outputFrame.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(outputFrame,mbitmap);
        return mbitmap;
    }

    public Mat onCalibrate(Mat inputFrame) {

        int horizonalCrossCount = 7;
        int verticalCrossCount = 10;

        List<Mat> image_point = new ArrayList<Mat>();
        MatOfPoint3f objects = new MatOfPoint3f();
        List<Mat> objcts_point = new ArrayList<Mat>();
        for (int j = 0; j < horizonalCrossCount * verticalCrossCount; j++) {
            objects.push_back(new MatOfPoint3f(new Point3(j / horizonalCrossCount, j % verticalCrossCount, 0.0f)));
        }

        corners = new MatOfPoint2f();
        TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS, 30, 0.1);
        Size sizea = new Size(horizonalCrossCount, verticalCrossCount);
        Size sizeb = new Size(11, 11);
        Size sizec = new Size(-1, -1);
        distcofes = Mat.zeros(4, 1, CV_32FC1);
        cameraMatrix = Mat.zeros(3, 3, CV_32FC1);
        List<Mat> t = new ArrayList<>();
        List<Mat> r = new ArrayList<>();

        boolean found = Calib3d.findChessboardCorners(inputFrame, sizea, corners,
                    Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);

        if (!found) return inputFrame;
        else {
            Imgproc.cornerSubPix(inputFrame, corners, sizeb, sizec, criteria);
            image_point.add(corners);
            objcts_point.add(objects);
            drawChessboardCorners(inputFrame, sizea, corners, found);
            System.out.println("success");

            float[][] dataA = {{320, 0, 160}, {0, 320, 120}, {0, 0, 1}};
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    cameraMatrix.put(i, j, dataA[i][j]);
                }
            }
            calibrateCamera(objcts_point, image_point, inputFrame.size(), cameraMatrix, distcofes, r, t);
            Mat undisort = new Mat();
            //Mat roi = getOptimalNewCameraMatrix(cameraMatrix, distcofes, inputFrame.size(), 1);
            Imgproc.undistort(inputFrame, undisort, cameraMatrix, distcofes, cameraMatrix);

            return undisort;
        }
    }

}
