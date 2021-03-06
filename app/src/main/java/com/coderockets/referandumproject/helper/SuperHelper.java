package com.coderockets.referandumproject.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aykuttasil.androidbasichelperlib.UiHelper;
import com.coderockets.referandumproject.R;
import com.coderockets.referandumproject.activity.IntroActivity;
import com.coderockets.referandumproject.db.DbManager;
import com.coderockets.referandumproject.model.ModelUser;
import com.coderockets.referandumproject.rest.ApiManager;
import com.coderockets.referandumproject.rest.RestModel.UserRequest;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.google.firebase.iid.FirebaseInstanceId;
import com.orhanobut.logger.Logger;
import com.slmyldz.random.Randoms;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import hugo.weaving.DebugLog;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by aykutasil on 30.07.2016.
 */
@EBean
public class SuperHelper extends com.aykuttasil.androidbasichelperlib.SuperHelper {

    @DebugLog
    public static boolean checkUser() {
        Logger.i("AccessToken.getCurrentAccessToken(): " + AccessToken.getCurrentAccessToken());
        Logger.i("FirebaseInstanceId.getInstance().getToken(): " + FirebaseInstanceId.getInstance().getToken());

        return AccessToken.getCurrentAccessToken() != null && DbManager.getModelUser() != null && DbManager.getModelUser().getRegId() != null;
    }

    @DebugLog
    public static String setRandomImage(Context context, final ImageView imageView, String signature) {
        String randomUrl = Randoms.imageUrl("png");
        Picasso.with(context)
                .load(randomUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.loading)
                .resize(1280, 680)
                .centerCrop()
                .into(imageView);
        Logger.i("Random Image Url: " + randomUrl);
        return randomUrl;
    }

    @UiThread
    @DebugLog
    public void makeBlur(Context context, View view, ImageView into) {
        Blurry.with(context)
                .async()
                .radius(25)
                .sampling(2)
                .capture(view)
                .into(into);

    }

    @DebugLog
    public static void UpdateUser(Context context) {

        if (checkUser()) {

            String regId = FirebaseInstanceId.getInstance().getToken();

            ModelUser modelUser = DbManager.getModelUser();
            modelUser.setRegId(regId);
            modelUser.save();

            UserRequest userRequest = new UserRequest();
            userRequest.setToken(AccessToken.getCurrentAccessToken().getToken());
            userRequest.setRegId(regId);

            ApiManager.getInstance(context).SaveUser(userRequest).subscribe();
        }
    }
//    /**
//     * İstenilen Activity içerisindeki bir container a fragment replace etmek için kullanılır.
//     * Eğer replace edilmeye çalışılan Fragment daha önce replace edilmiş ise popstack yapılarak yükleme yapılır.
//     *
//     * @param activity
//     * @param fragment
//     * @param containerID
//     * @param isBackStack
//     */
//    public static void ReplaceFragmentBeginTransaction(AppCompatActivity activity, Fragment fragment, int containerID, boolean isBackStack) {
//
//        /*
//        FragmentManager akışını loglamak istiyorsan comment satırını aktifleştir.
//         */
//        //FragmentManager.enableDebugLogging(true);
//        FragmentManager fragmentManager = activity.getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//
//        /*
//        Eğer daha önce yüklenen bir fragment tekrar yüklenmeye çalışılıyor ise replace yapmak yerine popstack ile yükleme yapıyoruz.
//         */
//        Fragment alreadyFragment = fragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
//
//        if (alreadyFragment == null) {
//            //Logger.i("alreadyFragment == null");
//            if (isBackStack) {
//                //Logger.i("isBackStack : true");
//                fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
//            }
//            fragmentTransaction.replace(containerID, fragment, fragment.getClass().getSimpleName());
//            fragmentTransaction.commit();
//        } else {
//            /*
//            for (Fragment frg : fragmentManager.getFragments()) {
//                if (frg != null) {
//                    Logger.i(frg.getTag());
//                }
//            }
//            */
//            //Logger.i("Fragment is visible: " + alreadyFragment.isVisible());
//            if (!alreadyFragment.isVisible()) {
//                //Logger.i(alreadyFragment.getClass().getSimpleName());
//
//                 /*
//                 * {@link fragmentManager.popBackStackImmediate()} kullanarak popstack olup olmadığını yakalayabiliriz.
//                 * Eğer fragment, uygulama ilk açıldığında yüklenen fragment ise popstack false olarak commit edileceği için
//                 * popBackStackImmadiate = false döner.
//                 * Bunun kontorülü yapıyoruz ve ilk yüklenen fragment herhangi bir butona basılarak tekrar yüklenmeye çalışılır ise
//                 * isBackStack değişken kontrolü yaparak replace ediyoruz.
//                 */
//                boolean isPopStack = fragmentManager.popBackStackImmediate(alreadyFragment.getClass().getSimpleName(), 0);
//                if (!isPopStack) {
//                    if (isBackStack) {
//                        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
//                    }
//                    fragmentTransaction.replace(containerID, fragment, fragment.getClass().getSimpleName());
//                    fragmentTransaction.commit();
//                }
//            }
//        }
//
//
//    }

