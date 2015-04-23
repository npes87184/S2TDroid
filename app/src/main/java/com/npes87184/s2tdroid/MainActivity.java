package com.npes87184.s2tdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class MainActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_ENCODING = "encoding";
    private static final String KEY_OUTPUT_ENCODING = "output_encoding";
    private static final String KEY_INPUT_FILE = "input_file";
    private static final String KEY_OUTPUT_FOLDER = "output_folder";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_START = "start";
    private static final String KEY_PATH = "path";
    private static final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/S2TDroid/";
    String[] charsetsToBeTested = {"UTF-8"};

    private static final int EX_FILE_PICKER_RESULT = 0;

    private String versionString = " ";

    boolean isIn = false;
    private Preference encoding;
    private Preference inputPreference;
    private Preference outputPreference;
    private Preference startPreference;
    private Preference outEncodePreference;
    private SharedPreferences prefs;

    String booknameString = "default";

    Object syncToken = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);

        encoding = findPreference(KEY_ENCODING);
        if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
            encoding.setSummary(getResources().getString(R.string.auto_detect));
        } else {
            encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
        }

        outEncodePreference = findPreference(KEY_OUTPUT_ENCODING);
        outEncodePreference.setSummary(prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));
        inputPreference = findPreference(KEY_INPUT_FILE);
        inputPreference.setSummary(prefs.getString(KEY_INPUT_FILE, APP_DIR));
        inputPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                isIn = true;
                Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
                intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
                intent.putExtra(ExFilePicker.ENABLE_QUIT_BUTTON, true);
                intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_FILES);
                intent.putExtra(ExFilePicker.SET_FILTER_LISTED, new String[] { "txt", "lrc" });
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
                Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
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

                            InputStream is = new FileInputStream(inFile);
                            String encodeString = "UTF-8";
                            if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
                                Charset charset = detectCharset(inFile, charsetsToBeTested);
                                if (charset == null) {  //maybe GBK
                                    encodeString = "GBK";
                                } else { //UTF-8
                                    encodeString = "UTF-8";
                                }
                            } else {
                                encodeString = prefs.getString(KEY_ENCODING, "UTF-8");
                            }
                            InputStreamReader isr = new InputStreamReader(is, encodeString);
                            BufferedReader bReader = new BufferedReader(isr);
                            booknameString = Analysis.StoT(bReader.readLine());
                            String firstLine = booknameString;
                            Message msg = new Message();
                            msg.what = 5;
                            mHandler.sendMessage(msg);
                            synchronized (syncToken) {
                                syncToken.wait();
                            }
                            File file = new File(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR));
                            if(!file.exists() || !file.isDirectory()) {
                                file.mkdir();
                            }
                            File outFile = new File(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + booknameString.split(" ")[0]  + "." + file_extension);
                            if(outFile.exists()) {
                                outFile.delete();
                            }
                            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));
                            BufferedWriter bw = new BufferedWriter(osw);
                            String line;
                            boolean first_write = true;
                            while((line = bReader.readLine()) != null) {
                                if(first_write) {
                                    first_write = false;
                                    bw.write(firstLine + "\r");
                                }
                                bw.write(Analysis.StoT(line) + "\r");
                                bw.newLine();
                            }
                            bReader.close();
                            bw.close();
                            //media rescan for correct show in pc
                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR) + booknameString.split(" ")[0] + ".txt"}, null, null);
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
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(getResources().getString(R.string.done))
                            .show();
                    break;
                case 5:
                    AlertDialog.Builder editDialog = new AlertDialog.Builder(MainActivity.this);
                    editDialog.setTitle(getResources().getString(R.string.bookname));

                    final EditText editText = new EditText(MainActivity.this);
                    editText.setText(booknameString);
                    editDialog.setView(editText);
                    editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            booknameString = editText.getText().toString();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.wait), Toast.LENGTH_SHORT).show();
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
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_ENCODING)) {
            if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
                encoding.setSummary(getResources().getString(R.string.auto_detect));
            } else {
                encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
            }
        } else if (key.equals(KEY_INPUT_FILE)) {
            inputPreference.setSummary(sharedPreferences.getString(KEY_INPUT_FILE, APP_DIR));
        } else if (key.equals(KEY_OUTPUT_FOLDER)) {
            outputPreference.setSummary(sharedPreferences.getString(KEY_OUTPUT_FOLDER, APP_DIR));
        } else if (key.equals(KEY_OUTPUT_ENCODING)) {
            outEncodePreference.setSummary(sharedPreferences.getString(KEY_OUTPUT_ENCODING, "Unicode"));
        }
    }

    //add action bar
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar bar;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            root.addView(bar, 0); // insert at top
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);

            root.removeAllViews();

            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);


            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }else{
                height = bar.getHeight();
            }

            content.setPadding(0, height, 0, 0);

            root.addView(content);
            root.addView(bar);
        }
        bar.inflateMenu(R.menu.menu_main);
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.action_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }
                return true;
            }
        });
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
