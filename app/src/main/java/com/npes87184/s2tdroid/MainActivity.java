package com.npes87184.s2tdroid;

import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.npes87184.s2tdroid.model.ListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AppCompatCallback {

    private AppCompatDelegate delegate;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView lvLeftMenu;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //let's create the delegate, passing the activity at both arguments (Activity, AppCompatCallback)
        delegate = AppCompatDelegate.create(this, this);

        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);

        //we use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_main);


        findViews();
        List<String> lvs = new ArrayList<String>(4);

        lvs.add(getString(R.string.home));
        lvs.add(getString(R.string.transformBubble));
        lvs.add(getString(R.string.setting));
        lvs.add(getString(R.string.about1));

        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setHomeButtonEnabled(true);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //setting menu
        adapter = new ListAdapter(this, lvs);
        lvLeftMenu.setAdapter(adapter);
        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
                FragmentManager fragmentManager = getFragmentManager();
                switch (arg2) {
                    case 0:
                        // home
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, HomeFragment.newInstance())
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                    case 1:
                        // bubble
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, BubbleFragment.newInstance())
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                    case 2:
                        // setting
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, SettingFragment.newInstance())
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                    case 3:
                        // about
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, AboutFragment.newInstance())
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                }
            }
        });

        // init
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commit();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        lvLeftMenu = (ListView) findViewById(R.id.lv_left_menu);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        //let's leave this empty, for now
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        // let's leave this empty, for now
    }

    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}
