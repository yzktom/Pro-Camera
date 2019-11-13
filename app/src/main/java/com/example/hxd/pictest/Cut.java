package com.example.hxd.pictest;


import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;
import static java.lang.System.lineSeparator;


public class Cut {
    public static Bitmap img;
    public static int imgWidth;
    public static int imgHeight;
    public static int[] imgPixels;

    //返回字符真正的比率
    public static double getRadio(int right,int left,int down,int top){
        double width = right - left;
        double height = down - top;
        double radio1 = width/height;
        double radio2 = height/width;
        //比率
        double radio;
        if(radio1>1)
            radio = radio2;
        else
            radio = radio1;
        return radio;
    }

    //返回字符 宽/高
    public static double getRadio2(int right,int left,int down,int top){
        double width = right - left;
        double height = down - top;
        double radio = width/height;
        return radio;
    }

    //返回字符 宽/高(如果大于1置为0)
    public static double getRadio3(int right,int left,int down,int top){
        double width = right - left;
        double height = down - top;
        double radio = width/height;
        if(radio<=1)
            return radio;
        else
            return 0;
    }

    //返回字符 高/宽(如果大于1.2置为0)
    public static double getRadio4(int right,int left,int down,int top){
        double width = right - left;
        double height = down - top;
        double radio = height/width;
        if(radio<=1.2)
            return radio;
        else
            return 0;
    }

    //获得上一步图像
    private static void setImgInfo() {
        img = ImgPretreatment.img;
        imgWidth = ImgPretreatment.imgWidth;
        imgHeight = ImgPretreatment.imgHeight;
        imgPixels = ImgPretreatment.imgPixels;
    }
    //设置结果
    private static void setInitImg(){
        ImgPretreatment.img = img;
        ImgPretreatment.imgWidth = imgWidth;
        ImgPretreatment.imgHeight = imgHeight;
        ImgPretreatment.imgPixels = imgPixels;
    }

