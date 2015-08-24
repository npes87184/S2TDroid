package com.npes87184.s2tdroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.npes87184.s2tdroid.model.KeyCollection;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class SettingFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

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

        mode = findPreference(KeyCollection.KEY_MODE);
        mode.setSummary(prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("t2s")?
                getString(R.string.t2s):getString(R.string.s2t));


        encoding = findPreference(KeyCollection.KEY_ENCODING);
        encoding.setSummary(prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")?
                getResources().getString(R.string.auto_detect):prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8"));

        outEncodePreference = findPreference(KeyCollection.KEY_OUTPUT_ENCODING);
        outEncodePreference.setSummary(prefs.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KeyCollection.KEY_ENCODING)) {
            encoding.setSummary(prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")?
                    getResources().getString(R.string.auto_detect):prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8"));
        } else if (key.equals(KeyCollection.KEY_OUTPUT_ENCODING)) {
            outEncodePreference.setSummary(sharedPreferences.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));
        } else if (key.equals(KeyCollection.KEY_MODE)) {
            mode.setSummary(prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("t2s")?
                    getActivity().getString(R.string.t2s):getActivity().getString(R.string.s2t));

        }
    }

}
