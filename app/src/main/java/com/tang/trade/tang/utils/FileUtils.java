package com.tang.trade.tang.utils;

import com.tang.trade.tang.net.TangConstant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/10/19.
 */

public class FileUtils {


    /**
     *
     * @param dir
     * @return
     */
    public static String[] getFileName(File dir){
        String[]  fileList  = null;

        if (dir.isDirectory()) {

            File[] files = dir.listFiles();
            if (files.length>0){
                fileList = new String[files.length];
                for (int i = 0; i<files.length;i++){
                    fileList[i] = files[i].getName();
                }
            }

        }

        return fileList;
    }


    public static String[] getFileName1(File dir){
        String[]  fileList  = null;


        if (dir.isDirectory()) {

            File[] files = dir.listFiles();
            if (files != null && files.length>0){
                fileList = new String[files.length];
                for (int i = 0; i<files.length;i++){
                    String end = files[i].getName().substring(files[i].getName().lastIndexOf(".") + 1, files[i].getName	().length()).toLowerCase();
                    if (end.equals("txt") || end.equals("bin") || end.equals("doc") || end.equals("pdf") ){
                        fileList[i] = files[i].getName();
                    }
                }
            }

        }

        return fileList;
    }

    public static String copyFile(File sourcefile,String file3,String file1) {

        String str = "";
        File file=new File(file3);
        if (file.mkdirs()){
            file.mkdirs();
        }

        File file2=new File(file3+file1);
        if(!file2.exists()){
            try {
                file2.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        FileInputStream input = null;
        BufferedInputStream inbuff = null;
        FileOutputStream out = null;
        BufferedOutputStream outbuff = null;

        try {

            input = new FileInputStream(sourcefile);
            inbuff = new BufferedInputStream(input);

            out = new FileOutputStream(file2);
            outbuff = new BufferedOutputStream(out);

            byte[] b = new byte[1024 * 5];
            int len = 0;
            while ((len = inbuff.read(b)) != -1) {
                outbuff.write(b, 0, len);
            }

            outbuff.flush();
        } catch (Exception ex) {
            str = "1";
            return str;

        } finally {
            try {

                if (inbuff != null)
                    inbuff.close();
                if (outbuff != null)
                    outbuff.close();
                if (out != null)
                    out.close();
                if (input != null)
                    input.close();
            } catch (Exception ex) {
                str = "1";
                return str;
            }
        }
        return str;
    }



    public static String readSDFile(String fileName)  {

        String res = "";
        File file = new File(fileName);

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int length = fis.available();

            byte [] buffer = new byte[length];
            fis.read(buffer);

            res = new String(buffer, "UTF-8");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return res;
    }


    public static int backUpCli(String oldPath, String newPath) {
        int result = 0;
        File destDir1 = new File(TangConstant.PATH_CLI_BACKUP);
        if (!destDir1.exists()) {
            destDir1.mkdirs();
        }

        File file=new File(newPath);//创建文件
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                result = -1;
            }
        }

        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }

        return result;
    }
}
