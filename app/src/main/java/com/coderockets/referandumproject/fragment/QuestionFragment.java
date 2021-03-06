package com.coderockets.referandumproject.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aykuttasil.androidbasichelperlib.UiHelper;
import com.aykuttasil.imageupload.ImageUpload;
import com.aykuttasil.imageupload.rest.models.ImgurUploadResponse;
import com.aykuttasil.imageupload.seed.Imgur;
import com.coderockets.referandumproject.R;
import com.coderockets.referandumproject.app.Const;
import com.coderockets.referandumproject.helper.SuperHelper;
import com.coderockets.referandumproject.model.Event.UpdateLoginEvent;
import com.coderockets.referandumproject.model.ModelQuestionInformation;
import com.coderockets.referandumproject.rest.ApiManager;
import com.coderockets.referandumproject.rest.RestModel.DynamicLinkRequest;
import com.coderockets.referandumproject.rest.RestModel.DynamicLinkResponse;
import com.coderockets.referandumproject.rest.RestModel.FavoriteRequest;
import com.coderockets.referandumproject.rest.RestModel.ReportAbuseRequest;
import com.coderockets.referandumproject.util.AutoFitTextView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import co.mobiwise.materialintro.shape.Focus;
import de.hdodenhof.circleimageview.CircleImageView;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.nightonke.boommenu.ButtonEnum.SimpleCircle;

/**
 * Created by aykutasil on 18.08.2016.
 */
@EFragment(R.layout.question_layout)
public class QuestionFragment extends Fragment {

    @ViewById(R.id.FabFavorite)
    FloatingActionButton mFabFavorite;

    @ViewById(R.id.SoruImage)
    ImageView mImageView_SoruImage;

    @ViewById(R.id.ProfilePicture)
    CircleImageView mProfilePicture;

    @ViewById(R.id.SoruText)
    AutoFitTextView mSoruText;

    @ViewById(R.id.Appbarlayout)
    AppBarLayout mAppbarlayout;

    @ViewById(R.id.Toolbar_Share)
    Toolbar mToolbar_Share;

    @ViewById(R.id.Fab_PreviousQuestion)
    FloatingActionButton mFab_PreviousQuestion;

    @ViewById(R.id.SoruLayout)
    CardView mSoruLayout;

    @ViewById(R.id.QuestionFragmentMainContainer)
    CoordinatorLayout mQuestionFragmentMainContainer;

    @ViewById(R.id.bmb)
    BoomMenuButton boomMenuButton;

    //

    private final String INTRO_KEY_QUESTION_FAVORITE = "question_fav6";

    private Context mContext;
    private ModelQuestionInformation mqi;
    private final String FAVORITE_KEY = "Favorite";
    private boolean mIsFavorite = false;
    CompositeSubscription mCompositeSubscriptions;

