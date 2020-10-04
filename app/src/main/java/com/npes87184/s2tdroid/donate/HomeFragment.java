package com.npes87184.s2tdroid.donate;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.ContextThemeWrapper;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

import com.npes87184.s2tdroid.donate.model.KeyCollection;
import com.npes87184.s2tdroid.donate.model.Transformer;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class HomeFragment extends PreferenceFragment {

    private static final int REQUEST_CODE_INPUT_CHOOSE = 1;
    private static final int REQUEST_CODE_OUTPUT_CHOOSE = 2;

    int wordNumber = 0;
    String booknameString = "S2TDroid";
    Object syncToken = new Object();
    private Preference inputPreference;
    private Preference outputPreference;
    private Preference startPreference;
    private SharedPreferences prefs;
    private SweetAlertDialog pDialog;
    private float progressNum = 0;
    private float lastProgressNum = 0;
    private ArrayList<Uri> fileList = new ArrayList<Uri>();
    Uri outputDirUri = null;
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
                case 7:
                    pDialog.hide();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.oops_general_err))
                            .show();
                    break;
                case 8:
                    pDialog.hide();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.oops_dir_does_not_exist))
                            .show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private float roundProgress = 0;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    private Preference.OnPreferenceClickListener inputListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            fileList.clear();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.app_name)), REQUEST_CODE_INPUT_CHOOSE);
            return true;
        }
    };

    private Preference.OnPreferenceClickListener outputListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            outputDirUri = null;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.app_name)), REQUEST_CODE_OUTPUT_CHOOSE);
            return true;
        }
    };

    private Preference.OnPreferenceClickListener startListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
            pDialog.show();
            pDialog.setCancelable(false);
            if (fileList.isEmpty()) {
                Message msg = new Message();
                msg.what = 2;
                mHandler.sendMessage(msg);
                return true;
            }
            if (outputDirUri == null && (outputDirUri = readOutputDirUriFromCache()) == null) {
                Message msg = new Message();
                msg.what = 8;
                mHandler.sendMessage(msg);
                return true;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg;
                    try {
                        for (int i = 0; i < fileList.size(); ++i) {
                            Uri uri = fileList.get(i);
                            String encodeString = detectEncode(uri);
                            int totalLine = countLines(uri, encodeString);

                            int TorS = 0; // >0 means t2s
                            if (encodeString.equals("GBK")) {
                                TorS = -100;
                            } else if (encodeString.equals("BIG5")) {
                                TorS = 100;
                            }

                            String name = getFileName(uri);
                            String file_extension = getFileExtension(name);

                            String translatedName;

                            try (InputStream is =
                                         getActivity().getContentResolver().openInputStream(uri)) {
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
                                // fix too large book name
                                if (booknameString.length() > 100) {
                                    booknameString = booknameString.substring(0, 100);
                                }

                                OutputStreamWriter osw = getOutputStreamWriter(booknameString + "." + file_extension);

                                if (osw == null) {
                                    Message errMsg = new Message();
                                    errMsg.what = 7;
                                    mHandler.sendMessage(errMsg);
                                    Thread.currentThread().interrupt();
                                    return;
                                }

                                // doing transform
                                BufferedWriter bw = new BufferedWriter(osw);
                                bw.write(firstLine + "\r");
                                bw.newLine();

                                while ((line = bReader.readLine()) != null) {
                                    progressNum += ((1) / (float) fileList.size()) * (1 / (float) totalLine);
                                    roundProgress = ((float) (int) (progressNum * 100)) / (float) 100;
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

                                if (prefs.getBoolean(KeyCollection.KEY_DELETE_SOURCE, false)) {
                                    deleteSourceFile(uri);
                                }
                            }
                        }
                    } catch (Exception e) {

                    }
                    msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    fileList.clear();
                    outputDirUri = null;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            inputPreference.setSummary(getString(R.string.selection_none_input));
                            outputPreference.setSummary(getString(R.string.selection_none_output));
                        }
                    });
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

        pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.wait));

        inputPreference = findPreference(KeyCollection.KEY_INPUT_FILE);
        inputPreference.setSummary(getString(R.string.selection_none_input));
        inputPreference.setOnPreferenceClickListener(inputListener);

        outputPreference = findPreference(KeyCollection.KEY_OUTPUT_FOLDER);
        outputPreference.setSummary(getString(R.string.selection_none_output));
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_INPUT_CHOOSE) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); ++i) {
                    fileList.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                fileList.add(data.getData());
            }

            if (fileList.size() > 0) {
                inputPreference.setSummary(String.format(getString(R.string.selection_files), fileList.size()));
            } else {
                inputPreference.setSummary(getString(R.string.selection_none_input));
            }
        } else if (requestCode == REQUEST_CODE_OUTPUT_CHOOSE) {
            if (data.getData() != null) {
                outputDirUri = data.getData();
                try {
                    int modeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getActivity().getContentResolver().takePersistableUriPermission(outputDirUri, modeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                prefs.edit().putString(KeyCollection.KEY_OUTPUT_DIR_URI, outputDirUri.toString()).apply();
            }

            if (outputDirUri == null) {
                outputPreference.setSummary(getString(R.string.selection_none_output));
            } else {
                outputPreference.setSummary(getString(R.string.selection_dir));
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private OutputStreamWriter getOutputStreamWriter(String name) {
        if (outputDirUri == null && (outputDirUri = readOutputDirUriFromCache()) == null) {
            return null;
        }

        DocumentFile dir = DocumentFile.fromTreeUri(getActivity(), outputDirUri);
        DocumentFile target = dir.createFile("text/plain", name);

        OutputStreamWriter osw;

        try {
            OutputStream os = getActivity().getContentResolver().openOutputStream(target.getUri());
            osw = new OutputStreamWriter(os);
        } catch (Exception e) {
            return null;
        }

        return osw;
    }

    private void deleteSourceFile(Uri uri) {
        try {
            DocumentsContract.deleteDocument(getActivity().getContentResolver(), uri);
        } catch (Exception e) {

        }
    }

    private int countLines(Uri uri, String encodeString) throws IOException {
        int count = 0;

        try (InputStream inputStream =
                     getActivity().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream), encodeString))) {
            while ((reader.readLine()) != null) {
                ++count;
            }
        }

        return count;
    }

    private String getFileExtension(String fileName) {
        int startIndex = fileName.lastIndexOf(46) + 1;
        int endIndex = fileName.length();
        return fileName.substring(startIndex, endIndex);
    }

    private boolean isFilenameValid(String fileName) {
        File f = new File(fileName);
        try {
            return f.getCanonicalFile().getName().equals(fileName);
        } catch (IOException e) {
            return false;
        }
    }

    private String detectEncode(Uri uri) {
        String encodeString;

        try {
            if (prefs.getString(KeyCollection.KEY_ENCODING, "0").equals("0")) {
                byte[] buf = new byte[4096];
                UniversalDetector detector = new UniversalDetector(null);
                int nread;

                try (InputStream inputStream =
                             getActivity().getContentResolver().openInputStream(uri)) {
                    while ((nread = inputStream.read(buf)) > 0 && !detector.isDone()) {
                        detector.handleData(buf, 0, nread);
                    }
                }

                detector.dataEnd();
                encodeString = detector.getDetectedCharset();
                if (encodeString == null) {
                    encodeString = "Unicode";
                }
                detector.reset();
            } else {
                encodeString = prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8");
            }
        } catch (Exception e) {
            encodeString = prefs.getString(KeyCollection.KEY_ENCODING, "UTF-8");
        }

        return encodeString;
    }

    private Uri readOutputDirUriFromCache() {
        String outputDirUriString = prefs.getString(KeyCollection.KEY_OUTPUT_DIR_URI, null);
        Uri newOutputDirUri = outputDirUriString != null ? Uri.parse(outputDirUriString) : null;
        return newOutputDirUri;
    }
}
