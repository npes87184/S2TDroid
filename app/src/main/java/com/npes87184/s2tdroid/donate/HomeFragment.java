package com.npes87184.s2tdroid.donate;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.Toast;

import com.npes87184.s2tdroid.donate.model.Analysis;
import com.npes87184.s2tdroid.donate.model.KeyCollection;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class HomeFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/S2TDroid/";

    private static final int EX_FILE_PICKER_RESULT = 0;

    boolean isIn = false;
    int wordNumber = 0;

    private Preference inputPreference;
    private Preference outputPreference;
    private Preference startPreference;
    private SharedPreferences prefs;
    private SweetAlertDialog pDialog;
    private String [] filter = { "txt", "lrc", "trc", "srt", "ssa", "ass", "saa" };
    private float progressNum = 0;

    String booknameString = "default";

    Object syncToken = new Object();

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    private boolean contain(String[] strings, String s) {
        for(int i=0;i< strings.length;++i) {
            if(strings[i].equals(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);

        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.wait));

        inputPreference = findPreference(KeyCollection.KEY_INPUT_FILE);
        inputPreference.setSummary(prefs.getString(KeyCollection.KEY_INPUT_FILE, APP_DIR));
        inputPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), getString(R.string.choose_tip), Toast.LENGTH_LONG).show();
                isIn = true;
                Intent intent = new Intent(getActivity(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
                intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
                intent.putExtra(ExFilePicker.ENABLE_QUIT_BUTTON, true);
                intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_ALL);
                intent.putExtra(ExFilePicker.SET_FILTER_LISTED, filter);
                intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KeyCollection.KEY_PATH, APP_DIR));
                startActivityForResult(intent, EX_FILE_PICKER_RESULT);
                return true;
            }
        });

        outputPreference = findPreference(KeyCollection.KEY_OUTPUT_FOLDER);
        outputPreference.setSummary(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR));
        outputPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                isIn = false;
                Intent intent = new Intent(getActivity(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
                intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR));
                intent.putExtra(ExFilePicker.ENABLE_QUIT_BUTTON, true);
                intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_DIRECTORIES);
                intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
                startActivityForResult(intent, EX_FILE_PICKER_RESULT);
                return true;
            }
        });

        startPreference = findPreference(KeyCollection.KEY_START);
        startPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File testDirFile = new File(prefs.getString(KeyCollection.KEY_INPUT_FILE, APP_DIR));
                            // detect file exist
                            if(!testDirFile.exists()) {
                                Message msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                                Thread.currentThread().interrupt();
                                return;
                            }
                            ArrayList<String> fileList = new ArrayList<String>();
                            if(testDirFile.isDirectory()) {
                                String[] filenames = testDirFile.list();
                                for (int i = 0 ; i < filenames.length ; ++i){
                                    File tempFile = new File(testDirFile.getAbsolutePath() + "/" + filenames[i]);
                                    int startIndex = tempFile.getName().lastIndexOf(46) + 1;
                                    int endIndex = tempFile.getName().length();
                                    String file_extension = tempFile.getName().substring(startIndex, endIndex);
                                    if(!tempFile.isDirectory() && contain(filter, file_extension)){
                                        fileList.add(tempFile.getAbsolutePath());
                                    }
                                }
                            } else {
                                fileList.add(testDirFile.getAbsolutePath());
                            }
                            for(int i=0;i<fileList.size();++i) {
                                File inFile = new File(fileList.get(i));
                                // detect encode
                                String encodeString;
                                if(prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")) {
                                    FileInputStream fis = new FileInputStream(fileList.get(i));
                                    byte[] buf = new byte[4096];
                                    UniversalDetector detector = new UniversalDetector(null);
                                    int nread;
                                    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                                        detector.handleData(buf, 0, nread);
                                    }
                                    detector.dataEnd();

                                    encodeString = detector.getDetectedCharset();
                                    if (encodeString == null) {
                                        encodeString = "Unicode";
                                    }
                                    detector.reset();
                                    fis.close();
                                }  else {
                                    encodeString = prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8");
                                }

                                int totalLine = countLines(fileList.get(i), encodeString);

                                int TorS = 0; // >0 means t2s
                                if(encodeString.equals("GBK")) {
                                    TorS = -100;
                                } else if(encodeString.equals("BIG5")) {
                                    TorS = 100;
                                }

                                // file extension, ex: .txt, .lrc
                                int startIndex = inFile.getName().lastIndexOf(46) + 1;
                                int endIndex = inFile.getName().length();
                                String file_extension = inFile.getName().substring(startIndex, endIndex);

                                // file name
                                String name = inFile.getName();
                                int pos = name.lastIndexOf(".");
                                if (pos > 0) {
                                    name = name.substring(0, pos);
                                }
                                InputStream is = new FileInputStream(inFile);
                                InputStreamReader isr = new InputStreamReader(is, encodeString);
                                BufferedReader bReader = new BufferedReader(isr);
                                String line;
                                if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("0")) {
                                    line = bReader.readLine();
                                    if(Analysis.isTraditional(line)>=0) {
                                        booknameString = Analysis.TtoS(line);
                                    } else {
                                        booknameString = Analysis.StoT(line);
                                    }
                                } else {
                                    booknameString = prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("s2t")?Analysis.StoT(bReader.readLine()):Analysis.TtoS(bReader.readLine());
                                }
                                String firstLine = booknameString;
                                // fix too large bookname
                                if(booknameString.length()>15) {
                                    booknameString = booknameString.substring(0, 15);
                                }
                                if(prefs.getBoolean(KeyCollection.KEY_SAME_FILENAME, false)) {
                                    booknameString = name;
                                    Message msg = new Message();
                                    msg.what = 3;
                                    mHandler.sendMessage(msg);
                                } else {
                                    Message msg = new Message();
                                    msg.what = 4;
                                    mHandler.sendMessage(msg);
                                    synchronized (syncToken) {
                                        syncToken.wait();
                                    }
                                    booknameString = booknameString.split(" ")[0];
                                }
                                File file = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR));
                                if(!file.exists() || !file.isDirectory()) {
                                    file.mkdir();
                                }

                                // if file exists add -1 in the last
                                File testFile = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR)  + booknameString  + "." + file_extension);
                                File outFile;
                                String scan;
                                if(testFile.exists()) {
                                    scan = "-1.";
                                    outFile = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR)  + booknameString + "-1." + file_extension);
                                } else {
                                    scan = ".";
                                    outFile = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR)  + booknameString  + "." + file_extension);
                                }
                                // doing transform
                                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), prefs.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));
                                BufferedWriter bw = new BufferedWriter(osw);
                                bw.write(firstLine + "\r");
                                bw.newLine();
                                while((line = bReader.readLine()) != null) {
                                    progressNum += ((1) / (float)fileList.size()) * (1 / (float)totalLine);
                                    Message msg = new Message();
                                    msg.what = 5;
                                    mHandler.sendMessage(msg);
                                    if(line.length()==0) {
                                        bw.write("\r");
                                        bw.newLine();
                                        continue;
                                    }
                                    wordNumber += line.length();
                                    if(prefs.getString(KeyCollection.KEY_MODE, "0").equals("0")) {
                                        if(TorS<100 && TorS>-100) {
                                            // detect step
                                            TorS += Analysis.isTraditional(line);
                                            if(TorS>=0) {
                                                bw.write(Analysis.TtoS(line) + "\r");
                                            } else {
                                                bw.write(Analysis.StoT(line) + "\r");
                                            }
                                        } else {
                                            if(TorS>0) {
                                                bw.write(Analysis.TtoS(line) + "\r");
                                            } else {
                                                bw.write(Analysis.StoT(line) + "\r");
                                            }
                                        }
                                    } else {
                                        if(prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("s2t")) {
                                            bw.write(Analysis.StoT(line) + "\r");
                                        } else {
                                            bw.write(Analysis.TtoS(line) + "\r");
                                        }
                                    }
                                    bw.newLine();
                                }
                                is.close();
                                isr.close();
                                bReader.close();
                                bw.close();

                                //media rescan for correctly showing in pc
                                if(scan.equals("-1.")) {
                                    MediaScannerConnection.scanFile(getActivity(), new String[]{prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR) + booknameString + "-1." + file_extension}, null, null);
                                } else {
                                    MediaScannerConnection.scanFile(getActivity(), new String[]{prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR) + booknameString + "." + file_extension}, null, null);
                                }
                            }
                        } catch(Exception e){

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
                    pDialog.getProgressHelper().setInstantProgress(1);
                    NumberFormat nf = NumberFormat.getInstance();
                    pDialog.setTitleText(getString(R.string.word_count) + nf.format(wordNumber))
                            .setConfirmText("OK")
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    break;
                case 2:
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.oops_detail))
                            .show();
                    break;
                case 3:
                    pDialog.show();
                    pDialog.setCancelable(false);
                    break;
                case 4:
                    AlertDialog.Builder editDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
                    editDialog.setTitle(getResources().getString(R.string.bookname));

                    final EditText editText = new EditText(getActivity());
                    editText.setText(booknameString);
                    editDialog.setView(editText);
                    editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            booknameString = editText.getText().toString();
                            pDialog.show();
                            pDialog.setCancelable(false);
                            synchronized(syncToken) {
                                syncToken.notify();
                            }
                        }
                    });
                    editDialog.show();
                    break;
                case 5:
                    pDialog.getProgressHelper().setInstantProgress(progressNum);
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
                        inputPreference.getEditor().putString(KeyCollection.KEY_INPUT_FILE, object.path + object.names.get(0) + "/").commit();
                        prefs.edit().putString(KeyCollection.KEY_PATH, object.path).commit();
                        prefs.edit().putString(KeyCollection.KEY_FILE_NAME, object.names.get(0)).commit();
                    }
                    else {
                        outputPreference.getEditor().putString(KeyCollection.KEY_OUTPUT_FOLDER, object.path + object.names.get(0) + "/").commit();
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
        if (key.equals(KeyCollection.KEY_INPUT_FILE)) {
            inputPreference.setSummary(sharedPreferences.getString(KeyCollection.KEY_INPUT_FILE, APP_DIR));
        } else if (key.equals(KeyCollection.KEY_OUTPUT_FOLDER)) {
            outputPreference.setSummary(sharedPreferences.getString(KeyCollection.KEY_OUTPUT_FOLDER, APP_DIR));
        }
    }

    private int countLines(String filename, String encodeString) throws IOException {
        File inFile = new File(filename);
        InputStream is = new FileInputStream(inFile);
        InputStreamReader isr = new InputStreamReader(is, encodeString);
        BufferedReader bReader = new BufferedReader(isr);
        int count = 0;
        while((bReader.readLine()) != null) {
            ++count;
        }
        is.close();
        isr.close();
        bReader.close();
        return count;
    }

}
