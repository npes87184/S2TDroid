package com.npes87184.g2bdroid;

import java.io.BufferedInputStream;
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
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

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
import android.net.Uri;
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
import cn.pedant.SweetAlert.SweetAlertDialog;

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
	private static final double version = 1.16;
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
		
		if(i > 2) {
			i = 0;
			new Thread(new Runnable() {  //auto check version
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo info = CM.getActiveNetworkInfo();
						if((info != null) && info.isConnected()) {
							BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
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
											versionString = versionString + "\n\n ";
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
		if(prefs.getString(KEY_ENCODING, "0").equals("0")) {
			encoding.setSummary(getResources().getString(R.string.auto_detect));
		} else {
			encoding.setSummary(prefs.getString(KEY_ENCODING, "UTF-8"));
		}
		
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
								String booknameString = Analysis.StoT(bReader.readLine()) + "\r";
								File outFile = new File(prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + booknameString.split(" ")[0]  + ".txt");
								OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outFile), prefs.getString(KEY_OUTPUT_ENCODING, "Unicode"));
								BufferedWriter bw = new BufferedWriter(osw); 
								String line;
								boolean first_write = true;
								while((line = bReader.readLine()) != null) {
									if(first_write) {
										first_write = false;
										bw.write(booknameString + "\r");
									}
									bw.write(Analysis.StoT(line) + "\r");
									bw.newLine();						
								}
								bReader.close();
								bw.close();
								//media rescan for correct show in pc
		        			 	MediaScannerConnection.scanFile(getApplicationContext(), new String[]{prefs.getString(KEY_OUTPUT_FOLDER, APP_DIR)   + booknameString.split(" ")[0]  + ".txt"}, null, null);
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
				BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
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
								versionString = versionString + "\n\n ";
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
				new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
			    .setTitleText(getResources().getString(R.string.done))
			    .show();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				break;
			case 3: //have ota
				SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MainActivity.this);
			    sweetAlertDialog.setTitleText(getResources().getString(R.string.new_version)).setContentText(versionString)
			    .setCancelText("No!")
			    .setConfirmText("Check it!")
			    .showCancelButton(true)
			    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
			    		@Override
			    		public void onClick(SweetAlertDialog sDialog) {
			    			sDialog.dismiss();
			    			Uri uri = Uri.parse("http://home.gamer.com.tw/creationDetail.php?sn=2527256");
			    			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			    			startActivity(intent);
			    		}
			    })
			    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
			    		@Override
			    		public void onClick(SweetAlertDialog sDialog) {
			    			sDialog.dismiss();
			    		}
			    });
   			    sweetAlertDialog.show();
   			    versionString = " ";
				break;
			case 4: // do not have ota
				SweetAlertDialog sweetAlertDialog1 = new SweetAlertDialog(MainActivity.this);
			    sweetAlertDialog1.setTitleText(getResources().getString(R.string.no_version)).setContentText(getResources().getString(R.string.problem))
			    .setCancelText("No!")
			    .setConfirmText("Contact me!")
			    .showCancelButton(true)
			    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
			    		@Override
			    		public void onClick(SweetAlertDialog sDialog) {
			    			sDialog.dismiss();
			    			Uri uri = Uri.parse("http://home.gamer.com.tw/creationDetail.php?sn=2527256");
			    			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			    			startActivity(intent);
			    		}
			    })
			    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
			    		@Override
			    		public void onClick(SweetAlertDialog sDialog) {
			    			sDialog.dismiss();
			    		}
			    });
   			    sweetAlertDialog1.show();
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