    /**
     * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
     * <p>
     * quality goes from 1 to 100. (Percentage).
     * <p>
     * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
     * depending on where you want to save the image.
     */
    @DebugLog
    public static boolean saveBitmapToFile(File dir, String fileName, Bitmap bm, Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format, quality, fos);

            fos.close();

            return true;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
            CrashlyticsLog(e);
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public static void CrashlyticsLog(Throwable e) {
        ModelUser modelUser = DbManager.getModelUser();
        if (modelUser != null) {
            Crashlytics.setUserIdentifier(modelUser.getUserId());
            Crashlytics.setUserName(modelUser.getName());
        }

        Crashlytics.logException(e);
    }


    @DebugLog
    public static boolean checkShowedIntro(Context context, boolean retry) {


        //Thread t = new Thread(() -> {

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

        if (retry || !SuperHelper.checkUser()) {
            isFirstStart = true;
        }

        if (isFirstStart) {

            Intent i = new Intent(context, IntroActivity.class);
            context.startActivity(i);

            SharedPreferences.Editor e = getPrefs.edit();
            e.putBoolean("firstStart", false);
            e.apply();
        }

        return isFirstStart;
        //});

        //t.start();
    }


    @DebugLog
    public static void showIntro(Activity activity, View view, MaterialIntroListener listener, String id, String text, Focus focusType) {

        WeakReference<View> weakReference = new WeakReference<>(view);

        new Handler().postDelayed(() -> {

            try {
                if (weakReference.get() != null) {

                    new MaterialIntroView.Builder(activity)
                            .enableDotAnimation(true)
                            .setFocusGravity(FocusGravity.CENTER)
                            .setFocusType(focusType)
                            .setDelayMillis(10)
                            .enableFadeAnimation(true)
                            .performClick(true)
                            .setInfoText(text)
                            .setTarget(view)
                            .setUsageId(id)
                            .dismissOnTouch(true)
                            .setListener(listener)
                            .show();
                }
            } catch (Exception e) {
                Logger.e(e, "HATA");
            }
        }, 1000);

    }

    public static void sharePrivateUrl(Activity activity, String title, String url, boolean goParentActivity) {
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title("Paylaş")
                .customView(R.layout.custom_share_private_question_url, true)
                .build();

        EditText editTextPrivateUrl = (EditText) dialog.findViewById(R.id.EditTextPrivateUrl);
        Button buttonPaylas = (Button) dialog.findViewById(R.id.ButtonPaylas);
        Button buttonKopyala = (Button) dialog.findViewById(R.id.ButtonKopyala);

        editTextPrivateUrl.setText(url);

        buttonKopyala.setOnClickListener(v -> {
            setClipboard(activity, url);
            UiHelper.UiToast.showSimpleToast(activity, "Kopyalandı", Toast.LENGTH_LONG);
        });

        buttonPaylas.setOnClickListener(v -> {
            dialog.dismiss();
            if (goParentActivity) {
                NavUtils.navigateUpFromSameTask(activity);
            }
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, title + " - " + url);
            activity.startActivity(Intent.createChooser(sharingIntent, "Paylaş"));
        });
        dialog.show();
    }

    public static void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Kopyalandı", text);
            clipboard.setPrimaryClip(clip);
        }
    }

}
