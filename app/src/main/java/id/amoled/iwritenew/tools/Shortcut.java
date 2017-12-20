package id.amoled.iwritenew.tools;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by TIUNIDA on 04/12/2017.
 *
 * Class ini sengaja dibuat untuk mempermudah proses koding, agar tidak mengetik berulang kali.
 *
 * Class ini berisi fitur yang sering dipakai, contoh: TOAST, RANDOM STRING, DATE, SNACK BAR
 * Jadi ketika ingin membuat TOAST, maka panggil saja Class ini.
 */

public class Shortcut{

    public Shortcut(){

    }

    public static void makeToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String random() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            char c = chars[generator.nextInt(chars.length)];
            randomStringBuilder.append(c);
        }
        return randomStringBuilder.toString();
    }

    public static String currentTime(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm a");
        // you can get seconds by adding  "...:ss" to it
        date.setTimeZone(TimeZone.getTimeZone("GMT+7:00"));

        return date.format(currentLocalTime);
    }

    public static String currentDate(){
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

    public static void makeSnackBar(View view, String message){
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /*public static void makeAlertDialogSimple(Context context, String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/
}


