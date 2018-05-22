package com.tang.trade.tang.utils;

/**
 * Created by Administrator on 2018/3/5.
 */

public class StringUtil {



    /***
     *判断是否包含=分隔符
     */

    public static boolean getStringDelimiter(String str){
        if (str.contains("_")||str.contains(".")){
            return true;
        }
        return false;

    }

    public static String[] getStringaArr(String str){
        String[] arr=new String[2];
        if (str.contains("_")&&str.contains(".")){
            int  length = str.indexOf(".");
            int  length2 = str.indexOf("_");
            if (length<length2){
                arr[0]= str.substring(0, length);
                arr[1]=str.substring(length+1,str.length());
                return arr;

            }else{
                arr[0]= str.substring(0, length2);
                arr[1]=str.substring(length2+1,str.length());
                return arr;
            }

        }else if (str.contains("_")){
            int  length = str.indexOf("_");
            arr[0]= str.substring(0, length);
            arr[1]=str.substring(length+1,str.length());
            return arr;

        }else if (str.contains(".")){
            int  length = str.indexOf(".");
            arr[0]= str.substring(0, length);
            arr[1]=str.substring(length+1,str.length());
            return arr;
        }

        return null;


    }



}
