package com.example.hxd.pictest;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Date;



public class HoughTransform {

    public static Bitmap img;
    public static int imgWidth;
    public static int imgHeight;
    public static int[] imgPixels;
    //降采样处理之后的结果（图片缩小）
    public static int newWidth;
    public static int newHeight;
    public static int [] newImgPixels;
    //hough投票机制
    public static int [][]houghArray;
    //提高表现
    public static double [] sinCache;
    public static double [] cosCache;
    //每次hough角度范围不一样
    public static int [] maxTheta = new int[3];
    //统计字符像素比例
    public static int darkCnt;


    private static void setImgInfo() {
        img = ImgPretreatment.img;
        imgWidth = ImgPretreatment.imgWidth;
        imgHeight = ImgPretreatment.imgHeight;
        imgPixels = ImgPretreatment.imgPixels;
    }

    private static void setInitImg(){
        ImgPretreatment.img = img;
        ImgPretreatment.imgWidth = imgWidth;
        ImgPretreatment.imgHeight = imgHeight;
        ImgPretreatment.imgPixels = imgPixels;
    }
    //降采样
    public static void downSampling(int k){
        ArrayList<Integer> edgePoint = new ArrayList<Integer>();
        //减半取样 （奇偶一起处理）
        newWidth = imgWidth/k;
        newHeight = imgHeight/k;
        newImgPixels = new int[newWidth*newHeight];
        for(int i = 0; i <newHeight; i++) {
            for (int j = 0; j <newWidth; j++) {
                newImgPixels[i*newWidth+j] = imgPixels[imgWidth * k*i + k*j];
            }
        }

        //提取所有非轮廓点
        for(int i = 1; i <newHeight-1; i++) {
            for (int j = 1; j < newWidth-1; j++) {
                int nowPoint = i * newWidth + j;
                //如果当前点是黑色
                if(Color.blue(newImgPixels[nowPoint])==0) {
                    int upPoint = nowPoint - newWidth;
                    int downPoint = nowPoint + newWidth;
                    int rightPoint = nowPoint + 1;
                    int leftPoint = nowPoint - 1;
                    //如果都为黑色
                    if (Color.blue(newImgPixels[upPoint])==0 && Color.blue(newImgPixels[downPoint])==0 &&
                            Color.blue(newImgPixels[rightPoint])==0 && Color.blue(newImgPixels[leftPoint])==0)
                        edgePoint.add(nowPoint);
                }
            }

        }
        //将所有非轮廓点像素置为255(白色)
        for(int i=0;i<edgePoint.size();i++){
            newImgPixels[edgePoint.get(i)] = Color.rgb(255,255,255);
        }
    }

