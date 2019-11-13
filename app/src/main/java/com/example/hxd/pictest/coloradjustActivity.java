package com.example.hxd.pictest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;

public class coloradjustActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private ImageView img;
    private float hue,saturation,lum;
    private Bitmap bm;
    private final float  MID_VALIE =  50;//猜测:代表进度为50的时候是原始状态
    private SeekBar seekBarHue, seekBarSaturation, seekBarLum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coloradjust);

        img = (ImageView) findViewById(R.id.mImageView);
        hue = 0;
        saturation = 0;
        lum = 0;
        bm = MainActivity.bitmap2;
        //bm =  BitmapFactory.decodeResource(((Activity)this).getResources(),R.drawable.letme);

        seekBarHue = (SeekBar) findViewById(R.id.sb_Hue);
        seekBarHue.setOnSeekBarChangeListener(this);
        seekBarSaturation = (SeekBar) findViewById(R.id.sb_Lum);
        seekBarSaturation.setOnSeekBarChangeListener(this);
        seekBarLum = (SeekBar) findViewById(R.id.sb_Sat);
        seekBarLum.setOnSeekBarChangeListener(this);
        img.setImageBitmap(bm);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_Hue:
                hue = (progress - MID_VALIE) * 1.0F / MID_VALIE * 180;
                break;
            case R.id.sb_Lum:
                saturation = progress * 1.0F / MID_VALIE;
                break;
            case R.id.sb_Sat:
                lum = progress * 1.0F / MID_VALIE;
                break;
        }

        img.setImageBitmap(handleImageEffect(bm, hue, saturation, lum));
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    private Bitmap handleImageEffect(Bitmap bm, float hue, float saturation, float lum) {
        /**
         * Android系统不允许直接修改原图,
         * 必须通过原图创建一个同样大小的bitmap，
         * 并将原图绘制到该Bitmap中，
         * 以一个副本的形式来修改图像
         */
        Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();

        //色调矩阵
        ColorMatrix hueMatrix = new ColorMatrix();
        hueMatrix.setRotate(0, hue);//红
        hueMatrix.setRotate(1, hue);//绿
        hueMatrix.setRotate(2, hue);//蓝

        //饱和度矩阵
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);

        //亮度矩阵
        ColorMatrix lumMatrix = new ColorMatrix();
        lumMatrix.setScale(lum, lum, lum, 1);


        //图片矩阵
        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(hueMatrix);
        imageMatrix.postConcat(saturationMatrix);
        imageMatrix.postConcat(lumMatrix);

        p.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        c.drawBitmap(bm, 0, 0,p);

        return bmp;

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode) {
        case 1:
            if (data != null) {
                //取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意
                Uri mImageCaptureUri = data.getData();
                //返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取
                if (mImageCaptureUri != null) {
                    Bitmap image;
                    try {
                        //这个方法是根据Uri获取Bitmap图片的静态方法
                        image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
                        if (image != null) {
                            img.setImageBitmap(image);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                        Bitmap image = extras.getParcelable("data");
                        if (image != null) {
                            img.setImageBitmap(image);
                        }
                    }
                }

            }
            break;
        default:
            break;

        }
    }
*/
}
