package ru.schneider_dev.tronfg;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;



public class AndroidLauncher extends AndroidApplication {

    private RelativeLayout mainView;

    private AdView bannerView;
    private ViewGroup bannerContainer;
    private RelativeLayout.LayoutParams bannerParams;

    private InterstitialAd interstitial;
    private boolean interstitialHasInited = false;
    private boolean enableAdmobInterstitials;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        mainView = new RelativeLayout(this);
        setContentView(mainView);

        View gameView = initializeForView(new TRONgame(gameCallback));
        mainView.addView(gameView);

        bannerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bannerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bannerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bannerContainer = new LinearLayout(this);

        mainView.addView(bannerContainer, bannerParams);
        bannerContainer.setVisibility(View.GONE);

        if (Setting.ADMOB_INTERSTITIAL == null) {
            enableAdmobInterstitials = false;
        } else {
            enableAdmobInterstitials = true;
        }
//        MobileAds.initialize(this, "ca-app-pub-6581884367254515~9239963339");
        new AdmobBannerTask().execute();

    }

    private GameCallback gameCallback = new GameCallback() {
        @Override
        public void sendMessage(int message) {
            if (message == TRONgame.SHOW_BANNER) {
                AndroidLauncher.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBanner();
                    }
                });
            } else if (message == TRONgame.HIDE_BANNER) {
                AndroidLauncher.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideBanner();
                    }
                });
            } else if (message == TRONgame.LOAD_INTERSTITIAL) {
                loadAdmobInterstitial();

            } else if (message == TRONgame.SHOW_INTERSTITIAL) {
                AndroidLauncher.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAdmobInterstitial();
                    }
                });

            } else if (message == TRONgame.OPEN_MARKET) {
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

    private class AdmobBannerTask extends AsyncTask<Void, Void, AdView> {
        @Override
        protected AdView doInBackground(Void... params) {
            AdView adView = new AdView(AndroidLauncher.this);

            return adView;
        }

        @Override
        protected void onPostExecute(AdView adView) {

            bannerView = adView;
            bannerView.setAdSize(AdSize.BANNER);
            bannerView.setAdUnitId(Setting.ADMOB_BANNER);

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(Setting.TEST_DEVICE)
                    .build();

            bannerView.setAdListener(bannerListener);
            bannerView.loadAd(adRequest);
        }
    }

    private AdListener bannerListener = new AdListener() {
        @Override
        public void onAdFailedToLoad(int i) {
            super.onAdFailedToLoad(i);
        }

        @Override
        public void onAdLoaded() {

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);

            if (bannerView.getParent() == null)
                bannerContainer.addView(bannerView, lp);

            super.onAdLoaded();
        }
    };

    private void showBanner() {
        bannerContainer.setVisibility(View.VISIBLE);
    }

    private void hideBanner() {
        bannerContainer.setVisibility(View.GONE);
    }

    private void loadAdmobInterstitial() {
        if (interstitialHasInited) return;
        interstitialHasInited = true;

        if (!enableAdmobInterstitials) {
            return;
        }

        new AdmobInterstitialTask().execute();

    }

    private void showAdmobInterstitial() {
        if (interstitial == null) return;

        if (interstitial.isLoaded()) {
            interstitial.show();
            interstitialHasInited = false;
        }
    }


    private class AdmobInterstitialTask extends AsyncTask<Void, Void, AdRequest> {
        @Override
        protected AdRequest doInBackground(Void... params) {

            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(Setting.TEST_DEVICE).build();

            if (interstitial != null) {
                return adRequest;
            }

            interstitial = new InterstitialAd(AndroidLauncher.this);
            interstitial.setAdUnitId(Setting.ADMOB_INTERSTITIAL);
            interstitial.setAdListener(interstitialListener);

            return adRequest;
        }

        @Override
        protected void onPostExecute(AdRequest adRequest) {
            if (adRequest == null) return;
            interstitial.loadAd(adRequest);
        }
    }

    private AdListener interstitialListener = new AdListener() {
        @Override
        public void onAdFailedToLoad(int i) {
            interstitialHasInited = false;
        }

        @Override
        public void onAdLoaded() {

        }
    };
}
