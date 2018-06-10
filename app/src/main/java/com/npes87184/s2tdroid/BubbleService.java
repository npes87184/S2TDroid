package com.npes87184.s2tdroid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;

import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.rebound.SpringConfig;
import com.npes87184.s2tdroid.model.Analysis;
import com.npes87184.s2tdroid.model.KeyCollection;
import com.premnirmal.Magnet.IconCallback;
import com.premnirmal.Magnet.Magnet;

/**
 * Created by npes87184 on 2015/8/14.
 */
public class BubbleService extends Service implements IconCallback {

    private Magnet mMagnet;
    private SharedPreferences prefs;
    private float scale;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SpringConfig springConfig = SpringConfig.fromBouncinessAndSpeed(10, 50);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, createNotificationChannel())
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification))
                        .setPriority(NotificationCompat.PRIORITY_MIN);
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
        mMagnet = Magnet.newBuilder(this)
                .setIconView(iconView)
                .setIconCallback(this)
                .setShouldShowRemoveView(true)
                .setShouldStickToWall(true)
                .setRemoveIconShouldBeResponsive(true)
                .setInitialPosition(300, 400)
                .setHideFactor(0.2f)
                .withSpringConfig(springConfig)
                .build();
        mMagnet.show();
    }

    @Override
    public void onFlingAway() {
        mMagnet.destroy();
        stopSelf();
    }

    @Override
    public void onMove(float x, float y) {

    }

    @Override
    public void onIconClick(View icon, float iconXPose, float iconYPose) {

        mMagnet.goToWall();

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppCompatAlertDialogStyle));
        String mode;
        if(prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("0")) {
            mode = getString(R.string.auto_detect);
        } else {
            mode = prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "s2t").equals("s2t")?getString(R.string.s2t):getString(R.string.t2s);
        }
        builder.setTitle("S2TDroid"+'-'+ mode);

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
                if(prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("0")) {
                    out = Analysis.isTraditional(out)>=0 ? Analysis.TtoS(out):Analysis.StoT(out);
                } else {
                    out = prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "s2t").equals("s2t") ? Analysis.StoT(out):Analysis.TtoS(out);
                }
                copyToClipboard(out);
            }
        });
        AlertDialog alert = builder.create();
        // in Lollipop the dialog will be covered by input...
        int size = (int)(150 * scale);
        alert.getWindow().getAttributes().y = -1*size;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        alert.show();
    }

    @Override
    public void onIconDestroyed() {
    }

    private void copyToClipboard(String str) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("text label", str);
        clipboard.setPrimaryClip(clip);
    }

    private String createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "";
        }

        /* Build.VERSION.SDK_INT >= Build.VERSION_CODES.O */
        String strChannelID = "com.npes87184.s2tdroid.bubble";
        String strChannelName = "S2TDroid - Bubble";
        NotificationChannel chan = new NotificationChannel(strChannelID,
                strChannelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.GREEN);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(chan);
        return strChannelID;
    }
}