    public static void transform1 (){
        setImgInfo();
        //程序运行时间-开始
        Date startDate = new Date(System.currentTimeMillis());
        //降采样
        downSampling(1);
        //角度范围
        maxTheta[0] = 180;
        //步长
        final double thetaStep = Math.PI / maxTheta[0];
        //hough数组
        int houghHeight = (int)Math.ceil(Math.hypot(newWidth,newHeight));
        int doubleHeight = 2*houghHeight;
        houghArray = new int[maxTheta[0]][doubleHeight];
        //存储sin cos 值
        sinCache = new double[maxTheta[0]];
        cosCache = new double[maxTheta[0]];
        for (int i=0;i<maxTheta[0];i++){
            double realTheta = i*thetaStep;
            sinCache[i] = Math.sin(realTheta);
            cosCache[i] = Math.cos(realTheta);
        }
        addPoints(houghHeight);
        int maxCnt = 0;

        for(int i=0;i<maxTheta[0];i++){
            for(int j=0;j<2*houghHeight;j++){
                if(houghArray[i][j]>maxCnt)
                    maxCnt = houghArray[i][j];
            }
        }
        //System.out.println("最大的极值："+maxCnt);
        for(int i=0;i<maxTheta[0];i++) {
            for (int j = 0; j < 2 * houghHeight; j++) {
                if ((double)houghArray[i][j] < ((double)maxCnt*0.7))
                    houghArray[i][j] = 0;
            }
        }
        int temp = 0;
        int resultTheta = 0;
        for(int i=0;i<maxTheta[0];i++) {
            int totalCnt = 0;
            for (int j = 0; j < 2 * houghHeight; j++) {
                totalCnt += houghArray[i][j];
            }
            if(totalCnt > temp){
                resultTheta = i;
                temp = totalCnt;
            }
        }
        //程序运行时间-结束
        Date endDate = new Date(System.currentTimeMillis());
        long diff = endDate.getTime() - startDate.getTime();
        System.out.println("倾斜角度为："+resultTheta);
        System.out.println("Hough执行时间为"+diff);



        //旋转校正
        //以图像中心为原点左上角，右上角，左下角和右下角的坐标,用于计算旋转后的图像的宽和高
        double sina = Math.sin(thetaStep*resultTheta);
        double cosa = Math.cos(thetaStep*resultTheta);
        ArrayList<Integer> resultSet = new ArrayList<>();
        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int grey = imgPixels[imgWidth * i + j];
                if(Color.blue(grey)==0){
                    int newCor_X = j -imgWidth/2;
                    int newCor_Y = -i + imgHeight/2;
                    //旋转
                    int newRotate_X = 0;
                    int newRotate_Y = 0;
                    if(resultTheta<90) {
                        newRotate_X = (int) (newCor_X * cosa) + (int) (newCor_Y * sina);
                        newRotate_Y = (int) (-newCor_X * sina) + (int) (newCor_Y * cosa);
                    }else{
                        newRotate_X = (int) (-newCor_X * cosa) + (int) (-newCor_Y * sina);
                        newRotate_Y = (int) (newCor_X * sina) + (int) (-newCor_Y * cosa);
                    }
                    //最终结果
                    int newPoint_X = - newRotate_Y + imgHeight/2;
                    int newPoint_Y = newRotate_X + imgWidth/2;
                    if(newPoint_X<0||newPoint_X>=imgHeight||newPoint_Y<0||newPoint_Y>=imgWidth)
                        continue;
                    resultSet.add(newPoint_X * imgWidth + newPoint_Y);
                }
            }
        }
        //所有都变成白色
        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                imgPixels[imgWidth * i + j] = Color.rgb(255,255,255);
            }
        }
        //得到最后结果图
        for(int i = 0; i < resultSet.size(); i++) {
            imgPixels[resultSet.get(i)] = Color.rgb(0,0,0);
        }
        setInitImg();
        Util.returnPic(3);
    }
    public static void transform2(){
        setImgInfo();
        //降采样
        downSampling(1);
        //角度范围
        maxTheta[0] = 180;
        //步长
        final double thetaStep = Math.PI / maxTheta[0];
        //hough数组
        int houghHeight = (int)Math.ceil(Math.hypot(newWidth,newHeight));
        int doubleHeight = 2*houghHeight;
        houghArray = new int[maxTheta[0]][doubleHeight];
        //存储sin cos 值
        sinCache = new double[maxTheta[0]];
        cosCache = new double[maxTheta[0]];
        for (int i=0;i<maxTheta[0];i++){
            double realTheta = i*thetaStep;
            sinCache[i] = Math.sin(realTheta);
            cosCache[i] = Math.cos(realTheta);
        }
        addPoints(houghHeight);
        int maxCnt = 0;

        for(int i=0;i<maxTheta[0];i++){
            for(int j=0;j<2*houghHeight;j++){
                if(houghArray[i][j]>maxCnt)
                    maxCnt = houghArray[i][j];
            }
        }
        //System.out.println("最大的极值："+maxCnt);
        for(int i=0;i<maxTheta[0];i++) {
            for (int j = 0; j < 2 * houghHeight; j++) {
                if ((double)houghArray[i][j] < ((double)maxCnt*0.7))
                    houghArray[i][j] = 0;
            }
        }
        int temp = 0;
        int resultTheta = 0;
        for(int i=0;i<maxTheta[0];i++) {
            int totalCnt = 0;
            for (int j = 0; j < 2 * houghHeight; j++) {
                totalCnt += houghArray[i][j];
            }
            if(totalCnt > temp){
                resultTheta = i;
                temp = totalCnt;
            }
        }
        System.out.println("倾斜角度为："+resultTheta);



        //旋转校正
        //以图像中心为原点左上角，右上角，左下角和右下角的坐标,用于计算旋转后的图像的宽和高
        double sina = Math.sin(thetaStep*resultTheta);
        double cosa = Math.cos(thetaStep*resultTheta);
        ArrayList<Integer> resultSet = new ArrayList<>();
        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                int grey = imgPixels[imgWidth * i + j];
                if(Color.blue(grey)==0){
                    int newCor_X = j -imgWidth/2;
                    int newCor_Y = -i + imgHeight/2;
                    //旋转
                    int newRotate_X = 0;
                    int newRotate_Y = 0;
                    if(resultTheta<90) {
                        newRotate_X = (int) (newCor_X * sina) - (int) (newCor_Y * cosa);
                        newRotate_Y = (int) (newCor_X * cosa) + (int) (newCor_Y * sina);
                    }else{
                        newRotate_X = (int) (newCor_X * sina) - (int) (newCor_Y * cosa);
                        newRotate_Y = (int) (newCor_X * cosa) + (int) (newCor_Y * sina);
                    }
                    //最终结果
                    int newPoint_X = - newRotate_Y + imgHeight/2;
                    int newPoint_Y = newRotate_X + imgWidth/2;
                    if(newPoint_X<0||newPoint_X>=imgHeight||newPoint_Y<0||newPoint_Y>=imgWidth)
                        continue;
                    resultSet.add(newPoint_X * imgWidth + newPoint_Y);
                }
            }
        }
        //所有都变成白色
        for(int i = 0; i < imgHeight; i++) {
            for (int j = 0; j < imgWidth; j++) {
                imgPixels[imgWidth * i + j] = Color.rgb(255,255,255);
            }
        }
        //得到最后结果图
        for(int i = 0; i < resultSet.size(); i++) {
            imgPixels[resultSet.get(i)] = Color.rgb(0,0,0);
        }
        setInitImg();
        Util.returnPic(3);
    }
    //找出黑色像素点 0
    public static void addPoints(int houghHeight){
        for(int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                int grey = newImgPixels[newWidth * i + j];
                if(Color.blue(grey)==0)
                    addPoint(i,j,houghHeight);
            }
        }
    }
    //x轴向下 y轴向上
    public static void addPoint(int x,int y,int houghHeight){
        for (int i=0;i<maxTheta[0];i++){
            int r = (int)(x*cosCache[i]+y*sinCache[i]);
            //处理负数
            r += houghHeight;
            if(r<0||r>=2*houghHeight)continue;
            houghArray[i][r] ++ ;
        }
        darkCnt++;
    }

}
