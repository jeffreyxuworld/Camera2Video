package com.worldtech.camera2video.activity;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.worldtech.camera2video.R;
import com.worldtech.camera2video.fragment.RecordVideo2Fragment;
import com.worldtech.camera2video.fragment.RecordVideoFragment;


public class RecordVideoActivity extends FragmentActivity {

    private ImageView cancel;
    private Fragment videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);


        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            finishFragment();
        });
        videoFragment = getRecordVideoFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_container,videoFragment ,"RecordVideoFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        finishFragment();
    }

    public Fragment getRecordVideoFragment(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new RecordVideoFragment();
        } else {
            return new RecordVideo2Fragment();
        }
    }
    public void finishFragment(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
//            ((RecordVideoFragment)videoFragment).exit();
        }else {
            finish();
        }

    }
}
