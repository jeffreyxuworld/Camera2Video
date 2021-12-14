package com.worldtech.camera2video.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.worldtech.camera2video.App;
import com.worldtech.camera2video.R;


public class CustomToast {
    public static void showToast(int txtid){
        try {
            View toastRoot = LayoutInflater.from(App.context).inflate(R.layout.custom_toast, null);
            TextView mTextView = toastRoot.findViewById(R.id.message);
            mTextView.setText(txtid);
            Toast toastStart = new Toast(App.context);
            WindowManager wm = (WindowManager) App.context.getSystemService(Context.WINDOW_SERVICE);
            int height = wm.getDefaultDisplay().getHeight();
            toastStart.setGravity(Gravity.TOP, 0, height / 2);
            toastStart.setDuration(Toast.LENGTH_SHORT);
            toastStart.setView(toastRoot);
            toastStart.show();
        }catch (Exception e){

        }

    }
    public static void showToast(String name){
        try {
            View toastRoot = LayoutInflater.from(App.context).inflate(R.layout.custom_toast, null);
            TextView mTextView = toastRoot.findViewById(R.id.message);
            mTextView.setText(name);
            Toast toastStart = new Toast(App.context);
            WindowManager wm = (WindowManager) App.context.getSystemService(Context.WINDOW_SERVICE);
            int height = wm.getDefaultDisplay().getHeight();
            toastStart.setGravity(Gravity.TOP, 0, height / 2);
            toastStart.setDuration(Toast.LENGTH_SHORT);
            toastStart.setView(toastRoot);
            toastStart.show();
        }catch (Exception e){

        }

    }
}
