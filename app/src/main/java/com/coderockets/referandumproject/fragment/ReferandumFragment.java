package com.coderockets.referandumproject.fragment;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.aykuttasil.androidbasichelperlib.UiHelper;
import com.coderockets.referandumproject.R;
import com.coderockets.referandumproject.activity.MainActivity;
import com.coderockets.referandumproject.app.Const;
import com.coderockets.referandumproject.db.DbManager;
import com.coderockets.referandumproject.helper.SuperHelper;
import com.coderockets.referandumproject.model.ModelQuestionInformation;
import com.coderockets.referandumproject.rest.RestClient;
import com.coderockets.referandumproject.rest.RestModel.AnswerRequest;
import com.coderockets.referandumproject.util.CustomAnswerPercent;
import com.coderockets.referandumproject.util.CustomButton;
import com.coderockets.referandumproject.util.adapter.CustomSorularAdapter;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Hashtable;

import hugo.weaving.DebugLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_referandum_layout)
public class ReferandumFragment extends BaseFragment {

    @ViewById(R.id.ViewPagerSorular)
    ViewPager mViewPagerSorular;

    @ViewById(R.id.ButtonTrue)
    CustomButton mButtonTrue;

    @ViewById(R.id.ButtonFalse)
    CustomButton mButtonFalse;


    Context mContext;
    MainActivity mActivity;

    CustomSorularAdapter mSorularAdapter;
    Hashtable<Integer, Boolean> modControl = new Hashtable<>();
    Hashtable<String, Boolean> answerControl = new Hashtable<>();
    QuestionFragment mQuestionFragment;

    boolean isAnswered = false;

    @DebugLog
    @AfterViews
    public void ReferandumFragmentInit() {
        this.mContext = getActivity();
        this.mActivity = (MainActivity) getActivity();
        //
        setViewPager();
        setSorular();
    }

    private void setViewPager() {

        mSorularAdapter = new CustomSorularAdapter(getChildFragmentManager());
        mViewPagerSorular.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @DebugLog
            @Override
            public void onPageSelected(int position) {
                setQuestionFragment();
                position = position == 0 ? 0 : position - 1;
                ModelQuestionInformation mqi = getQuestionFragment(position).getQuestion();
                if (!isAnswered && !checkAnswered(mqi)) {

                    sendQuestionAnswer("atla", "s", mqi);
                }
                ControlTempQuestion();
                isAnswered = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPagerSorular.setAdapter(mSorularAdapter);
        mViewPagerSorular.setPageTransformer(true, new BackgroundToForegroundTransformer());
    }

    @DebugLog
    private void ControlTempQuestion() {
        // Eğer modControl hashtable ında 10 ve katlarında bir kayıt yoksa 10 soru çekilir.
        // Bu kontrolün amacı 11. soruda iken 10. soruya dönüldüğünde tekrar soru yüklenmesini engellemek için.
        if (modControl.get(mViewPagerSorular.getCurrentItem()) == null) {
            if (mViewPagerSorular.getCurrentItem() % 10 == 0) {
                // HashTable a ViewPager.getCurrentItem() değerini atıyoruz.
                // Bu sayede ViewPager.getCurrentItem() sayfasında iken null değeri dönmeyecek ve tekrar soru yüklmesi yapılmayacak.
                modControl.put(mViewPagerSorular.getCurrentItem(), true);
                addQuestionsAdapter("10");
            }
        }
    }

    @DebugLog
    private void setSorular() {

        //mSorularAdapter = new CustomSorularAdapter(mActivity.getSupportFragmentManager());
        //mViewPagerAdapter.addView(cardView);
        //mViewPagerAdapter.addView(cardView);

        Logger.i("mSorularAdapter.getCount(): " + mSorularAdapter.getCount());
        if (mSorularAdapter.getCount() == 0) {
            addQuestionsAdapter("20");
        }
    }

    @Click(R.id.ButtonTrue)
    public void ButtonTrueClick() {
        try {
            isAnswered = true;
            //setQuestionFragment();
            showAnswerResult();
            sendQuestionAnswer("evet", "a", mQuestionFragment.getQuestion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Click(R.id.ButtonFalse)
    public void ButtonFalseClick() {
        isAnswered = true;
        //setQuestionFragment();
        showAnswerResult();
        sendQuestionAnswer("hayir", "b", mQuestionFragment.getQuestion());
    }

    private void showAnswerResult() {
        try {

            View viewGroup = mQuestionFragment.getView().findViewById(R.id.SoruText);

            CustomAnswerPercent customAnswerPercent = (CustomAnswerPercent) mQuestionFragment.getView().findViewById(R.id.MyCustomAnswerPercent);
            customAnswerPercent.addAlphaView(viewGroup);
            customAnswerPercent.setAValue(90);
            customAnswerPercent.setBValue(10);

            customAnswerPercent.showResult();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DebugLog
    private void sendQuestionAnswer(String text, String option, ModelQuestionInformation mqi) {

        if (!checkAnswered(mqi)) {

            AnswerRequest answerRequest = AnswerRequest.AnswerRequestInit();
            answerRequest.setOption(option);
            answerRequest.setQuestionId(mqi.getSoruId());
            answerRequest.setText(text);

            Logger.i("AnswerQuestion: " + new Gson().toJson(answerRequest));

            RestClient.getInstance().getApiService().Answer(
                    Const.CLIENT_ID,
                    Const.REFERANDUM_VERSION,
                    SuperHelper.getDeviceId(mContext),
                    answerRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        UiHelper.UiSnackBar.showSimpleSnackBar(getView(), "Cevap gönderildi.", Snackbar.LENGTH_LONG);
                    }, error -> {

                    });
        }
        answerControl.put(mqi.getSoruId(), true);
    }

    private void addQuestionsAdapter(String count) {
        try {

            RestClient restClient = RestClient.getInstance();
            restClient.getApiService()
                    .SoruGetir(
                            Const.CLIENT_ID,
                            Const.REFERANDUM_VERSION,
                            SuperHelper.getDeviceId(mContext),
                            count,
                            SuperHelper.checkUser() ? DbManager.getModelUser().getUserId() : ""
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                //QuestionFragment_ soruFragment = QuestionFragment_.builder().
                                for (ModelQuestionInformation mqi : response.getData().getRows()) {
                                    Logger.i(mqi.getQuestionText());
                                    mSorularAdapter.addFragment(QuestionFragment_.builder().build(), mqi);
                                    //answerControl.put(mqi.getSoruId(), false);
                                }
                            },
                            Throwable::printStackTrace,
                            this::setQuestionFragment);

                    /*
                    .enqueue(new Callback<SoruGetirBaseResponse>() {
                        @Override
                        public void onResponse(Call<SoruGetirBaseResponse> call, Response<SoruGetirBaseResponse> response) {
                            for (ModelQuestionInformation mqi : response.body().getData().getRows()) {
                                Logger.i(mqi.getQuestionText());
                            }
                        }

                        @Override
                        public void onFailure(Call<SoruGetirBaseResponse> call, Throwable t) {

                        }
                    });
                    */

                    /*

                    */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DebugLog
    private void setQuestionFragment() {
        mQuestionFragment = (QuestionFragment) mSorularAdapter.getItem(mViewPagerSorular.getCurrentItem());
        //return mQuestionFragment;
    }

    private QuestionFragment getQuestionFragment(int position) {
        return (QuestionFragment) mSorularAdapter.getItem(position);
    }

    @DebugLog
    private boolean checkAnswered(ModelQuestionInformation mqi) {

        return answerControl.get(mqi.getSoruId()) == null ? false : answerControl.get(mqi.getSoruId());
    }
}
