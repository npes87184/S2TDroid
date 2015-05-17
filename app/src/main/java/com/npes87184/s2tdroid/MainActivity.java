package com.npes87184.s2tdroid;

import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.npes87184.s2tdroid.model.ListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView lvLeftMenu;
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        List<String> lvs = new ArrayList<String>(3);

        lvs.add(getString(R.string.home));
        lvs.add(getString(R.string.setting));
        lvs.add(getString(R.string.about1));

        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        //設置菜單列表
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
                                .replace(R.id.container, HomeFragment.newInstance(0))
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                    case 1:
                        // setting
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, HomeFragment.newInstance(0))
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                    case 2:
                        // about
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, AboutFragment.newInstance(0))
                                .commit();
                        mDrawerLayout.closeDrawers();
                        break;
                }
            }
        });

        // init
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance(0))
                .commit();
    }
    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
        lvLeftMenu = (ListView) findViewById(R.id.lv_left_menu);
    }
}
