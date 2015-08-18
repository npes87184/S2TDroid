package com.npes87184.s2tdroid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;

import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.premnirmal.Magnet.IconCallback;
import com.premnirmal.Magnet.Magnet;

/**
 * Created by npes87184 on 2015/8/14.
 */
public class BubbleService extends Service implements IconCallback {

    private static final String TAG = "Magnet";
    private Magnet mMagnet;
    private static final String KEY_MODE = "mode";
    private SharedPreferences prefs;
    float scale;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(getString(R.string.notification))
                        .setContentText(getString(R.string.notification));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

    // The stack builder object will contain an artificial back stack for the
    // started Activity.
    // This ensures that navigating backward from the Activity leads out of
    // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
    // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        startForeground(1, mBuilder.build());

        scale = getResources().getDisplayMetrics().density;
        int size = (int)(60 * scale);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(R.drawable.telegram);
        iconView.setAdjustViewBounds(true);
        iconView.setMaxHeight(size);
        iconView.setMaxWidth(size);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mMagnet = new Magnet.Builder(this)
                .setIconView(iconView)
                .setIconCallback(this)
                .setRemoveIconResId(R.drawable.trash)
                .setRemoveIconShadow(R.drawable.bottom_shadow)
                .setShouldFlingAway(true)
                .setShouldStickToWall(true)
                .setRemoveIconShouldBeResponsive(true)
                .build();
        mMagnet.show();
    }

    @Override
    public void onFlingAway() {
        Log.i(TAG, "onFlingAway");
    }

    @Override
    public void onMove(float x, float y) {

    }

    @Override
    public void onIconClick(View icon, float iconXPose, float iconYPose) {
        Log.i(TAG, "onIconClick(..)");

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle));
        builder.setTitle("S2TDroid"+'-'+
                (prefs.getString(KEY_MODE, "s2t").equals("s2t")?getString(R.string.s2t):getString(R.string.t2s)));

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint(getString(R.string.dialogHint));
        input.setHintTextColor(Color.parseColor("#757575"));
        input.setTextColor(Color.parseColor("#000000"));
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton(getString(R.string.copy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String out = input.getText().toString();
                out = prefs.getString(KEY_MODE, "s2t").equals("s2t") ? Analysis.StoT(out):Analysis.TtoS(out);
                copyToClipboard(out);
            }
        });
        AlertDialog alert = builder.create();
        // in Lollipop the dialog will be covered by input...
        int size = (int)(150 * scale);
        alert.getWindow().getAttributes().y = -1*size;
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    @Override
    public void onIconDestroyed() {
        Log.i(TAG, "onIconDestroyed()");
        stopSelf();
    }
    private void copyToClipboard(String str) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("text label", str);
        clipboard.setPrimaryClip(clip);
    }
}
