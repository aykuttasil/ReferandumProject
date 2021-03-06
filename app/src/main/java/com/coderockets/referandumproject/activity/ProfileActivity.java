package com.coderockets.referandumproject.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.coderockets.referandumproject.R;
import com.coderockets.referandumproject.db.DbManager;
import com.coderockets.referandumproject.fragment.ProfileMyFavorites_;
import com.coderockets.referandumproject.fragment.ProfileMyQuestions_;
import com.coderockets.referandumproject.model.ModelUser;
import com.coderockets.referandumproject.util.adapter.MyFragmentPagerAdapter;
import com.facebook.login.LoginManager;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import rx.Subscription;

/**
 * Created by aykutasil on 5.09.2016.
 */
@Fullscreen
@EActivity(R.layout.activiy_profile)
public class ProfileActivity extends BaseActivity {

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.ProfileMainLayout)
    ViewGroup mProfileMainLayout;

    @ViewById(R.id.tabs)
    TabLayout mTabs;

    @ViewById(R.id.ProfileViewPager)
    ViewPager mProfileViewPager;

    //

    RxPermissions mRxPermissions;
    List<Subscription> mListSubscription;

    @DebugLog
    @AfterViews
    @Override
    public void initAfterViews() {
        mRxPermissions = new RxPermissions(this);
        mListSubscription = new ArrayList<>();
        //
        setToolbar();
        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @DebugLog
    private void setToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_indigo_300_24dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //getSupportActionBar().setHomeAsUpIndicator(new IconDrawable(this, FontAwesomeIcons.fa_home).actionBarSize().getCurrent());
    }

    @DebugLog
    @Override
    public void updateUi() {

        setupViewPager(mProfileViewPager);
        mTabs.setupWithViewPager(mProfileViewPager);
        setUi();

    }

    @DebugLog
    private void setUi() {
        ModelUser modelUser = DbManager.getModelUser();
        mToolbar.setTitle(modelUser.getName());
        supportInvalidateOptionsMenu();
    }

    private void setupViewPager(ViewPager viewPager) {

        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        //adapter.addFragment(ProfileMe_.builder().build(), "Profil");
        adapter.addFragment(ProfileMyQuestions_.builder().build(), "Sorularım");
        adapter.addFragment(ProfileMyFavorites_.builder().build(), "Favorilerim");

        viewPager.setAdapter(adapter);

    }

    @DebugLog
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profil_menu, menu);
        updateProfileIcon(menu.findItem(R.id.menuProfil));
        return super.onCreateOptionsMenu(menu);
    }

    @DebugLog
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //
        switch (item.getItemId()) {
            case android.R.id.home: {

                NavUtils.navigateUpFromSameTask(this);

                return true;
            }
            case R.id.menuCikisYap: {

                Intent i = new Intent(ProfileActivity.this, IntroActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                LoginManager.getInstance().logOut();

                startActivity(i);

                break;
            }
            case R.id.menuTanitimSayfasi: {
                Intent i = new Intent(ProfileActivity.this, IntroActivity.class);
                startActivity(i);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {

        for (Subscription subscription : mListSubscription) {
            if (!subscription.isUnsubscribed()) {
                //subscription.unsubscribe();
            }
        }

        super.onDestroy();
    }

}
