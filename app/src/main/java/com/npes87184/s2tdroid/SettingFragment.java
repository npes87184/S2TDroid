package com.npes87184.s2tdroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class SettingFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_ENCODING = "encoding";
    private static final String KEY_OUTPUT_ENCODING = "output_encoding";
    private static final String KEY_MODE = "mode";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Preference mode;
    private SharedPreferences prefs;
    private Preference outEncodePreference;
    private Preference encoding;

    public static SettingFragment newInstance(int sectionNumber) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);

        mode = findPreference(KEY_MODE);
        if(prefs.getString(KEY_MODE, "s2t").equals("t2s")) {
            mode.setSummary(getString(R.string.t2s));
        } else {
            mode.setSummary(getString(R.string.s2t));
        }

        encoding = findPreference(KEY_ENCODING);
        if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
            encoding.setSummary(getResources().getString(R.string.auto_detect));
        } else {
            encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
        }

        outEncodePreference = findPreference(KEY_OUTPUT_ENCODING);
        outEncodePreference.setSummary(prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_ENCODING)) {
            if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
                encoding.setSummary(getResources().getString(R.string.auto_detect));
            } else {
                encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
            }
        } else if (key.equals(KEY_OUTPUT_ENCODING)) {
            outEncodePreference.setSummary(sharedPreferences.getString(KEY_OUTPUT_ENCODING, "Unicode"));
        } else if (key.equals(KEY_MODE)) {
            if(prefs.getString(KEY_MODE, "s2t").equals("t2s")) {
                mode.setSummary(getActivity().getString(R.string.t2s));
            } else {
                mode.setSummary(getActivity().getString(R.string.s2t));
            }
        }
    }

}