    public static int getHistoByChanceForGap(List<Integer> gap,int maxGap){
        double []getHisto = new double[maxGap+1];
        for(int i=0;i<gap.size();i++){
            getHisto[gap.get(i)]++;
        }
        //测试
        for(int i = 0;i<getHisto.length;i++){
            System.out.println("间隔"+i+"个像素点："+getHisto[i]+"个");
        }
        //求概率
        for(int i=0;i<getHisto.length;i++){
            getHisto[i] /= gap.size();
        }
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


    public static Bitmap doCut(Bitmap bitmap){
        //bitmap 已完成了初始化工作
        ImgPretreatment.doPretreatment(bitmap);
        HoughTransform.transform1();
        //行切分
        doRowCut();

        return Util.returnPic(4);
    }

    public static Bitmap doCut2(Bitmap bitmap){
        //bitmap 已完成了初始化工作
        ImgPretreatment.doPretreatment(bitmap);
        HoughTransform.transform2();
        //列切分
        doColCut();

        return Util.returnPic(4);
    }

    public static Bitmap doCut3(Bitmap bitmap){
        //bitmap 已完成了初始化工作
        ImgPretreatment.doPretreatment(bitmap);
        HoughTransform.transform1();
        //行切分
        doRowCut();
        HoughTransform.transform2();
        //列切分
        doColCut();

        return Util.returnPic(4);
    }

    public static int getMin(int left,int right){
        return left<right?left:right;
    }

    public static int getMax(int left,int right){
        return left>right?left:right;
    }

    public static int[] getNewTopAndDown(int left,int right,int top,int down){
        int []topAndDown = new int[2];
        boolean firstLine = false;
        for(int i = top;i<=down;i++){
            for(int j=left;j<=right;j++){
                boolean isBlack = Color.blue(imgPixels[i*imgWidth+j])==0 ? true:false;
                //找到第一个遇到的黑色素点
                if(firstLine==false&&isBlack){
                    topAndDown[0]=i;
                    topAndDown[1]=i;
                    firstLine = true;
                    break;
                }
                if(isBlack) {
                    topAndDown[1] = i;
                    break;
                }
            }
        }
        return topAndDown;
    }

    public static void drawLine(List<Character> charRegion){
        int top,down,left,right;
        for(int i =0 ;i<charRegion.size();i++){
            top = charRegion.get(i).top;
            down = charRegion.get(i).down;
            left = charRegion.get(i).left;
            right = charRegion.get(i).right;
            //上下两行为红色
            for(int j = left;j<=right;j++){
                imgPixels[top*imgWidth+j] = Color.rgb(255,0,0);
                imgPixels[(top+1)*imgWidth+j] = Color.rgb(255,0,0);
                imgPixels[down*imgWidth+j] = Color.rgb(255,0,0);
                imgPixels[(down-1)*imgWidth+j] = Color.rgb(255,0,0);
            }
            //左右两列为红色
            for(int p = top;p<=down;p++){
                imgPixels[p*imgWidth+left] = Color.rgb(255,0,0);
                imgPixels[p*imgWidth+left+1] = Color.rgb(255,0,0);
                imgPixels[p*imgWidth+right] = Color.rgb(255,0,0);
                imgPixels[p*imgWidth+right-1] = Color.rgb(255,0,0);
            }
        }
        //更新原图片颜色
        setInitImg();
    }

    public static void drawLine2(List<Character> temp){
        int top,down,left,right;
        for(int i =0 ;i<temp.size();i++){
            top = temp.get(i).top;
            down = temp.get(i).down;
            left = temp.get(i).left;
            right = temp.get(i).right;
            //上下两行为红色
            for(int j = left;j<=right;j++){
                imgPixels[top*imgWidth+j] = Color.rgb(255,0,0);
                imgPixels[(top-1)*imgWidth+j] = Color.rgb(255,0,0);
                imgPixels[down*imgWidth+j] = Color.rgb(255,0,0);
                imgPixels[(down+1)*imgWidth+j] = Color.rgb(255,0,0);
            }
            //左右两列为红色
            for(int p = top;p<=down;p++){
                imgPixels[p*imgWidth+left] = Color.rgb(255,0,0);
                imgPixels[p*imgWidth+left-1] = Color.rgb(255,0,0);
                imgPixels[p*imgWidth+right] = Color.rgb(255,0,0);
                imgPixels[p*imgWidth+right+1] = Color.rgb(255,0,0);
            }
        }
        //更新原图片颜色
        setInitImg();
    }

    public static void drawLine3(List<Integer> allStartLine,List<Integer> allEndLine){
        for(int i = 0;i<imgWidth;i++){
            for(int j=0;j<allStartLine.size();j++){
                imgPixels[allStartLine.get(j)*imgWidth+i] = Color.rgb(255,0,0);
                imgPixels[(allStartLine.get(j)+1)*imgWidth+i] = Color.rgb(255,0,0);
                imgPixels[allEndLine.get(j)*imgWidth+i] = Color.rgb(255,0,0);
                imgPixels[(allEndLine.get(j)-1)*imgWidth+i] = Color.rgb(255,0,0);
            }
        }
    }

    public static void doRowCut(){
        setImgInfo();
        //文本行
        List<Integer> allStartLine = new ArrayList<Integer>();
        List<Integer> allEndLine = new ArrayList<Integer>();
        int lineSum = 0;//总行数
        //每一行存放在一个List中
        List<List<Integer>> col_Lefts  = new ArrayList<List<Integer>>();
        List<List<Integer>> col_Rights = new ArrayList<List<Integer>>();
        int colSum = 0;//总列数
        boolean inLine = false;
        for(int i = 0; i < imgHeight; i++)  {
            int lineCnt = 0;
            for(int j = 0; j < imgWidth; j++) {
                if(Color.blue(imgPixels[i*imgWidth+j]) == 0)
                    lineCnt ++;
            }
            if(lineCnt >5&&inLine==false) {
                allStartLine.add(i);
                inLine = true;
            }
            if(lineCnt<=5&&inLine==true){
                allEndLine.add(i-1);
                inLine = false;
            }
            if(lineCnt>5&&i == imgHeight - 1){
                allEndLine.add(i);
            }
        }
        if(allStartLine.size()!=allEndLine.size()){
            System.out.println("首尾行不匹配");
            exit(1);
        }else{
            lineSum = allStartLine.size();
        }
        //对于每个文本行
        for(int k = 0;k<lineSum;k++){
            //每一行存放在一个List中
            List<Integer> col_Left = new ArrayList<>();
            List<Integer> col_Right = new ArrayList<>();
            boolean inCol = false;
            for(int j = 0;j<imgWidth;j++){
                //从0列到最后 统计每一列的黑色素点数
                int colCnt = 0;
                //从文本行上边界到下边界
                for(int i = allStartLine.get(k);i<=allEndLine.get(k);i++){
                    if(Color.blue(imgPixels[i*imgWidth+j]) == 0)
                        colCnt ++;
                }
                if(colCnt >0&&inCol==false) {
                    col_Left.add(j);
                    inCol = true;
                }
                if(colCnt ==0&&inCol==true){
                    col_Right.add(j-1);
                    inCol = false;
                }
                if(colCnt>0&&j == imgWidth - 1){
                    col_Right.add(j);
                }
            }
            col_Lefts.add(col_Left);
            col_Rights.add(col_Right);
        }
//
//        if(col_Left.size()!=col_Right.size()){
//            System.out.println("首尾列不匹配");
//            exit(1);
//        }else{
//            colSum = col_Left.size();
//        }

        //存放所有连通域
        List<Character> temp = new ArrayList<Character>();
        int [] newTopAndDown = new int[2];
        for(int i = 0;i<col_Lefts.size();i++){
            for(int j=0;j<col_Lefts.get(i).size();j++){
                int left = col_Lefts.get(i).get(j);
                int right = col_Rights.get(i).get(j);
                int old_Top = allStartLine.get(i);
                int old_Down = allEndLine.get(i);
                newTopAndDown = getNewTopAndDown(left,right,old_Top,old_Down);
                int top = newTopAndDown[0];
                int down = newTopAndDown[1];
                temp.add(new Character(top,down,left,right));
//                temp.add(new Character(old_Top,old_Down,left,right));
            }
        }

        //存放所有的字符间隙
        List<Integer> gap = new ArrayList<>();
        for(int i =0;i<temp.size()-1;i++){
            if(temp.get(i).down > temp.get(i+1).top){
                gap.add(temp.get(i+1).left - temp.get(i).right);
            }
        }
        int maxGap = 0;
        int minGap = 0;
        if(gap.size()>0){
            maxGap = gap.get(0);
            minGap = gap.get(0);
            //求最大值&最小值
            for(int i = 0 ;i<gap.size();i++){
                if(gap.get(i)>maxGap)
                    maxGap = gap.get(i);
                if(gap.get(i)<minGap)
                    minGap = gap.get(i);
            }
        }

        //gap阈值
        int threshold = getHistoByChanceForGap(gap,maxGap);
        System.out.println("gap阈值是："+threshold);
        System.out.println("连通域总数是："+temp.size());
        for(int i = 0;i<temp.size();i++){
            System.out.println(getRadio(temp.get(i).right,temp.get(i).left,temp.get(i).down,temp.get(i).top));
        }
        //释放内存
        allStartLine = null;
        allEndLine = null;
        col_Lefts = null;
        col_Rights = null;

        //存放所有字符
        List<Character> charRegion = new ArrayList<Character>();
        for(int i = 0;i<temp.size();i++){
            double radio = getRadio(temp.get(i).right,temp.get(i).left,temp.get(i).down,temp.get(i).top);
            if(radio<0.2){
                charRegion.add(temp.get(i));
                continue;
            }
            //记录连通域i到i+k之间的合并为一个字符
            int k =0;
            while ( (i+k+1< temp.size()) && (temp.get(i+k+1).left-temp.get(i).right)<=threshold && temp.get(i+k+1).top<temp.get(i).down) {
                double radio2 = getRadio(temp.get(i+k+1).right,temp.get(i).left,getMax(temp.get(i+k+1).down,temp.get(i).down),getMin(temp.get(i+k+1).top,temp.get(i).top));
                if(radio2<0.2){
                    charRegion.add(temp.get(i));
                    break;
                }
                if(radio2 > radio){
                    k++;
                    radio = radio2;
                    temp.get(i).right = temp.get(i+k).right;
                    temp.get(i).top = getMin(temp.get(i+k).top,temp.get(i).top);
                    temp.get(i).down = getMax(temp.get(i+k).down,temp.get(i).down);
                }else{
                    break;
                }
            }
            charRegion.add(temp.get(i));
            i = i+k;
        }

//        for(int i = 0;i<temp.size();i++){
//            //第一个连通域的radio
//            double radio1 = getRadio3(temp.get(i).right,temp.get(i).left,temp.get(i).down,temp.get(i).top);
//            if(i<temp.size()-2) {
//                //如果下一个连通域在下一行就不再判断
//                if (temp.get(i).top == temp.get(i + 1).top) {
//                    //第二个连通域的radio
//                    double radio2 = getRadio3(temp.get(i+1).right,temp.get(i).left,temp.get(i+1).down,temp.get(i+1).top);
//                    if (radio2 > radio1) {
//                        //如果下一个连通域在下一行就不再判断
//                        if (temp.get(i + 1).top == temp.get(i + 2).top) {
//                            //第三个连通域的radio
//                            double radio3 = getRadio3(temp.get(i+2).right,temp.get(i).left,temp.get(i+2).down,temp.get(i+2).top);
//                            if (radio3 > radio2) {
//                                temp.get(i).right = temp.get(i + 2).right;
//                                temp.get(i).unAvailable();
//                                temp.get(i+1).unAvailable();
//                                temp.get(i+2).unAvailable();
//                                charRegion.add(temp.get(i));
//                                i += 2;
//                            } else {
//                                temp.get(i).right = temp.get(i + 1).right;
//                                temp.get(i).unAvailable();
//                                temp.get(i+1).unAvailable();
//                                charRegion.add(temp.get(i));
//                                i += 1;
//                            }
//                        } else {
//                            temp.get(i).right = temp.get(i + 1).right;
//                            temp.get(i).unAvailable();
//                            temp.get(i+1).unAvailable();
//                            charRegion.add(temp.get(i));
//                            i += 1;
//                        }
//                    } else {
//                        temp.get(i).unAvailable();
//                        charRegion.add(temp.get(i));
//                    }
//                } else {
//                    temp.get(i).unAvailable();
//                    charRegion.add(temp.get(i));
//                }
//            }
//            if(i==temp.size()-2&&temp.get(i).available){
//                //判断下一个连通域
//                //如果下一个连通域在下一行就不再判断
//                if (temp.get(i).top == temp.get(i + 1).top) {
//                    //第二个连通域的radio
//                    double radio2 = getRadio3(temp.get(i+1).right,temp.get(i).left,temp.get(i+1).down,temp.get(i+1).top);
//                    if (radio2 > radio1) {
//                            temp.get(i).right = temp.get(i + 1).right;
//                            temp.get(i).unAvailable();
//                            temp.get(i+1).unAvailable();
//                            charRegion.add(temp.get(i));
//                            i += 1;
//                    } else {
//                        temp.get(i).unAvailable();
//                        charRegion.add(temp.get(i));
//                    }
//                } else {
//                    temp.get(i).unAvailable();
//                    charRegion.add(temp.get(i));
//                }
//            }
//            if(i == temp.size()-1&&temp.get(i).available) {
//                temp.get(i).unAvailable();
//                charRegion.add(temp.get(i));
//            }
//        }

        //测试
//        drawLine3(allStartLine,allEndLine);
        drawLine(temp);
    }

    public static void doColCut(){
        setImgInfo();
        //文本列
        List<Integer> allStartCol = new ArrayList<Integer>();
        List<Integer> allEndCol = new ArrayList<Integer>();
        int colSum = 0;//总列数
        //所有连通域的上下边界
        List<Integer> row_Up = new ArrayList<Integer>();
        List<Integer> row_Down = new ArrayList<Integer>();
        int rowSum = 0;//总行数
        boolean inCol = false;
        for(int i = 0; i < imgWidth; i++)  {
            int colCnt = 0;
            for(int j = 0; j < imgHeight; j++) {
                if(Color.blue(imgPixels[j*imgWidth+i]) == 0)
                    colCnt ++;
            }
            if(colCnt >5&&inCol==false) {
                allStartCol.add(i);
                inCol = true;
            }
            if(colCnt<=5&&inCol==true){
                allEndCol.add(i-1);
                inCol = false;
            }
            if(colCnt>5&&i == imgWidth - 1){
                allEndCol.add(i);
            }
        }
        if(allStartCol.size()!=allEndCol.size()){
            System.out.println("首尾行不匹配");
            exit(1);
        }else{
            colSum = allStartCol.size();
        }
        //对于每个文本列
        for(int k = 0;k<colSum;k++){
            boolean inRow = false;
            for(int j = 0;j<imgHeight;j++){
                //从0行到最后 统计每一行的黑色素点数
                int rowCnt = 0;
                //从文本行上边界到下边界
                for(int i = allStartCol.get(k);i<=allEndCol.get(k);i++){
                    if(Color.blue(imgPixels[j*imgWidth+i]) == 0)
                        rowCnt ++;
                }
                if(rowCnt >0&&inRow==false) {
                    row_Up.add(j);
                    inRow = true;
                }
                if(rowCnt ==0&&inRow==true){
                    row_Down.add(j-1);
                    inRow = false;
                }
                if(rowCnt>0&&j == imgHeight - 1){
                    row_Down.add(j);
                }
            }
            row_Up.add(-1);
            row_Down.add(-1);
        }

        if(row_Up.size()!=row_Down.size()){
            System.out.println("首尾列不匹配");
            exit(1);
        }else{
            rowSum = row_Up.size();
        }

        Character [] connectRegion = new Character[rowSum];
        //去掉-1
        List<Character> temp = new ArrayList<Character>();
        //存放所有字符域（组）
        List<Character> charRegion = new ArrayList<Character>();
        //第k列
        int k = 0;
        for(int i = 0;i<rowSum;i++){
            if(row_Up.get(i)!=-1&&row_Down.get(i)!=-1) {
                connectRegion[i] = new Character(row_Up.get(i), row_Down.get(i), allStartCol.get(k), allEndCol.get(k));
            }else{
                k++;
            }
        }
        for(int i = 0;i<rowSum;i++){
            if(connectRegion[i]!=null)
                temp.add(connectRegion[i]);
        }

        //释放内存
        allStartCol = null;
        allEndCol = null;
        row_Up = null;
        row_Down = null;
        connectRegion = null;

        for(int i = 0;i<temp.size();i++){
            //第一个连通域的radio
            double radio1 = getRadio4(temp.get(i).right,temp.get(i).left,temp.get(i).down,temp.get(i).top);
            if(i<temp.size()-2) {
                //如果下一个连通域在下一列就不再判断
                if (temp.get(i).right == temp.get(i + 1).right) {
                    //第二个连通域的radio
                    double radio2 = getRadio4(temp.get(i).right,temp.get(i).left,temp.get(i+1).down,temp.get(i).top);
                    if (radio2 > radio1) {
                        //如果下一个连通域在下一行就不再判断
                        if (temp.get(i + 1).right == temp.get(i + 2).right) {
                            //第三个连通域的radio
                            double radio3 = getRadio4(temp.get(i).right,temp.get(i).left,temp.get(i+2).down,temp.get(i).top);
                            if (radio3 > radio2) {
                                temp.get(i).down = temp.get(i + 2).down;
                                temp.get(i).unAvailable();
                                temp.get(i+1).unAvailable();
                                temp.get(i+2).unAvailable();
                                charRegion.add(temp.get(i));
                                i += 2;
                            } else {
                                temp.get(i).down = temp.get(i + 1).down;
                                temp.get(i).unAvailable();
                                temp.get(i+1).unAvailable();
                                charRegion.add(temp.get(i));
                                i += 1;
                            }
                        } else {
                            temp.get(i).down = temp.get(i + 1).down;
                            temp.get(i).unAvailable();
                            temp.get(i+1).unAvailable();
                            charRegion.add(temp.get(i));
                            i += 1;
                        }
                    } else {
                        temp.get(i).unAvailable();
                        charRegion.add(temp.get(i));
                    }
                } else {
                    temp.get(i).unAvailable();
                    charRegion.add(temp.get(i));
                }
            }
            if(i==temp.size()-2&&temp.get(i).available){
                //判断下一个连通域
                //如果下一个连通域在下一列就不再判断
                if (temp.get(i).right == temp.get(i + 1).right) {
                    //第二个连通域的radio
                    double radio2 = getRadio4(temp.get(i).right,temp.get(i).left,temp.get(i+1).down,temp.get(i).top);
                    if (radio2 > radio1) {
                        temp.get(i).down = temp.get(i + 1).down;
                        temp.get(i).unAvailable();
                        temp.get(i+1).unAvailable();
                        charRegion.add(temp.get(i));
                        i += 1;
                    } else {
                        temp.get(i).unAvailable();
                        charRegion.add(temp.get(i));
                    }
                } else {
                    temp.get(i).unAvailable();
                    charRegion.add(temp.get(i));
                }
            }
            if(i == temp.size()-1&&temp.get(i).available) {
                temp.get(i).unAvailable();
                charRegion.add(temp.get(i));
            }
        }
        //测试
        drawLine(charRegion);
    }

}


