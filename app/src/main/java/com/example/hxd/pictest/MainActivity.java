package com.example.hxd.pictest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    static {
        if(!OpenCVLoader.initDebug())
        {
            Log.d("opencv","初始化失败");
        }
    }

    public static Bitmap bitmap2;

    private ImageView ivTest;

    private Button btnGetPicFromCamera;
    private Button btnGetPicFromPhotoAlbum;
    private Button btnGetPermission;
    //private Button btn_max;
    private Button btn_gray;
    private Button btn_margin;
    private Button Btn_margin1;
    private Button btn_adj;
    private Button btn_roll;

    //private TextView edt_max;//显示最大面积

    private File cameraSavePath;//拍照照片路径
    private Uri uri;
    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetPicFromCamera = findViewById(R.id.btn_get_pic_from_camera);
        btnGetPicFromPhotoAlbum = findViewById(R.id.btn_get_pic_form_photo_album);
        btnGetPermission = findViewById(R.id.btn_get_Permission);
        //btn_max = findViewById(R.id.btn_max);
        btn_gray = findViewById(R.id.btn_gray);
        btn_margin = findViewById(R.id.btn_margin);
        Btn_margin1 = findViewById(R.id.btn_margin1);

        ivTest = findViewById(R.id.iv_test);
        //edt_max = findViewById(R.id.edt_max);
        btn_adj = findViewById(R.id.btn_adj);
        btn_roll = findViewById(R.id.btn_rolling_over);

        btnGetPicFromCamera.setOnClickListener(this);
        btnGetPicFromPhotoAlbum.setOnClickListener(this);
        btnGetPermission.setOnClickListener(this);
        //btn_max.setOnClickListener(this);
        btn_gray.setOnClickListener(this);
        btn_margin.setOnClickListener(this);
        Btn_margin1.setOnClickListener(this);

        btn_adj.setOnClickListener(this);
        btn_roll.setOnClickListener(this);

        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_get_pic_from_camera:
                goCamera();
                break;
            case R.id.btn_get_pic_form_photo_album:
                goPhotoAlbum();
                break;
            case R.id.btn_get_Permission:
                getPermission();
                break;
            /*case R.id.btn_max:
                Bitmap bitmap2 = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();
                Cut.doCut2(bitmap2);
                ivTest.setImageBitmap(Cut.doCut(bitmap2));
                //Maxarea();
                break;*/
            case R.id.btn_gray:
                gray();
                break;
            case R.id.btn_margin:
                Bitmap bitmap = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();
                ivTest.setImageBitmap(Cut.doCut(bitmap));
                break;
            case R.id.btn_margin1:
                Bitmap bitmap1 = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();
                ivTest.setImageBitmap(Cut.doCut2(bitmap1));
                break;
            case R.id.btn_adj:
                bitmap2 = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();
                Intent intent = new Intent(MainActivity.this,coloradjustActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_rolling_over:
                Bitmap bitmap3 = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();
                ivTest.setImageBitmap(convertBmp(bitmap3));
                break;
        }
    }

    //获取权限
    private void getPermission() {
        if (EasyPermissions.hasPermissions(this, permissions)) {
            //已经打开权限
            Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
        } else {
            //没有打开相关权限、申请权限
            EasyPermissions.requestPermissions(this, "需要获取您的相册、照相使用权限", 1, permissions);
        }

    }


    //激活相册操作
    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    //激活相机操作
    private void goCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(MainActivity.this, "com.example.hxd.pictest.fileprovider", cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        MainActivity.this.startActivityForResult(intent, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    //成功打开权限
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        Toast.makeText(this, "相关权限获取成功", Toast.LENGTH_SHORT).show();
    }
    //用户未同意权限
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "请同意相关权限，否则功能无法使用", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String photoPath;
        if (requestCode == 1 && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoPath = String.valueOf(cameraSavePath);
            } else {
                photoPath = uri.getEncodedPath();
            }
            Log.d("拍照返回图片路径:", photoPath);
            Glide.with(MainActivity.this).load(photoPath).into(ivTest);
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            photoPath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
            Glide.with(MainActivity.this).load(photoPath).into(ivTest);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    /*@TargetApi(Build.VERSION_CODES.N)
    private void Maxarea() {
        Bitmap bitmap = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();//获取image里的资源
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);//置灰
        Imgproc.Canny(grayMat, cannyEdges, 10, 100);//Canny边缘检测
        Mat element = getStructuringElement(MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(cannyEdges, cannyEdges, element);//膨胀
        Imgproc.erode(cannyEdges, cannyEdges, element);//腐蚀
        List<MatOfPoint> contours=new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyEdges,contours,hierarchy ,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));
        double Max = 0,contourarea;
        int j = 0;//最大轮廓的索引号
        for (int i=0;i<contours.size();i++) {
            contourarea = Imgproc.contourArea(contours.get(i));
            if(Max < contourarea)
            {
                Max = contourarea;
                j = i;
            }
        }
        Imgproc.drawContours(cannyEdges, contours, j, new Scalar(250), 50,8);//将最大面积轮廓加粗画出
        Bitmap processedImage = Bitmap.createBitmap(cannyEdges.cols(), cannyEdges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyEdges, processedImage);
        ivTest.setImageBitmap(processedImage);
        DecimalFormat df = new DecimalFormat("#.####");
        if (Max != 0) {
            edt_max.setText("最大面积：" + df.format(Max)+"cm");
        }
    }*/
    public Bitmap convertBmp(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1); // 镜像水平翻转
        Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

        return convertBmp;
    }


    @TargetApi(Build.VERSION_CODES.N)
    private void gray(){
        Bitmap bitmap = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();//获取image里的资源
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);//置灰
        Imgproc.Canny(grayMat, cannyEdges, 25, 100);//Canny边缘检测
        Mat element = getStructuringElement(MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(cannyEdges, cannyEdges, element);//膨胀
        Imgproc.erode(cannyEdges, cannyEdges, element);//腐蚀
        Bitmap processedImage = Bitmap.createBitmap(cannyEdges.cols(), cannyEdges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyEdges, processedImage);
        ivTest.setImageBitmap(processedImage);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void margin(){
        Bitmap bitmap = ((BitmapDrawable)ivTest.getDrawable()).getBitmap();//获取image里的资源
//        Mat grayMat = new Mat();
//        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        getContouresPic(bitmap);
//        Utils.bitmapToMat(bitmap, src);
//        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_RGB2GRAY);
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(20,20));
//        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_OPEN, kernel);
//        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_CLOSE, kernel);
//        Imgproc.threshold(grayMat, grayMat, 80, 255, Imgproc.THRESH_BINARY);
//        Mat edges = new Mat();
//        Imgproc.Canny(grayMat, edges, 20, 160);
//        Mat lines = new Mat();//检测到的直线集合
//        Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, 45, 20, 20);
//        Mat rot_mat = Imgproc.getRotationMatrix2D( center, angle, scale );
    }

    public void getContouresPic (Bitmap source) {
        Mat imageSobleOutThreshold = new Mat();

        Mat gray = new Mat();
        Utils.bitmapToMat(source, imageSobleOutThreshold);
        Imgproc.cvtColor(imageSobleOutThreshold, imageSobleOutThreshold, Imgproc.COLOR_BGR2GRAY);

        Imgproc.threshold(imageSobleOutThreshold, gray, 125, 225, Imgproc.THRESH_BINARY);//maxVal就是控制黑白反转的,0是黑

        //Utils.matToBitmap(gray, source);

        ArrayList<RotatedRect> rects = new  ArrayList<RotatedRect>();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);


        for(int i=0;i<contours.size();i++){
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if (rect.width > 500 && rect.width/rect.height == 1) {
                MatOfPoint2f mp2f = new MatOfPoint2f(contours.get(i).toArray());
                RotatedRect mr = Imgproc.minAreaRect(mp2f);

                double area = Math.abs(Imgproc.contourArea(mp2f));

                double angle = mr.angle+90;

                Mat ratationedImg = new Mat(gray.rows(), gray.cols(), CvType.CV_8UC3);
                ratationedImg.setTo(new Scalar(0, 0, 0));



                Point center = mr.center;//中心点
                Mat m2 = Imgproc.getRotationMatrix2D(center, angle, 1);
                Imgproc.warpAffine(imageSobleOutThreshold, ratationedImg, m2, imageSobleOutThreshold.size(), 1, 0, new Scalar(0) );//仿射变换

                Utils.matToBitmap(ratationedImg, source);


                File file = new File(Environment.getExternalStorageDirectory()+"/AiLingGong/", "ll"+System.currentTimeMillis()+".jpg");
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    source.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("----------------", "旋转角度是:"+angle+"------"+"-----"+mr);

            }

        }
    }

}
