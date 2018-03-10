package com.npes87184.s2tdroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.npes87184.s2tdroid.model.KeyCollection;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import za.co.riggaroo.materialhelptutorial.TutorialItem;
import za.co.riggaroo.materialhelptutorial.tutorial.MaterialTutorialActivity;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class SettingFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference mode;
    private Preference bubble_mode;
    private SharedPreferences prefs;
    private Preference outEncodePreference;
    private Preference encoding;
    private Preference delete_source;
    private static SettingFragment fragment;
    final private int REQUEST_CODE_PRO = 123;

    public static SettingFragment newInstance() {
        if(fragment==null) {
            fragment = new SettingFragment();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);

        mode = findPreference(KeyCollection.KEY_MODE);
        if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("t2s")) {
            mode.setSummary(getActivity().getString(R.string.t2s));
        } else if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("s2t")) {
            mode.setSummary(getActivity().getString(R.string.s2t));
        } else {
            mode.setSummary(getActivity().getString(R.string.auto_detect));
        }

        bubble_mode = findPreference(KeyCollection.KEY_BUBBLE_MODE);
        if(prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("t2s")) {
            bubble_mode.setSummary(getActivity().getString(R.string.t2s));
        } else if(prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("s2t")) {
            bubble_mode.setSummary(getActivity().getString(R.string.s2t));
        } else {
            bubble_mode.setSummary(getActivity().getString(R.string.auto_detect));
        }

        encoding = findPreference(KeyCollection.KEY_ENCODING);
        encoding.setSummary(prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")?
                getResources().getString(R.string.auto_detect):prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8"));

        outEncodePreference = findPreference(KeyCollection.KEY_OUTPUT_ENCODING);
        outEncodePreference.setSummary(prefs.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));

        delete_source = findPreference(KeyCollection.KEY_DELETE_SOURCE);
        delete_source.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShowProPromotion();
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KeyCollection.KEY_ENCODING)) {
            encoding.setSummary(prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")?
                    getResources().getString(R.string.auto_detect):prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8"));
        } else if (key.equals(KeyCollection.KEY_OUTPUT_ENCODING)) {
            outEncodePreference.setSummary(sharedPreferences.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));
        } else if (key.equals(KeyCollection.KEY_MODE)) {
            if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("t2s")) {
                mode.setSummary(getActivity().getString(R.string.t2s));
            } else if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("s2t")) {
                mode.setSummary(getActivity().getString(R.string.s2t));
            } else {
                mode.setSummary(getActivity().getString(R.string.auto_detect));
            }
        } else if(key.equals(KeyCollection.KEY_BUBBLE_MODE)) {
            if(prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("t2s")) {
                bubble_mode.setSummary(getActivity().getString(R.string.t2s));
            } else if(prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("s2t")) {
                bubble_mode.setSummary(getActivity().getString(R.string.s2t));
            } else {
                bubble_mode.setSummary(getActivity().getString(R.string.auto_detect));
            }
        }
    }

    private void ShowProPromotion() {
        TutorialItem tutorialItem1 = new TutorialItem(this.getString(R.string.pro_batch), this.getString(R.string.pro_batch_desc),
                R.color.blue, R.drawable.batch);
        TutorialItem tutorialItem2 = new TutorialItem(this.getString(R.string.pro_external), this.getString(R.string.pro_external_desc),
                R.color.bluegray, R.drawable.external);
        TutorialItem tutorialItem3 = new TutorialItem(this.getString(R.string.pro_progress), this.getString(R.string.pro_progress_desc),
                R.color.brown, R.drawable.progress);
        TutorialItem tutorialItem4 = new TutorialItem(this.getString(R.string.pro_no_pro), this.getString(R.string.pro_no_pro_desc),
                R.color.teal, R.drawable.no_pro);
        TutorialItem tutorialItem5 = new TutorialItem(this.getString(R.string.pro_and_more), this.getString(R.string.pro_and_more_desc),
                R.color.blue, R.drawable.and_more);
        ArrayList<TutorialItem> tutorialItems = new ArrayList<>();
        tutorialItems.add(tutorialItem1);
        tutorialItems.add(tutorialItem2);
        tutorialItems.add(tutorialItem3);
        tutorialItems.add(tutorialItem4);
        tutorialItems.add(tutorialItem5);

        Intent mainAct = new Intent(this.getActivity(), MaterialTutorialActivity.class);
        mainAct.putParcelableArrayListExtra(MaterialTutorialActivity.MATERIAL_TUTORIAL_ARG_TUTORIAL_ITEMS, tutorialItems);
        getActivity().startActivityForResult(mainAct, REQUEST_CODE_PRO);
    }

}
