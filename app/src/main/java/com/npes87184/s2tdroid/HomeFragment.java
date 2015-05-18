package com.npes87184.s2tdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class HomeFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String KEY_ENCODING = "encoding";
    private static final String KEY_MODE = "mode";
    private static final String KEY_OUTPUT_ENCODING = "output_encoding";
    private static final String KEY_INPUT_FILE = "input_file";
    private static final String KEY_OUTPUT_FOLDER = "output_folder";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_START = "start";
    private static final String KEY_PATH = "path";
    private static final String KEY_SAME_FILENAME = "same_filename";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/S2TDroid/";
    String[] charsetsToBeTestedCN = {"UTF-8", "GBK"};
    String[] charsetsToBeTestedTW = {"UTF-8", "BIG5"};

    private static final int EX_FILE_PICKER_RESULT = 0;

    boolean isIn = false;

    private Preference inputPreference;
    private Preference outputPreference;
    private Preference startPreference;
    private SharedPreferences prefs;

    String booknameString = "default";

    Object syncToken = new Object();

    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);


        inputPreference = findPreference(KEY_INPUT_FILE);
        inputPreference.setSummary(prefs.getString(KEY_INPUT_FILE, APP_DIR));
        inputPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                isIn = true;
                Intent intent = new Intent(getActivity(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
                intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
                intent.putExtra(ExFilePicker.ENABLE_QUIT_BUTTON, true);
                intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_FILES);
                intent.putExtra(ExFilePicker.SET_FILTER_LISTED, new String[] { "txt", "lrc", "trc" });
                intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KEY_PATH, APP_DIR));
                startActivityForResult(intent, EX_FILE_PICKER_RESULT);
                return true;
            }
        });

        outputPreference = findPreference(KEY_OUTPUT_FOLDER);
        outputPreference.setSummary(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR));
        outputPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                isIn = false;
                Intent intent = new Intent(getActivity(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
                intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR));
                intent.putExtra(ExFilePicker.ENABLE_QUIT_BUTTON, true);
                intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_DIRECTORIES);
                intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
                startActivityForResult(intent, EX_FILE_PICKER_RESULT);
                return true;
            }
        });

        startPreference = findPreference(KEY_START);
        startPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File inFile = new File(prefs.getString(KEY_INPUT_FILE, APP_DIR));
                            //file extension, ex .txt, .lrc
                            int startIndex = inFile.getName().lastIndexOf(46) + 1;
                            int endIndex = inFile.getName().length();
                            String file_extension = inFile.getName().substring(startIndex, endIndex);

                            //file name
                            String name = inFile.getName();
                            int pos = name.lastIndexOf(".");
                            if (pos > 0) {
                                name = name.substring(0, pos);
                            }

                            InputStream is = new FileInputStream(inFile);
                            String encodeString = "UTF-8";
                            if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
                                if(prefs.getString(KEY_MODE, "s2t").equals("s2t")) {
                                    Charset charset = detectCharset(inFile, charsetsToBeTestedCN);
                                    if (charset == null) {  //maybe Unicode
                                        encodeString = "Unicode";
                                    } else if(charset.name().equals("UTF-8")) { //UTF-8
                                        encodeString = "UTF-8";
                                    } else {
                                        encodeString = "GBK";
                                    }
                                } else {
                                    Charset charset = detectCharset(inFile, charsetsToBeTestedTW);
                                    if (charset == null) {  //maybe Unicode
                                        encodeString = "Unicode";
                                    } else if(charset.name().equals("UTF-8")) { //UTF-8
                                        encodeString = "UTF-8";
                                    } else {
                                        encodeString = "BIG5";
                                    }
                                }
                            } else {
                                encodeString = prefs.getString(KEY_ENCODING, "UTF-8");
                            }
                            InputStreamReader isr = new InputStreamReader(is, encodeString);
                            BufferedReader bReader = new BufferedReader(isr);
                            if(prefs.getString(KEY_MODE, "s2t").equals("s2t")) {
                                booknameString = Analysis.StoT(bReader.readLine());
                            } else {
                                booknameString = Analysis.TtoS(bReader.readLine());
                            }
                            String firstLine = booknameString;
                            if(prefs.getBoolean(KEY_SAME_FILENAME, false)) {
                                booknameString = name;
                                Message msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = 5;
                                mHandler.sendMessage(msg);
                                synchronized (syncToken) {
                                    syncToken.wait();
                                }
                                booknameString = booknameString.split(" ")[0];
                            }
                            File file = new File(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR));
                            if(!file.exists() || !file.isDirectory()) {
                                file.mkdir();
                            }
                            File outFile = new File(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + booknameString  + "." + file_extension);
                            if(outFile.exists()) {
                                outFile.delete();
                            }
                            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));
                            BufferedWriter bw = new BufferedWriter(osw);
                            String line;
                            bw.write(firstLine + "\r");
                            bw.newLine();
                            while((line = bReader.readLine()) != null) {
                                if(prefs.getString(KEY_MODE, "s2t").equals("s2t")) {
                                    bw.write(Analysis.StoT(line) + "\r");
                                } else {
                                    Log.i("info", "look");
                                    bw.write(Analysis.TtoS(line) + "\r");
                                }
                                bw.newLine();
                            }
                            bReader.close();
                            bw.close();
                            //media rescan for correct show in pc
                            MediaScannerConnection.scanFile(getActivity(), new String[]{prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR) + booknameString.split(" ")[0] + ".txt"}, null, null);
                        } catch(Exception e){
                            System.out.println("error");
                        }
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                }).start();

                return true;
            }
        });
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(getResources().getString(R.string.done))
                            .show();
                    break;
                case 2:
                    Toast.makeText(getActivity(), getResources().getString(R.string.wait), Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    AlertDialog.Builder editDialog = new AlertDialog.Builder(getActivity());
                    editDialog.setTitle(getResources().getString(R.string.bookname));

                    final EditText editText = new EditText(getActivity());
                    editText.setText(booknameString);
                    editDialog.setView(editText);
                    editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            booknameString = editText.getText().toString();
                            Toast.makeText(getActivity(), getResources().getString(R.string.wait), Toast.LENGTH_SHORT).show();
                            synchronized(syncToken) {
                                syncToken.notify();
                            }
                        }
                    });
                    editDialog.show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            if (data != null) {
                ExFilePickerParcelObject object = (ExFilePickerParcelObject) data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                if (object.count > 0) {
                    // Here is object contains selected files names and path
                    if(isIn) {
                        inputPreference.getEditor().putString(KEY_INPUT_FILE, object.path + object.names.get(0) + "/").commit();
                        prefs.edit().putString(KEY_PATH, object.path).commit();
                        prefs.edit().putString(KEY_FILE_NAME, object.names.get(0)).commit();
                    }
                    else {
                        outputPreference.getEditor().putString(KEY_OUTPUT_FOLDER, object.path + object.names.get(0) + "/").commit();
                        System.out.println(object.path);

                    }
                }
            }
        }
        getActivity().finish();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_INPUT_FILE)) {
            inputPreference.setSummary(sharedPreferences.getString(KEY_INPUT_FILE, APP_DIR));
        } else if (key.equals(KEY_OUTPUT_FOLDER)) {
            outputPreference.setSummary(sharedPreferences.getString(KEY_OUTPUT_FOLDER, APP_DIR));
        }
    }

    public Charset detectCharset(File f, String[] charsets) {

        Charset charset = null;

        // charsets 是我們定義的 編碼 矩陣, 包括 UTF8, BIG5 etc.
        for (String charsetName : charsets) {
            charset = detectCharset(f, Charset.forName(charsetName));
            if (charset != null) {
                break;
            }
        }
        System.out.printf("\t[Test] Using '%s' encoding!\n", charset);
        return charset;
    }

    private Charset detectCharset(File f, Charset charset) {
        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));

            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();

            byte[] buffer = new byte[512];
            boolean identified = false;
            while ((input.read(buffer) != -1) && (!identified)) {
                identified = identify(buffer, decoder);
            }

            input.close();

            if (identified) {
                return charset;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
}
