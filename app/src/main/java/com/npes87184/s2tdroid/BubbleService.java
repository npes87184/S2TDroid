package com.npes87184.s2tdroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.premnirmal.Magnet.IconCallback;
import com.premnirmal.Magnet.Magnet;

/**
 * Created by npes87184 on 2015/8/14.
 */
public class BubbleService extends Service implements IconCallback {

    private static final String TAG = "Magnet";
    private Magnet mMagnet;
    private boolean isOpne = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(R.drawable.ic_launcher);
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
        Log.i(TAG, "onMove(" + x + "," + y + ")");
    }

    @Override
    public void onIconClick(View icon, float iconXPose, float iconYPose) {
        Log.i(TAG, "onIconClick(..)");
        Intent window = new Intent(this, com.npes87184.s2tdroid.BubbleActivity.class);
        window.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(window);
    }

    @Override
    public void onIconDestroyed() {
        Log.i(TAG, "onIconDestroyed()");
        stopSelf();
    }

}
