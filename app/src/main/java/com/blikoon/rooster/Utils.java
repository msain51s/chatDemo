package com.blikoon.rooster;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.glidebitmappool.GlideBitmapFactory;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Administrator on 4/11/2017.
 */

public class Utils {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static AVLoadingIndicatorView loaderView;
    public static Dialog dialog;

        /*SHOW LOADER */

    public static TextView showLoader(Context context){

        dialog=new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view= LayoutInflater.from(context)
                .inflate(R.layout.custom_loader,null);
        loaderView= (AVLoadingIndicatorView) view.findViewById(R.id.custom_loader);
        final TextView textView= (TextView) view.findViewById(R.id.loader_text);

        dialog.setContentView(view);
        dialog.setCancelable(false);

        loaderView.show();
        dialog.show();

        return textView;
    }

  /*DISMISS LOADER*/

    public static void dismissLoader(){
        if(loaderView!=null){
            loaderView.hide();
            dialog.dismiss();
        }
    }




    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

/*FULL SCREEN IMAGE DIALOG*/

    public static void showFullImageDialog(final Context context,Bitmap imageBitmap)
    {
        final Dialog dialog=new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View prompView=LayoutInflater.from(context).inflate(R.layout.image_prompt,null);
        dialog.setCancelable(true);
        final ImageView view = (ImageView) prompView.findViewById(R.id.prompt_image_view);
        ImageView close_btn=(ImageView) prompView.findViewById(R.id.close_dialog_btn);

        dialog.setContentView(prompView);
        view.setImageBitmap(imageBitmap);
        dialog.show();
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

  /*IMAGE SAMPLING*/

    public  static Bitmap decodeSampledBitmapFromFile(String path, int sampleSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path ,options);

        // Calculate inSampleSize
        options.inSampleSize = sampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap loadSampledImage(String imageFile){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile ,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        int sampleSize=calculateInSampleSize(options, 500, 500);
        Bitmap bm=decodeSampledBitmapFromFile(imageFile, sampleSize);

        return bm;
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

  /*FILE OPEN THROUGH DEFAULT INTENT*/

    public static void openFile(Context context, String fileName){
        // Create URI
        File file=new File(fileName);
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (fileName.contains(".doc") || fileName.contains(".docx") || fileName.contains(".DOC")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(fileName.contains(".pdf") || fileName.contains(".PDF")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(fileName.contains(".ppt") || fileName.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(fileName.contains(".xls") ||fileName.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(fileName.contains(".zip") || fileName.contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if(fileName.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if(fileName.contains(".wav") || fileName.contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(fileName.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(fileName.contains(".jpg") || fileName.contains(".jpeg") || fileName.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(fileName.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(fileName.contains(".3gp") || fileName.contains(".mpg") || fileName.contains(".mpeg") || fileName.contains(".mpe") || fileName.contains(".mp4") || fileName.contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static Typeface getCustomFont(Context context, int type){
        Typeface typeface=null;
        if(type==FontType.ROBOTO_LIGHT)
            typeface=Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-Light.ttf");
        else if(type==FontType.ROBOTO_THIN_ITALIC)
            typeface= Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        else if(type==FontType.ROBOTO_THIN)
            typeface= Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-Thin.ttf");
        else if(type==FontType.ROBOTO_MEDIUM)
            typeface= Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-Medium.ttf");
        else if(type==FontType.ROBOTO_CONDENSED)
            typeface= Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-Condensed.ttf");
        else if(type==FontType.ROBOTO_BOLD)
            typeface= Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-Bold.ttf");
        else if(type==FontType.ROBOTO_REGULAR)
            typeface= Typeface.createFromAsset(AppController.mInstance.getAssets(), "fonts/Roboto-Regular.ttf");

        return typeface;
    }

    public static String capitalize(String input){

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    public static void compressImage(Uri uri){

        Bitmap imgBitmap=loadSampledImage(uri);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes=stream.toByteArray();
        try{
            File inf = new File(uri.getPath());
            inf.createNewFile();
            FileOutputStream fos = new FileOutputStream(inf);
            fos.write(imageBytes);
            fos.flush();
            fos.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static Bitmap loadSampledImage(Uri uri){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath() ,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        int sampleSize=calculateInSampleSize(options, 600, 600);
        Bitmap bm=decodeSampledBitmapFromFile(uri.getPath(), sampleSize);

        return bm;
    }


   /* public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public  static Bitmap decodeSampledBitmapFromFile(String path, int sampleSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path ,options);

        // Calculate inSampleSize
        options.inSampleSize = sampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }*/


    public static String getFormattedTime(long timestamp){

        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long timeDifference = System.currentTimeMillis() - timestamp;
        Date startDate=new Date(timestamp);
        Date endDate=new Date(System.currentTimeMillis());

        if(!DateFormat.format("dd/MM/yyyy", startDate).toString().equals(DateFormat.format("dd/MM/yyyy", endDate))) {
            return DateFormat.format("dd/MM/yyyy - hh:mm a", timestamp).toString();
        }
        else if( timeDifference < oneDayInMillis ) {
            return /*"Today - "+*/ DateFormat.format("hh:mm a", timestamp).toString();
        }else{
            return DateFormat.format("dd MMM yyyy - hh:mm a", timestamp).toString();
        }
    }
}
