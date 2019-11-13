package com.example.hxd.pictest;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Util {
    public static Bitmap returnPic(int i){
        Bitmap result1 = Bitmap.createBitmap(ImgPretreatment.imgWidth, ImgPretreatment.imgHeight, Bitmap.Config.RGB_565);
        result1.setPixels(ImgPretreatment.imgPixels, 0, ImgPretreatment.imgWidth, 0, 0,ImgPretreatment.imgWidth, ImgPretreatment.imgHeight);
        File appDir = new File(Environment.getExternalStorageDirectory(),"charsCut");
        if(!appDir.exists()){//若文件夹不存在，则创建目录
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis()+"_"+i+".jpg";
        File file=new File(appDir,fileName);
        try{
            if(file.exists()){//防止图像重名，保存最新图像
                file.delete();
            }
            file.createNewFile();
        }catch(IOException e){
            e.printStackTrace();
        }
        try{
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            result1.compress(Bitmap.CompressFormat.JPEG,100,bos);
            bos.flush();
            bos.close();
//            Uri uri  = Uri.fromFile(file);

        }catch(IOException e){
            e.printStackTrace();
        }

//        try{
//            MediaStore.Images.Media.insertImage(context.getContent)
//        }
        return result1;
    }
}
