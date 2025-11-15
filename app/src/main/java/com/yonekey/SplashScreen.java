package com.yonekey;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import pl.droidsonroids.gif.GifImageView;

public class SplashScreen extends AppCompatActivity {

    private static SplashScreen instance;
    private ImageView splashImage;

    public static SplashScreen getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CRITICAL: Set fullscreen BEFORE calling super.onCreate() or setContentView()
        setupFullscreen();
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d("TOKEN", token);
                });


        String userAgent = WebSettings.getDefaultUserAgent(this);
        // Phone orientation setting for Android 8 (Oreo)
        if (userAgent.contains("Mobile") && android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            if (Config.PHONE_ORIENTATION == "auto") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            } else if (Config.PHONE_ORIENTATION == "portrait") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            } else if (Config.PHONE_ORIENTATION == "landscape") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
            }
            // Phone orientation setting for all other Android versions
        } else if (userAgent.contains("Mobile")) {
            if (Config.PHONE_ORIENTATION == "auto") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            } else if (Config.PHONE_ORIENTATION == "portrait") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (Config.PHONE_ORIENTATION == "landscape") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            // Tablet/Other orientation setting
        } else {
            if (Config.TABLET_ORIENTATION == "auto") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            } else if (Config.TABLET_ORIENTATION == "portrait") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (Config.TABLET_ORIENTATION == "landscape") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        setContentView(R.layout.splash_screen);
        instance = this;

        if (Config.PREVENT_SLEEP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        splashImage = findViewById(R.id.splash);

        try {
            subScribePushChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (Config.SPLASH_SCREEN_ACTIVATED) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Display display = getWindowManager().getDefaultDisplay();
            display.getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;

            if (Config.SCALE_SPLASH_IMAGE != 100) {
                // Do not run this if SCALE_SPLASH_IMAGE = 100 as otherwise no real full-screen
                ViewGroup.LayoutParams params = splashImage.getLayoutParams();
                if (width < height
                    /*&& (display.getRotation() == Surface.ROTATION_0
                    || display.getRotation() == Surface.ROTATION_180)*/) {
                    if (0 <= Config.SCALE_SPLASH_IMAGE && Config.SCALE_SPLASH_IMAGE <= 100) {
                        params.width = (int) (width * Config.SCALE_SPLASH_IMAGE / 100);
                    } else {
                        params.width = width;
                    }
                } else {
                    if (0 <= Config.SCALE_SPLASH_IMAGE && Config.SCALE_SPLASH_IMAGE <= 100) {
                        params.width = (int) (height * Config.SCALE_SPLASH_IMAGE / 100);
                    } else {
                        params.width = height;
                    }
                }
                params.height = params.width;
                splashImage.setLayoutParams(params);
            }
            Handler handler = new Handler();

            if (!Config.REMAIN_SPLASH_OPTION && Config.SPLASH_FADE_OUT == 0) {
                handler.postDelayed(this::finish, Config.SPLASH_TIMEOUT);
            }

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

        } else {
            splashImage.setVisibility(View.GONE);
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                finish();
            }, 0);
        }
    }

    private void setupFullscreen() {
        // Request window features before setContentView
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 and above - use WindowInsetsController
            window.setDecorFitsSystemWindows(false);
            WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, window.getDecorView());
            insetsController.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9 and 10
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            window.getDecorView().setSystemUiVisibility(flags);
        } else {
            // Android 8 and below
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );

            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            window.getDecorView().setSystemUiVisibility(flags);
        }

        // Make status and navigation bars transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void subScribePushChannel() {
        try {
            if (Config.FIREBASE_PUSH_ENABLED) {
                if (BuildConfig.IS_DEBUG_MODE) Log.d("Splash_Screen", "Subscribing to topic");

                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic(Config.firebasechanneltopic)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = getString(R.string.msg_subscribed);
                                if (BuildConfig.IS_DEBUG_MODE)
                                    Log.d("Token", String.valueOf(FirebaseMessaging.getInstance()));
                                if (!task.isSuccessful()) {
                                    msg = getString(R.string.msg_subscribe_failed);
                                }
                                if (BuildConfig.IS_DEBUG_MODE) Log.d("Splash_Screen", msg);
                            }
                        });
                // [END subscribe_topics]
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void animateFinish() {
        findViewById(android.R.id.content).animate()
                .alpha(0f)
                .setDuration(Config.SPLASH_FADE_OUT)
                .withEndAction(this::finish)
                .start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reapply fullscreen when activity resumes
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, window.getDecorView());
            insetsController.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Reapply fullscreen when window gains focus
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, window.getDecorView());
                insetsController.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            } else {
                int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                window.getDecorView().setSystemUiVisibility(flags);
            }
        }
    }
}