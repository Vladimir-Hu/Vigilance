package com.bobo.vigilancetimer;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bobo.vigilancetimer.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private boolean isExit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            toast();
            return false;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 1) {
            MainActivity.this.finish();
        }
        return false;
    };

    protected void toast() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast textToast = Toast.makeText(this,getResources().getString(R.string.exitPrompt), Toast.LENGTH_LONG);
            textToast.show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        }
        else {
            finish();
            System.exit(0);
        }

    }


}