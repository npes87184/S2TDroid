package com.npes87184.s2tdroid.donate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.npes87184.s2tdroid.donate.model.Transformer;
import com.npes87184.s2tdroid.donate.model.FileUtil;
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class HomeFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_CODE_STORAGE_ACCESS = 0;

    int wordNumber = 0;
    String booknameString = "S2TDroid";
    Object syncToken = new Object();
    private Preference inputPreference;
    private Preference outputPreference;
    private Preference startPreference;
    private SharedPreferences prefs;
    private SweetAlertDialog pDialog;
    private String [] filter = { "txt", "lrc", "trc", "srt", "ssa", "ass", "saa", "ini" };
    private float progressNum = 0;
    private float lastProgressNum = 0;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    pDialog.getProgressHelper().setInstantProgress(1);
                    pDialog.hide();
                    NumberFormat nf = NumberFormat.getInstance();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(getString(R.string.word_count) + nf.format(wordNumber))
                            .setConfirmText("OK")
                            .show();
                    wordNumber = 0;
                    progressNum = 0;
                    lastProgressNum = 0;
                    pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                            .setTitleText(getString(R.string.wait));
                    break;
                case 2:
                    pDialog.hide();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.oops_file_does_not_exist))
                            .show();
                    break;
                case 3:
                    pDialog.show();
                    pDialog.setCancelable(false);
                    break;
                case 4:
                    AlertDialog.Builder editDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppCompatAlertDialogStyle));
                    editDialog.setTitle(getResources().getString(R.string.bookname));

                    pDialog.hide();

                    final EditText editText = new EditText(getActivity());
                    editText.setText(booknameString);
                    editDialog.setView(editText);
                    editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            booknameString = editText.getText().toString();
                            pDialog.show();
                            pDialog.setCancelable(false);
                            synchronized (syncToken) {
                                syncToken.notify();
                            }
                        }
                    });
                    editDialog.show();
                    break;
                case 5:
                    pDialog.getProgressHelper().setInstantProgress(progressNum);
                    break;
                case 6:
                    pDialog.hide();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.illegal_filename))
                            .setConfirmText("OK")
                            .show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private float roundProgress = 0;
    private int fileOrder = 0;

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

    private Preference.OnPreferenceClickListener inputListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Toast.makeText(getActivity(), getString(R.string.choose_tip), Toast.LENGTH_LONG).show();
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT;
            properties.root = new File("/");
            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            properties.offset = new File(prefs.getString(KeyCollection.KEY_PATH, DialogConfigs.DEFAULT_DIR));
            properties.extensions = filter;
            properties.file_order = fileOrder;
            showFilePickerDialog(properties, new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    inputPreference.getEditor().putString(KeyCollection.KEY_INPUT_FILE, files[0] + "/").commit();
                    prefs.edit().putString(KeyCollection.KEY_PATH, new File(files[0]).getParent()).apply();
                }
            });
            return true;
        }
    };

    private Preference.OnPreferenceClickListener outputListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.DIR_SELECT;
            properties.root = new File("/");
            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            properties.offset = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, DialogConfigs.DEFAULT_DIR));
            properties.file_order = fileOrder;
            showFilePickerDialog(properties, new DialogSelectionListener() {
                @Override
                public void onSelectedFilePaths(String[] files) {
                    outputPreference.getEditor().putString(KeyCollection.KEY_OUTPUT_FOLDER, files[0] + "/").commit();
                }
            });
            return true;
        }
    };

    private void showFilePickerDialog(DialogProperties properties, DialogSelectionListener listener) {
        FilePickerDialog dialog = new FilePickerDialog(getActivity(), properties);
        WindowManager.LayoutParams lp = null;
        Window window = dialog.getWindow();
        if (window != null) {
            lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        dialog.setPositiveBtnName(getString(R.string.select));
        dialog.setNegativeBtnName(getString(R.string.cancel));
        dialog.setDialogSelectionListener(listener);
        dialog.show();
        if (lp != null) {
            dialog.getWindow().setAttributes(lp);
        }
    }

    private Preference.OnPreferenceClickListener startListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
            pDialog.show();
            pDialog.setCancelable(false);
            final File testSDcardFolder = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, DialogConfigs.DEFAULT_DIR));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg;
                    if (checkFolder(testSDcardFolder)) {
                        try {
                            File testDirFile = new File(prefs.getString(KeyCollection.KEY_INPUT_FILE, DialogConfigs.DEFAULT_DIR));
                            if (!testDirFile.exists()) {
                                msg = new Message();
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                                Thread.currentThread().interrupt();
                                return;
                            }
                            ArrayList<String> fileList = new ArrayList<String>();
                            prepareFileList(testDirFile, fileList);

                            for (int i = 0; i < fileList.size(); ++i) {
                                File inFile = new File(fileList.get(i));
                                // detect encode
                                String encodeString = detectEncode(fileList.get(i));
                                int totalLine = countLines(fileList.get(i), encodeString);

                                int TorS = 0; // >0 means t2s
                                if (encodeString.equals("GBK")) {
                                    TorS = -100;
                                } else if (encodeString.equals("BIG5")) {
                                    TorS = 100;
                                }

                                String file_extension = getFileExtension(inFile);

                                String name = getFileName(inFile);
                                String translatedName;

                                InputStream is = new FileInputStream(inFile);
                                InputStreamReader isr = new InputStreamReader(is, encodeString);
                                BufferedReader bReader = new BufferedReader(isr);
                                String line;
                                if (prefs.getString(KeyCollection.KEY_MODE, "0").equals("0")) {
                                    line = bReader.readLine();
                                    if (Transformer.isTraditional(line) >= 0) {
                                        booknameString = Transformer.TtoS(line);
                                        translatedName = Transformer.TtoS(name);
                                    } else {
                                        booknameString = Transformer.StoT(line);
                                        translatedName = Transformer.StoT(name);
                                    }
                                } else {
                                    booknameString = prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("s2t") ? Transformer.StoT(bReader.readLine()) : Transformer.TtoS(bReader.readLine());
                                    translatedName = prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("s2t") ? Transformer.StoT(name) : Transformer.TtoS(name);
                                }
                                String firstLine = booknameString;
                                if (prefs.getString(KeyCollection.KEY_FILENAME, getString(R.string.filename_manual_key)).equals(getString(R.string.filename_same_key))) {
                                    booknameString = name;
                                } else if (prefs.getString(KeyCollection.KEY_FILENAME, getString(R.string.filename_manual_key)).equals(getString(R.string.filename_same_transformed_key))) {
                                    booknameString = translatedName;
                                } else {
                                    msg = new Message();
                                    msg.what = 4;
                                    mHandler.sendMessage(msg);
                                    synchronized (syncToken) {
                                        syncToken.wait();
                                    }
                                    if (!isFilenameValid(booknameString)) {
                                        Message filenameNotValidMsg = new Message();
                                        filenameNotValidMsg.what = 6;
                                        mHandler.sendMessage(filenameNotValidMsg);
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                }
                                // fix too large bookname
                                if (booknameString.length() > 100) {
                                    booknameString = booknameString.substring(0, 100);
                                }

                                File file = new File(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, DialogConfigs.DEFAULT_DIR));
                                if (!file.exists() || !file.isDirectory()) {
                                    file.mkdir();
                                }

                                File outFile = getOutFile(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, DialogConfigs.DEFAULT_DIR), booknameString, file_extension);

                                OutputStreamWriter osw = getOutputStreamWriter(outFile);

                                // doing transform
                                BufferedWriter bw = new BufferedWriter(osw);
                                bw.write(firstLine + "\r");
                                bw.newLine();

                                while ((line = bReader.readLine()) != null) {
                                    progressNum += ((1) / (float) fileList.size()) * (1 / (float) totalLine);
                                    roundProgress = ((float) (int)(progressNum*100))/(float) 100;
                                    if (roundProgress - lastProgressNum > 0.01) {
                                        lastProgressNum = roundProgress;
                                        msg = new Message();
                                        msg.what = 5;
                                        mHandler.sendMessage(msg);
                                    }
                                    wordNumber += line.length();
                                    if (prefs.getString(KeyCollection.KEY_MODE, "0").equals("0")) {
                                        if (TorS < 100 && TorS > -100) {
                                            // detect step
                                            TorS += Transformer.isTraditional(line);
                                            if (TorS >= 0) {
                                                bw.write(Transformer.TtoS(line) + "\r");
                                            } else {
                                                bw.write(Transformer.StoT(line) + "\r");
                                            }
                                        } else {
                                            if (TorS > 0) {
                                                bw.write(Transformer.TtoS(line) + "\r");
                                            } else {
                                                bw.write(Transformer.StoT(line) + "\r");
                                            }
                                        }
                                    } else {
                                        if (prefs.getString(KeyCollection.KEY_MODE, "s2t").equals("s2t")) {
                                            bw.write(Transformer.StoT(line) + "\r");
                                        } else {
                                            bw.write(Transformer.TtoS(line) + "\r");
                                        }
                                    }
                                    bw.newLine();
                                }
                                bw.close();
                                osw.close();
                                bReader.close();
                                isr.close();
                                is.close();

                                //media rescan for correctly showing in pc
                                MediaScannerConnection.scanFile(getActivity(), new String[]{outFile.getAbsolutePath()}, null, null);

                                if (prefs.getBoolean(KeyCollection.KEY_DELETE_SOURCE, false)) {
                                    deleteSourceFile(inFile);
                                }
                            }
                        } catch (Exception e) {

                        }
                        msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pDialog.hide();
                                // trigger SAF or in kikat.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    final SweetAlertDialog sdcardDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                                    sdcardDialog.setTitleText(getString(R.string.oops))
                                            .setContentText(getString(R.string.oops_sdcard_detail))
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    triggerStorageAccessFramework();
                                                    sdcardDialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                                    final SweetAlertDialog sdcardDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
                                    sdcardDialog.setTitleText(getString(R.string.oops))
                                            .setContentText(getString(R.string.oops_sdcard_kitkat_detail))
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sdcardDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }
                        });
                    }
                }
            }).start();
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.home);

        prefs = getPreferenceManager().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);

        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.wait));

        fileOrder = Integer.parseInt(prefs.getString(KeyCollection.KEY_FILE_SORT, "0"));

        inputPreference = findPreference(KeyCollection.KEY_INPUT_FILE);
        inputPreference.setSummary(prefs.getString(KeyCollection.KEY_INPUT_FILE, DialogConfigs.DEFAULT_DIR));
        inputPreference.setOnPreferenceClickListener(inputListener);

        outputPreference = findPreference(KeyCollection.KEY_OUTPUT_FOLDER);
        outputPreference.setSummary(prefs.getString(KeyCollection.KEY_OUTPUT_FOLDER, DialogConfigs.DEFAULT_DIR));
        outputPreference.setOnPreferenceClickListener(outputListener);

        startPreference = findPreference(KeyCollection.KEY_START);
        startPreference.setOnPreferenceClickListener(startListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pDialog.dismiss();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = data.getData();

                // Persist URI in shared preference so that you can use it later.
                // Use your own framework here instead of PreferenceUtil.
                prefs.edit().putString(KeyCollection.KEY_SDCARD_URI, treeUri.toString()).apply();

                // Persist access permissions.
                getActivity().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KeyCollection.KEY_INPUT_FILE)) {
            inputPreference.setSummary(sharedPreferences.getString(KeyCollection.KEY_INPUT_FILE, DialogConfigs.DEFAULT_DIR));
        } else if (key.equals(KeyCollection.KEY_OUTPUT_FOLDER)) {
            outputPreference.setSummary(sharedPreferences.getString(KeyCollection.KEY_OUTPUT_FOLDER, DialogConfigs.DEFAULT_DIR));
        }
    }

    private OutputStreamWriter getOutputStreamWriter(File outFile) {
        FileUtil fileUtil = new FileUtil(getActivity());
        OutputStreamWriter osw;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && fileUtil.getExtSdCardFolder(outFile)!=null) {
            // in external sdcard
            Uri uri;
            try {
                uri = Uri.parse(prefs.getString(KeyCollection.KEY_SDCARD_URI, null));
            } catch (Exception e) {
                uri = null;
            }
            DocumentFile targetDocument = fileUtil.getDocumentFile(outFile, uri);
            try {
                OutputStream outStream = getActivity().getApplication().
                        getContentResolver().openOutputStream(targetDocument.getUri());
                osw = new OutputStreamWriter(outStream, prefs.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));
            } catch (Exception e) {
                osw = null;
            }
        } else {
            try {
                osw = new OutputStreamWriter(new FileOutputStream(outFile), prefs.getString(KeyCollection.KEY_OUTPUT_ENCODING, "Unicode"));
            } catch (Exception e) {
                osw = null;
            }
        }
        return osw;
    }

    private File getOutFile(String strPath, String strName, String strExtention) {
        File file = new File(strPath + strName + "." +strExtention);
        for (int i = 1; file.exists(); ++i) {
            file = new File(strPath + strName + "(" + i + ")." + strExtention);
        }
        return file;
    }

    private boolean deleteSourceFile(File inFile) {
        boolean blRet;
        FileUtil fileUtil = new FileUtil(getActivity());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && fileUtil.getExtSdCardFolder(inFile)!=null) {
            // in external sdcard
            Uri uri;
            try {
                uri = Uri.parse(prefs.getString(KeyCollection.KEY_SDCARD_URI, null));
            } catch (Exception e) {
                uri = null;
            }
            DocumentFile targetDocument = fileUtil.getDocumentFile(inFile, uri);
            blRet = targetDocument.delete();
        } else {
            blRet = inFile.delete();
        }

        return blRet;
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
        bReader.close();
        isr.close();
        is.close();
        return count;
    }

    private String getFileExtension(File inFile) {
        int startIndex = inFile.getName().lastIndexOf(46) + 1;
        int endIndex = inFile.getName().length();
        String file_extension = inFile.getName().substring(startIndex, endIndex);
        return file_extension;
    }

    private boolean isFilenameValid(String fileName) {
        File f = new File(fileName);
        try {
            return f.getCanonicalFile().getName().equals(fileName);
        } catch (IOException e) {
            return false;
        }
    }

    private void prepareFileList(File testDirFile, ArrayList<String> fileList) {
        if(testDirFile.isDirectory()) {
            String[] filenames = testDirFile.list();
            for (int i = 0 ; i < filenames.length ; ++i){
                File tempFile = new File(testDirFile.getAbsolutePath() + "/" + filenames[i]);
                String file_extension = getFileExtension(tempFile);
                if(!tempFile.isDirectory() && contain(filter, file_extension)){
                    fileList.add(tempFile.getAbsolutePath());
                }
            }
        } else {
            fileList.add(testDirFile.getAbsolutePath());
        }
    }

    private String detectEncode(String testFileName) {
        String encodeString;

        try {
            if (prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")) {
                FileInputStream fis = new FileInputStream(testFileName);
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
            } else {
                encodeString = prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8");
            }
        } catch (Exception e) {
            encodeString = prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8");
        }

        return encodeString;
    }

    private String getFileName(File inFile) {
        String name = inFile.getName();

        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }

        return name;
    }

    /**
     * Check the folder for writeability. If not, then on Android 5 retrieve Uri for extsdcard via Storage
     * Access Framework.
     *
     * @param folder The folder to be checked.
     * @return true if the check was successful or if SAF has been triggered.
     *         false trigger SAF or in kikat.
     */
    private boolean checkFolder(@NonNull final File folder) {
        FileUtil fileUtil = new FileUtil(getActivity().getApplicationContext());
        Uri uri;
        try {
            uri = Uri.parse(prefs.getString(KeyCollection.KEY_SDCARD_URI, null));
        } catch (Exception e) {
            uri = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && fileUtil.isOnExtSdCard(folder)) {
            // On Android 5, trigger storage access framework.
            if (!fileUtil.isWritableNormalOrSaf(folder, uri)) {
                return false;
            }
            // Only accept after SAF stuff is done.
            return true;
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return !(fileUtil.isOnExtSdCard(folder) && !FileUtil.isWritableNormal(folder));
        } else {
            // some unknown error
            return FileUtil.isWritable(new File(folder, "DummyFile"));
        }
    }

    /**
     * Trigger the storage access framework to access the base folder of the ext sd card.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
    }

}