    @DebugLog
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsFavorite = savedInstanceState.getBoolean(FAVORITE_KEY, false);
        }
        mCompositeSubscriptions = new CompositeSubscription();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FAVORITE_KEY, mIsFavorite);
    }

    @DebugLog
    @AfterViews
    public void SoruFragmentInstance() {
        this.mContext = getActivity();
        mAppbarlayout.setExpanded(false);
        mqi = getArguments().getParcelable(ModelQuestionInformation.class.getSimpleName());
        setSoru(mqi);
        changeFavoriteFabColor();
        setFavoriteFab();
        registerForContextMenu(mSoruText);
        //setShareToolbar();
        initBoomMenu();

        if (getUserVisibleHint()) {
            SuperHelper.showIntro(
                    getActivity(),
                    mFabFavorite,
                    s -> {

                    },
                    INTRO_KEY_QUESTION_FAVORITE,
                    "Soruyu favorilerinize ekleyerek daha sonra takibini sağlayabilirsiniz",
                    Focus.MINIMUM);
        }
    }

    private void initBoomMenu() {
        boomMenuButton.setBoomEnum(BoomEnum.PARABOLA_3);
        boomMenuButton.setButtonEnum(SimpleCircle);
        boomMenuButton.setPiecePlaceEnum(PiecePlaceEnum.DOT_1);
        boomMenuButton.setButtonPlaceEnum(ButtonPlaceEnum.SC_1);
        boomMenuButton.setInFragment(true); // Fragment içinde kullanıyorsak true değerini giriyoruz.
        boomMenuButton.setBackPressListened(false); // geri tuşuna basıldığında menünü tekrar açılmamasını söylüyoruz
        boomMenuButton.setBoomInWholeScreen(true); // boom menü açıldığında tüm ekranı (actionbar vs dahil) kaplayıp kaplamayacağını belirtiyoruz

        for (int i = 0; i < boomMenuButton.getPiecePlaceEnum().pieceNumber(); i++) {

            SimpleCircleButton.Builder builder = new SimpleCircleButton.Builder()
                    .normalImageRes(R.drawable.ic_facebook)
                    //.normalText("Butter Doesn't fly!")
                    .normalColorRes(R.color.white)
                    //.normalTextColor(R.color.blue)
                    //.subNormalText("Aykut Asil")
                    //.subNormalTextColor(R.color.blue)
                    .rippleEffect(true)
                    .listener(index -> {
                        shareQuestionToFacebook();
                    });
            boomMenuButton.addBuilder(builder);
        }
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    private void changeFavoriteFabColor() {
        if (!mIsFavorite) {
            mFabFavorite.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        } else {
            mFabFavorite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accentColor)));
        }
    }

    @DebugLog
    private void setFavoriteFab() {
        if (SuperHelper.checkUser()) {
            mFabFavorite.show();
        } else {
            mFabFavorite.hide();
        }
    }

    public ModelQuestionInformation getQuestion() {
        return mqi;
    }

    public void setQuestion(ModelQuestionInformation mqi) {
        this.mqi = mqi;
        mIsFavorite = false;
        setSoru(mqi);
        changeFavoriteFabColor();
        setFavoriteFab();
    }

    private void setSoru(ModelQuestionInformation mqi) {

        Uri soruImageUri = Uri.parse(mqi.getQuestionImage());
        if (!mqi.getQuestionImage().contains("loremflickr")
                && !mqi.getQuestionImage().equals("")
                && mqi.getQuestionImage() != null
                && !mqi.getQuestionImage().contains("https://github.com/aykuttasil/www.aykutasil.com/blob/master/images/referandum.jpg?raw=true")) {
            mImageView_SoruImage.setScaleType(ImageView.ScaleType.CENTER);

            Picasso.with(mContext)
                    .load(soruImageUri)
                    .placeholder(R.drawable.loading)
                    .into(mImageView_SoruImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (mImageView_SoruImage != null) {
                                mImageView_SoruImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
        Uri profileImageUri = Uri.parse(mqi.getAskerProfileImg());
        Picasso.with(mContext)
                .load(profileImageUri)
                .placeholder(R.drawable.loading)
                .into(mProfilePicture);
        //mProfilePicture.setImageDrawable(new IconDrawable(mContext, FontAwesomeIcons.fa_github).sizeDp(150).getCurrent());
        String soruText = mqi.getQuestionText();
        if (soruText.length() > 0) {
            //soruText = soruText.substring(0, 1).toUpperCase() + soruText.substring(1).toLowerCase();
        }
        mSoruText.setText(soruText.toLowerCase());
    }

    @DebugLog
    @Click(R.id.FabFavorite)
    public void FabFavoriteClick() {
        mIsFavorite = !mIsFavorite;
        changeFavoriteFabColor();

        FavoriteRequest favoriteRequest = FavoriteRequest.FavoriteRequestInit();
        favoriteRequest.setQuestionId(mqi.getSoruId());

        Logger.i("Favorite Soru Id: " + mqi.getSoruId());
        favoriteRequest.setUnFavorite(!mIsFavorite);

        Subscription subscription = ApiManager.getInstance(mContext).Favorite(favoriteRequest)
                .subscribe(success -> {
                    if (mIsFavorite) {
                        UiHelper.UiSnackBar.showSimpleSnackBar(getView(), "Favorilere Eklendi", Snackbar.LENGTH_SHORT);
                    } else {
                        UiHelper.UiSnackBar.showSimpleSnackBar(getView(), "Favorilerden Çıkarıldı", Snackbar.LENGTH_SHORT);
                    }
                }, error -> {
                    error.printStackTrace();
                    SuperHelper.CrashlyticsLog(error);
                });
        mCompositeSubscriptions.add(subscription);
    }

    @DebugLog
    @Click(R.id.Fab_PreviousQuestion)
    public void Fab_PreviousQuestionClick() {
        if (getParentFragment() != null && getParentFragment() instanceof ReferandumFragment) {
            ((ReferandumFragment) getParentFragment()).skipPreviousQuestion(0);
        }
    }

    public boolean visible = true;

    @DebugLog
    @Click(R.id.ProfilePicture)
    public void ProfilePictureClick() {
        Transition transitionSlide1 = new Slide();
        transitionSlide1.setDuration(300);
        transitionSlide1.addTarget(boomMenuButton);

        TransitionManager.beginDelayedTransition(mQuestionFragmentMainContainer, transitionSlide1);
        boomMenuButton.setVisibility(boomMenuButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @UiThread(delay = 1000)
    @DebugLog
    public void shareQuestionToFacebook() {
        View drawingView = mSoruLayout.getRootView().findViewById(R.id.ParentLayout);
        drawingView.buildDrawingCache(true);
        Bitmap bitmap = drawingView.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false);
        drawingView.destroyDrawingCache();

        MaterialDialog dialog = UiHelper.UiDialog.newInstance(mContext).getProgressDialog("Lütfen bekleyiniz", null);
        dialog.show();

        ImageUpload.create(Imgur.getInstance(mContext, Const.IMGUR_CLIENT_ID), Imgur.resp())
                .upload(bitmap, "Referandum")
                .flatMap(new Func1<ImgurUploadResponse, Observable<DynamicLinkResponse>>() {
                    @Override
                    public Observable<DynamicLinkResponse> call(ImgurUploadResponse resp) {
                        String link = "http://referandum?questionId=" + mqi.getSoruId();
                        DynamicLinkRequest request = DynamicLinkRequest.getDynamicLinkRequest(link, mqi.getQuestionText(), "Referandum'a katılarak sende hemen cevap verebilirsin.", resp.getData().getLink());
                        return ApiManager.getInstance(mContext).DynamicLink(request);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    dialog.dismiss();
                    Logger.i(success.getShortLink());

                    ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse(success.getShortLink()))
                            .build();

                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        ShareDialog.show(this, shareLinkContent);
                        Logger.i("shareQuestion is success");
                    } else {
                        Logger.i("shareQuestion is fatal");
                    }

                }, error -> {
                    dialog.dismiss();
                    error.printStackTrace();
                });
    }

    @DebugLog
    public void showShareButton() {
        Transition transitionSlide1 = new Slide();
        transitionSlide1.setDuration(300);
        transitionSlide1.addTarget(boomMenuButton);

        TransitionManager.beginDelayedTransition(mQuestionFragmentMainContainer, transitionSlide1);
        //boomMenuButton.setVisibility(boomMenuButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        boomMenuButton.setVisibility(View.VISIBLE);

    }

    @DebugLog
    public void hideShareButton() {
        Transition transitionSlide1 = new Slide();
        transitionSlide1.setDuration(300);
        transitionSlide1.addTarget(boomMenuButton);

        TransitionManager.beginDelayedTransition(mQuestionFragmentMainContainer, transitionSlide1);
        boomMenuButton.setVisibility(boomMenuButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(UpdateLoginEvent updateLoginEvent) {
        setFavoriteFab();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == mSoruText.getId()) {
            menu.clearHeader();
            menu.add(0, 0, 1, "Şikayet Et");
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Logger.i("ItemId: " + item.getItemId());
        Logger.i("getUserVisibleHint():" + getUserVisibleHint());

        if (!SuperHelper.checkUser()) {
            return true;
        }

        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case 0: {

                    ReportAbuseRequest reportAbuseRequest = ReportAbuseRequest.ReportAbuseRequestInit();
                    reportAbuseRequest.setQuestionId(mqi.getSoruId());

                    Logger.i("Abuse Soru Id: " + mqi.getSoruId());

                    Subscription subscription = ApiManager.getInstance(mContext).ReportAbuse(reportAbuseRequest)
                            .subscribe(success -> {
                                Logger.i("Abuse Success");
                                UiHelper.UiSnackBar.showSimpleSnackBar(getView(), "Şikayetiniz bize ulaştı. Teşekkür ederiz.", Snackbar.LENGTH_LONG);
                            }, error -> {
                                Logger.e(error, "HATA");
                                SuperHelper.CrashlyticsLog(error);
                                UiHelper.UiSnackBar.showSimpleSnackBar(getView(), error.getMessage(), Snackbar.LENGTH_LONG);
                            });

                    mCompositeSubscriptions.add(subscription);

                    return true;
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    @DebugLog
    public void setPreviousNextButtonUi() {
        if (getParentFragment() != null && getParentFragment() instanceof ReferandumFragment) {
            ReferandumFragment referandumFragment = ((ReferandumFragment) getParentFragment());
            referandumFragment.mSorularAdapter.getRegisteredFragment(referandumFragment.mViewPagerSorular.getCurrentItem())
                    .getView().findViewById(R.id.Fab_PreviousQuestion).setVisibility(View.INVISIBLE);
        }
    }

    @DebugLog
    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @DebugLog
    @Override
    public void onDestroy() {
        if (mCompositeSubscriptions.hasSubscriptions()) {
            mCompositeSubscriptions.clear();
        }
        super.onDestroy();
    }

}
