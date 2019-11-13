package com.example.hxd.pictest;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;


public class ImgPretreatment {
    public static Bitmap img;
    public static int imgWidth;
    public static int imgHeight;
    public static int[] imgPixels;

    /**
     * 图片预处理
     * @param bitmap
     * @return
     */
    public static void doPretreatment(Bitmap bitmap){
        setImgInfo(bitmap);
        //打印图像的尺寸
        System.out.println("图片的宽"+imgWidth);
        System.out.println("图片的高"+imgHeight);
        //灰度化
        convertGreyImg();
        Binary(getOtsuHresholdValue());
        medianImage();
        Util.returnPic(2);
    }

    /**
     * 获得图片基本信息
     * @param image
     */
    private static void setImgInfo(Bitmap image) {
        img = image;
        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
        imgPixels = new int[imgWidth * imgHeight];
        img.getPixels(imgPixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
    }

    /**
     * 图片灰度化
     * @return
     */
    public static void convertGreyImg() {
        int alpha = 0xFF << 24;
        for(int i = 0; i < imgHeight; i++)  {
            for(int j = 0; j < imgWidth; j++) {
                int grey = imgPixels[imgWidth * i + j];


                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                imgPixels[imgWidth * i + j] = grey;
            }
        }

    }


    /**
     * 图片二值化
     */
    public static void Binary(int threshold){
        int white = 0;
        int black = 0;
        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int grey = imgPixels[imgWidth * i + j];
                if(Color.blue(grey)<=threshold)
                    black++;
                else
                    white++;
            }
        }
        //System.out.println("白色的："+white);
        //System.out.println("黑色的："+black);
        if(black<=white){
            for(int i = 0; i < imgHeight; i++) {
                for (int j = 0; j < imgWidth; j++) {
                    int grey = imgPixels[imgWidth * i + j];
                    if (Color.blue(grey) <= threshold)
                        imgPixels[imgWidth * i + j] = Color.rgb(0,0,0);
                    else
                        imgPixels[imgWidth * i + j] = Color.rgb(255,255,255);
                }
            }
        }else{
            for(int i = 0; i < imgHeight; i++) {
                for (int j = 0; j < imgWidth; j++) {
                    int grey = imgPixels[imgWidth * i + j];
                    if (Color.blue(grey) <= threshold)
                        imgPixels[imgWidth * i + j] = Color.rgb(255,255,255);
                    else
                        imgPixels[imgWidth * i + j] = Color.rgb(0,0,0);
                }
            }
        }

        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int grey = imgPixels[imgWidth * i + j];
            }
        }
    }
    /**
     * 直方图谷点门限法
     */
    public static int histogramThreshold(){
        int threshold = 0;
        //灰度均值
        int average = 0;
        //灰度均方差
        int standardDeviation = 0;
        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                average += Color.blue(imgPixels[imgWidth * i + j]);
            }
        }
        average /= (imgWidth*imgHeight);

        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                standardDeviation += Math.pow(average-Color.blue(imgPixels[imgWidth * i + j]),2);
            }
        }
        standardDeviation /= (imgWidth*imgHeight);
        standardDeviation = (int)Math.sqrt(standardDeviation);
        Log.d("阈值是：",average-standardDeviation+"");
        return average-standardDeviation;
    }

    /**
     * 迭代法求阈值
     * @return
     */
    public static int getIterationHresholdValue(){
        int []MinMax = new int[2];
        double [] getHisto = getMinMaxGrayValue(MinMax);
        int T1 =(MinMax[0]+MinMax[1])/2;
        int T2;
        double G1=0,G2=0;
        double m1=0,m2=0;
        double c1=0,c2=0;
        do {
            T2=T1;
            for(int i =0;i<=T2;i++){
                G2 += i*getHisto[i];
                c2 += getHisto[i];
            }
            m2 = G2/c2;

            for(int i =T2+1;i<getHisto.length;i++){
                G1 += i*getHisto[i];
                c1 += getHisto[i];
            }
            m1 = G1/c1;
            T1 = (int)((m1+m2)/2);
        }while(T2 != T1);
        return T2;
    }

    /**
     * 灰度直方图 version1
     * @param srcImg
     * @return
     */
    public static double[] getHisto(int []srcImg){
        double []getHisto = new double[256];
        for(int i=0;i<srcImg.length;i++){
            getHisto[Color.blue(srcImg[i])]++;
        }
        //求概率
        for(int i=0;i<getHisto.length;i++){
        }
        return getHisto;
    }

    /**
     * 灰度直方图 version2
     * @param srcImg
     * @return
     */
    public static double[] getHistoByChance(int []srcImg){
        double []getHisto = new double[256];
        for(int i=0;i<srcImg.length;i++){
            getHisto[Color.blue(srcImg[i])]++;
        }
        //求概率
        for(int i=0;i<getHisto.length;i++){
            getHisto[i] /= srcImg.length;
        }
        return getHisto;
    }
    /**
     * 获得该图像中最小最大灰度
     */
    public static double[] getMinMaxGrayValue(int []MinMax){
        double []getHisto = getHisto(imgPixels);
        int minGrayValue = 255;
        int maxGrayValue = 0;
        for(int i=0;i<getHisto.length;i++){
            if(getHisto[i]>0&&i<minGrayValue)
                minGrayValue = i;
            if(getHisto[i]>0&&i>maxGrayValue)
                maxGrayValue = i;
        }
        MinMax[0] = minGrayValue;
        MinMax[1] = maxGrayValue;
        return getHisto;
    }



    /**
     * OTSU
     */
    private static int getOtsuHresholdValue() {

        double [] getHisto = getHistoByChance(imgPixels);
        int T = 0;
        //统计T的个数
        double betweenClassVariance = 0;
        for(int i=0;i<getHisto.length;i++){
            double p1 = 0;
            double mk = 0;
            //mg 全局平均灰度
            double mg = 0;
            //类间方差
            for(int k=0;k<=i;k++) {
                p1 += getHisto[k];
                mk += k*getHisto[k];
            }
            for(int j=0;j<getHisto.length;j++) {
                mg += j*getHisto[j];
            }
            if(Math.pow(mg*p1-mk,2)/(p1*(1-p1))>betweenClassVariance ){
                betweenClassVariance = Math.pow(mg*p1-mk,2)/(p1*(1-p1));
                T = i;
            }
        }
        return T;
    }

    public static void medianImage(){
        int []temp = new int[9];
        //边界点无法检测
        for(int y=0; y<imgHeight; y++) {
            for(int x=0; x<imgWidth; x++) {
                if(x!=0 && x!=imgWidth-1 && y!=0 && y!=imgHeight-1) {
                    temp[0] = imgPixels[x-1+(y-1)*imgWidth];
                    temp[1] = imgPixels[x+(y-1)*imgWidth];
                    temp[2] = imgPixels[x+1+(y-1)*imgWidth];
                    temp[3] = imgPixels[x-1+(y)*imgWidth];
                    temp[4] = imgPixels[x+(y)*imgWidth];
                    temp[5] = imgPixels[x+1+(y)*imgWidth];
                    temp[6] = imgPixels[x-1+(y+1)*imgWidth];
                    temp[7] = imgPixels[x+(y+1)*imgWidth];
                    temp[8] = imgPixels[x+1+(y+1)*imgWidth];
                    Arrays.sort(temp);
                    imgPixels[x+(y)*imgWidth] = temp[4];
                }
            }
        }
    }
//    public static void test (){
//        Log.d("total imgPixels:",imgWidth*imgHeight+"");
//    }
}
