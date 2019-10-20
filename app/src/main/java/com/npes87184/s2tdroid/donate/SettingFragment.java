package com.npes87184.s2tdroid.donate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.npes87184.s2tdroid.donate.model.KeyCollection;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class SettingFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference mode;
    private Preference bubble_mode;
    private Preference bubble_size;
    private Preference file_name;
    private Preference file_sort;
    private SharedPreferences prefs;
    private Preference outEncodePreference;
    private Preference encoding;
    private static SettingFragment fragment;

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

        bubble_size = findPreference(KeyCollection.KEY_BUBBLE_SIZE);
        if(prefs.getString(KeyCollection.KEY_BUBBLE_SIZE, "large").equals("large")) {
            bubble_size.setSummary(getActivity().getString(R.string.large));
        } else {
            bubble_size.setSummary(getActivity().getString(R.string.small));
        }

        file_name = findPreference(KeyCollection.KEY_FILENAME);
        if (prefs.getString(KeyCollection.KEY_FILENAME, getString(R.string.filename_manual_key)).equals(getString(R.string.filename_same_key))) {
            file_name.setSummary(getString(R.string.filename_same));
        } else if (prefs.getString(KeyCollection.KEY_FILENAME, getString(R.string.filename_manual_key)).equals(getString(R.string.filename_same_transformed_key))) {
            file_name.setSummary(getString(R.string.filename_same_transformed));
        } else {
            file_name.setSummary(getString(R.string.filename_manual));
        }

        file_sort = findPreference(KeyCollection.KEY_FILE_SORT);
        switch (Integer.parseInt(prefs.getString(KeyCollection.KEY_FILE_SORT, "0"))) {
            default:
            case 0:
                file_sort.setSummary(getActivity().getString(R.string.file_order_name_ascending));
                break;
            case 1:
                file_sort.setSummary(getActivity().getString(R.string.file_order_name_descending));
                break;
            case 2:
                file_sort.setSummary(getActivity().getString(R.string.file_order_date_ascending));
                break;
            case 3:
                file_sort.setSummary(getActivity().getString(R.string.file_order_date_descending));
                break;
        }

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
            if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("t2s")) {
                mode.setSummary(getActivity().getString(R.string.t2s));
            } else if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("s2t")) {
                mode.setSummary(getActivity().getString(R.string.s2t));
            } else {
                mode.setSummary(getActivity().getString(R.string.auto_detect));
            }
        } else if (key.equals(KeyCollection.KEY_BUBBLE_MODE)) {
            if (prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("t2s")) {
                bubble_mode.setSummary(getActivity().getString(R.string.t2s));
            } else if (prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("s2t")) {
                bubble_mode.setSummary(getActivity().getString(R.string.s2t));
            } else {
                bubble_mode.setSummary(getActivity().getString(R.string.auto_detect));
            }
        } else if (key.equals(KeyCollection.KEY_BUBBLE_SIZE)) {
            if(prefs.getString(KeyCollection.KEY_BUBBLE_SIZE, "large").equals("large")) {
                bubble_size.setSummary(getActivity().getString(R.string.large));
            } else {
                bubble_size.setSummary(getActivity().getString(R.string.small));
            }
        } else if (key.equals(KeyCollection.KEY_FILENAME)) {
            if (prefs.getString(KeyCollection.KEY_FILENAME, getString(R.string.filename_manual_key)).equals(getString(R.string.filename_same_key))) {
                file_name.setSummary(getString(R.string.filename_same));
            } else if (prefs.getString(KeyCollection.KEY_FILENAME, getString(R.string.filename_manual_key)).equals(getString(R.string.filename_same_transformed_key))) {
                file_name.setSummary(getString(R.string.filename_same_transformed));
            } else {
                file_name.setSummary(getString(R.string.filename_manual));
            }
        } else if (key.equals(KeyCollection.KEY_FILE_SORT)) {
            switch (Integer.parseInt(prefs.getString(KeyCollection.KEY_FILE_SORT, "0"))) {
                default:
                case 0:
                    file_sort.setSummary(getActivity().getString(R.string.file_order_name_ascending));
                    break;
                case 1:
                    file_sort.setSummary(getActivity().getString(R.string.file_order_name_descending));
                    break;
                case 2:
                    file_sort.setSummary(getActivity().getString(R.string.file_order_date_ascending));
                    break;
                case 3:
                    file_sort.setSummary(getActivity().getString(R.string.file_order_date_descending));
                    break;
            }
        }
    }

}
