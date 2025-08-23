package ru.schneider_dev.tronfg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    private RelativeLayout mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Настройки окна для фиксированной альбомной ориентации
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Полноэкранный режим - скрываем статус бар
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        
        // Скрываем системную навигацию (кнопки назад, домой, переключение приложений)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                               WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                               WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                               WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        
        // Дополнительные настройки для скрытия системных элементов
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            // Для Android 9+ используем системные настройки для скрытия навигации
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                           View.SYSTEM_UI_FLAG_FULLSCREEN |
                           View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                           View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                           View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                           View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            
            // Обработчик для поддержания скрытого состояния
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    // Если системные элементы стали видимыми, скрываем их снова
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                }
            });
        }
        
        // Поддержка изменения размера окна
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 
                           WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        
        // Дополнительные флаги для API 35+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
                               WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        mainView = new RelativeLayout(this);
        setContentView(mainView);
        
        try {
            // Настройки OpenGL для совместимости с API 35 и поддержки больших экранов
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.useGL30 = false; // Используем OpenGL ES 2.0 для совместимости
            config.useAccelerometer = false;
            config.useCompass = false;
            config.r = 8; // Red bits
            config.g = 8; // Green bits
            config.b = 8; // Blue bits
            config.a = 8; // Alpha bits
            config.depth = 16; // Depth bits
            config.stencil = 0; // Stencil bits
            config.numSamples = 0; // Anti-aliasing
            
            // Поддержка изменения размера и больших экранов
            // Настройки для больших экранов управляются через AndroidManifest.xml
            config.useImmersiveMode = false; // Отключаем immersive mode для больших экранов
            
            // Дополнительные настройки для API 35
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                config.useWakelock = false;
            }
            
            View gameView = initializeForView(new TRONgame(gameCallback), config);
            
            // Настройки для API 35
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Принудительно устанавливаем слой для SurfaceView
                if (gameView instanceof android.view.SurfaceView) {
                    android.view.SurfaceView surfaceView = (android.view.SurfaceView) gameView;
                    surfaceView.setZOrderOnTop(false);
                    surfaceView.setZOrderMediaOverlay(false);
                }
            }
            
            mainView.addView(gameView);
        } catch (Exception e) {
            android.util.Log.e("TRONFG", "Error initializing libGDX: " + e.getMessage(), e);
        }
    }

    private GameCallback gameCallback = new GameCallback() {
        @Override
        public void sendMessage(int message) {
            if (message == TRONgame.OPEN_MARKET) {
                Uri uri = Uri.parse(getString(R.string.share_url));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            } else if (message == TRONgame.SHRE) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareTitle = getString(R.string.share_title);
                String shareBody = getString(R.string.share_body);
                String url = getString(R.string.share_url);

                String body = shareBody + url;

                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, body);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Восстанавливаем скрытое состояние системных элементов
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                           View.SYSTEM_UI_FLAG_FULLSCREEN |
                           View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                           View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                           View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                           View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Приложение приостановлено
    }
    

}
