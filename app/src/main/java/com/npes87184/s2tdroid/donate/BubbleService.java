package com.npes87184.s2tdroid.donate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.NotificationCompat;

import com.npes87184.s2tdroid.donate.model.KeyCollection;
import com.npes87184.s2tdroid.donate.model.Transformer;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

/**
 * Created by npes87184 on 2015/8/14.
 */
public class BubbleService extends Service implements FloatingViewListener {

    private static final int NOTIFICATION_ID = 801105;
    private FloatingViewManager mFloatingViewManager;
    private SharedPreferences prefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int bubbleSize;

        if (mFloatingViewManager != null) {
            return START_STICKY;
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString(KeyCollection.KEY_BUBBLE_SIZE, "large").equals("large")) {
            bubbleSize = 96;
        } else {
            bubbleSize = 72;
        }
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);
        final ImageView iconView = (ImageView) inflater.inflate(R.layout.floating_bubble, null, false);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getApplicationContext(), R.style.AppCompatAlertDialogStyle));
                String mode;
                if (prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("0")) {
                    mode = getString(R.string.auto_detect);
                } else {
                    mode = prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "s2t").equals("s2t") ? getString(R.string.s2t) : getString(R.string.t2s);
                }
                builder.setTitle("S2TDroid" + '-' + mode);

                // Set up the input
                final EditText input = new EditText(getApplicationContext());
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
                        if (prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "0").equals("0")) {
                            out = Transformer.isTraditional(out) >= 0 ? Transformer.TtoS(out) : Transformer.StoT(out);
                        } else {
                            out = prefs.getString(KeyCollection.KEY_BUBBLE_MODE, "s2t").equals("s2t") ? Transformer.StoT(out) : Transformer.TtoS(out);
                        }
                        copyToClipboard(out);
                    }
                });
                AlertDialog alert = builder.create();
                // in Lollipop the dialog will be covered by input...
                int size = (int) (150 * getResources().getDisplayMetrics().density);
                alert.getWindow().getAttributes().y = -1 * size;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                } else {
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                }
                alert.show();
            }
        });

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(resize(R.drawable.ic_trash_fixed, bubbleSize));
        mFloatingViewManager.setActionTrashIconImage(resize(R.drawable.ic_trash_action, 200));
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.floatingViewX = (int) convertDpToPixel(512, this);
        options.floatingViewY = (int) convertDpToPixel(512, this);
        options.floatingViewHeight = (int) convertDpToPixel(bubbleSize, this);
        options.floatingViewWidth = (int) convertDpToPixel(bubbleSize, this);
        options.overMargin = (int) (16 * metrics.density);
        mFloatingViewManager.addViewToWindow(iconView, options);

        startForeground(NOTIFICATION_ID, createNotification(this));

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }

    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            mFloatingViewManager = null;
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private Drawable resize(int imageId, int radius) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), imageId);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, radius, radius, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    private Notification createNotification(Context context) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, createNotificationChannel());
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(context.getString(R.string.notification));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
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
        String strChannelID = "com.npes87184.s2tdroid.donate.bubble";
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
