package com.npes87184.g2bdroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

	private static final String APP_NAME = "S2TDroid";
	private static final String APP_ENTER_NUMBER = "enter_number";
	private static final String KEY_ENCODING = "encoding";
	private static final String KEY_OUTPUT_ENCODING = "output_encoding";
	private static final String KEY_INPUT_FILE = "input_file";
	private static final String KEY_OUTPUT_FOLDER = "output_folder";
	private static final String KEY_FILE_NAME = "file_name";
	private static final String KEY_START = "start";
	private static final String KEY_PATH = "path";
	private static final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/G2BDroid/";
	private static final double version = 1.11;
	
	private static final int EX_FILE_PICKER_RESULT = 0;
	
	private String versionString = " ";
	
	boolean isIn = false;    
	private Preference encoding;
	private Preference inputPreference;
	private Preference outputPreference;
	private Preference startPreference;
	private Preference outEncodePreference;
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private static int i;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		

		prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		i = prefs.getInt(APP_ENTER_NUMBER, 0);
		i++;
		
		if(i > 4) {
			i = 0;
			new Thread(new Runnable() {  //auto check version
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo info = CM.getActiveNetworkInfo();
						if((info != null) && info.isConnected()) {
							BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData()));
							String line;
							double temp_version = -1;
							while((line = reader.readLine())!=null) {
								String [] data = line.split(",");
								if(data[0].equals(APP_NAME)) {
									temp_version = Double.parseDouble(data[1]);
									System.out.println(temp_version);
									
									boolean first = true;
									boolean second = true;
									int i = 1;
									for(String aString : data) {
										if(first) {
											first = false;
											continue;
										}
										if(second) {
											versionString = versionString + getResources().getString(R.string.new_version_number) + aString;
											versionString = versionString + "\n ";
											second = false;
											continue;
										}
										versionString = versionString + "      " + i + "." +aString;
										versionString = versionString + "\n ";
										i++;
									}
									break;
								}
							}
							if(temp_version > version) {
								Message msg = new Message();
								msg.what = 3;
								mHandler.sendMessage(msg);
							}
							else {
								//do nothing
							}
						}
						else {
							//do nothing
						}
					} catch (IOException e) {
						System.out.println("IO");
					} catch (URISyntaxException e) {
						System.out.println("URL");
					}
				}
			}).start();
		}
		
		prefs.edit().putInt(APP_ENTER_NUMBER, i).commit();
		
		encoding = findPreference(KEY_ENCODING);
		encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
		outEncodePreference = findPreference(KEY_OUTPUT_ENCODING);
		outEncodePreference.setSummary(prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));
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
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.wait), Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								File inFile = new File(prefs.getString(KEY_INPUT_FILE, APP_DIR));
								InputStream is = new FileInputStream(inFile);
								InputStreamReader isr = new InputStreamReader(is, prefs.getString(KEY_ENCODING, "UTF-8"));
								BufferedReader bReader = new BufferedReader(isr);
								File outFile = new File(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + prefs.getString(KEY_FILE_NAME, "default") + "__" + prefs.getString(KEY_OUTPUT_ENCODING, "Unicode") + ".txt");
								OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));
								BufferedWriter bw = new BufferedWriter(osw); 
								String line;
								while((line = bReader.readLine()) != null) {
								//	System.out.println(Analysis.StoT(line));
									bw.write(Analysis.StoT(line));
									bw.newLine();
								}
								bReader.close();
								bw.close();
		        			 	MediaScannerConnection.scanFile(getApplicationContext(), new String[]{prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + prefs.getString(KEY_FILE_NAME, "default") + "__" + prefs.getString(KEY_OUTPUT_ENCODING, "Unicode") + ".txt"}, null, null);
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
	
	//get latest version code
	public InputStream getUrlData() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.cmlab.csie.ntu.edu.tw/~npes87184/version.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}

	private void check_ota() {
		try {
			ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = CM.getActiveNetworkInfo();
			if((info != null) && info.isConnected()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData()));
				String line;
				double temp_version = -1;
				while((line = reader.readLine())!=null) {
					String [] data = line.split(",");
					if(data[0].equals(APP_NAME)) {
						temp_version = Double.parseDouble(data[1]);
						System.out.println(temp_version);
						
						boolean first = true;
						boolean second = true;
						int i = 1;
						for(String aString : data) {
							if(first) {
								first = false;
								continue;
							}
							if(second) {
								versionString = versionString + getResources().getString(R.string.new_version_number) + aString;
								versionString = versionString + "\n ";
								second = false;
								continue;
							}
							versionString = versionString + "      " + i + "." +aString;
							versionString = versionString + "\n ";
							i++;
						}
						break;
					}
				}
				progressDialog.dismiss();
				if(temp_version > version) {
					Message msg = new Message();
					msg.what = 3;
					mHandler.sendMessage(msg);
				}
				else {
					Message msg = new Message();
					msg.what = 4;
					mHandler.sendMessage(msg);
				}
			}
			else {
				Message msg = new Message();
				msg.what = 2;
				mHandler.sendMessage(msg);
			}
		} catch (IOException e) {
			System.out.println("IO");
		} catch (URISyntaxException e) {
			System.out.println("URL");
		}
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				break;
			case 3: //have ota
				View view = View.inflate(MainActivity.this, R.layout.ota, null);
				TextView versionChange = (TextView) view.findViewById(R.id.textView1);
				versionChange.setText(versionString);
				versionString = " ";
				TextView textView = (TextView) view.findViewById(R.id.textView3);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getResources().getString(R.string.new_version)).setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
			case 4: // do not have ota
				View view1 = View.inflate(MainActivity.this, R.layout.no_ota, null);
				TextView textview = (TextView) view1.findViewById(R.id.textView3);
				textview.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog1 = new AlertDialog.Builder(MainActivity.this);
				dialog1.setTitle(getResources().getString(R.string.no_version)).setView(view1).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
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
		} else if (key.equals(KEY_OUTPUT_ENCODING)) {
			outEncodePreference.setSummary(sharedPreferences.getString(KEY_OUTPUT_ENCODING, "Unicode"));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add(0,0,0,getResources().getString(R.string.ota));
		menu.add(0,1,0,getResources().getString(R.string.about1));
		menu.add(0,2,0,getResources().getString(R.string.exit));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
		case 0:
			progressDialog = ProgressDialog.show(MainActivity.this,getResources().getString(R.string.check_update), getResources().getString(R.string.checking));
			progressDialog.setCancelable(true);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					check_ota();
				}
			}).start();
			break;
		case 1:
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
		case 2:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


}
