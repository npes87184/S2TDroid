package com.npes87184.g2bdroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;
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
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity implements
SharedPreferences.OnSharedPreferenceChangeListener {

	
	private static final String KEY_ENCODING = "encoding";
	private static final String KEY_INPUT_FILE = "input_file";
	private static final String KEY_OUTPUT_FOLDER = "output_folder";
	private static final String KEY_FILE_NAME = "file_name";
	private static final String KEY_START = "start";
	private static final String KEY_PATH = "path";
	private static final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/G2BDroid/";
	
	private static final int EX_FILE_PICKER_RESULT = 0;
	boolean isIn = false;    
	private Preference encoding;
	private Preference inputPreference;
	private Preference outputPreference;
	private Preference startPreference;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		

		prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
				
		encoding = findPreference(KEY_ENCODING);
		encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
		inputPreference = findPreference(KEY_INPUT_FILE);
		inputPreference.setSummary(prefs.getString(KEY_INPUT_FILE, APP_DIR));
		inputPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				isIn = true;
				Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
				intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
				intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_FILES);
				intent.putExtra(ExFilePicker.SET_FILTER_LISTED, new String[] { "txt" });
				intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KEY_PATH, APP_DIR));
				startActivityForResult(intent, EX_FILE_PICKER_RESULT);
				return true;
			}
		});
		
		outputPreference = findPreference(KEY_OUTPUT_FOLDER);
		outputPreference.setSummary(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR));
		outputPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				isIn = false;
				Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
				intent.putExtra(ExFilePicker.SET_START_DIRECTORY, prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR));
				intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_DIRECTORIES);
				intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
				startActivityForResult(intent, EX_FILE_PICKER_RESULT);
				return true;
			}
		});
		
		startPreference = findPreference(KEY_START);
		startPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
        			new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								File inFile = new File(prefs.getString(KEY_INPUT_FILE, APP_DIR));
								InputStream is = new FileInputStream(inFile);
								InputStreamReader isr = new InputStreamReader(is, prefs.getString(KEY_ENCODING, "UTF-8"));
								BufferedReader bReader = new BufferedReader(isr);
								FileWriter fWriter = new FileWriter(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + prefs.getString(KEY_FILE_NAME, "default") + "__done.txt" , false);
								BufferedWriter bw = new BufferedWriter(fWriter); 
								String line;
								while((line = bReader.readLine()) != null) {
									System.out.println(Analysis.StoT(line));
									bw.write(Analysis.StoT(line));
									bw.newLine();
								}
								bReader.close();
								fWriter.flush();
								bw.close();
		        			 	fWriter.close();
		        			 	MediaScannerConnection.scanFile(getApplicationContext(), new String[]{prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + prefs.getString(KEY_FILE_NAME, "default") + "__done.txt"}, null, null);
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
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
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
			encoding.setSummary(sharedPreferences.getString(KEY_ENCODING, "UTF-8"));
		} else if (key.equals(KEY_INPUT_FILE)) {
			inputPreference.setSummary(sharedPreferences.getString(KEY_INPUT_FILE, APP_DIR));
		} else if (key.equals(KEY_OUTPUT_FOLDER)) {
			outputPreference.setSummary(sharedPreferences.getString(KEY_OUTPUT_FOLDER, APP_DIR));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0,0,0,getResources().getString(R.string.about1));
		menu.add(0,1,0,getResources().getString(R.string.exit));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
		case 0:
			View view = View.inflate(MainActivity.this, R.layout.about, null); 
			TextView textView = (TextView) view.findViewById(R.id.textView3);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(getResources().getString(R.string.about1)).setView(view)/*.setIcon(R.drawable.ic_menu_info_details)*/.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub		
				}
			}).show();
			break;
		case 1:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


}
