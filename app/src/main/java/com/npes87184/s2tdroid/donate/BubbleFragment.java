package com.npes87184.s2tdroid.donate;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by npes87184 on 2015/8/14.
 */
public class BubbleFragment extends Fragment {

    private View v;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    public static BubbleFragment newInstance() {
        BubbleFragment bubbleFragment = new BubbleFragment();
        return bubbleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        v = inflater.inflate(R.layout.bubble, container, false);
        Button startBubble = (Button)v.findViewById(R.id.button);
        startBubble.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(Build.VERSION.SDK_INT >= 23) {
                    // Marshmallow+
                    if (!Settings.canDrawOverlays(getActivity())) {
                        new SweetAlertDialog(getActivity())
                                .setTitleText(getString(R.string.app_name))
                                .setContentText(getString(R.string.floatingPermission))
                                .setConfirmText("OK")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:" + getActivity().getPackageName()));
                                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                                        sDialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        getActivity().startService(new Intent(getActivity(), BubbleService.class));
                        getActivity().finish();
                    }
                } else {
                    getActivity().startService(new Intent(getActivity(), BubbleService.class));
                    getActivity().finish();
                }

            }
        });
        TextView textView = (TextView) v.findViewById(R.id.textView2);
        final float scale = getResources().getDisplayMetrics().density;
        int size = (int)(7 * scale);
        textView.setTextSize(size);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if(Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(getActivity())) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    /* there is a bug in android O, which is canDrawOverlays always return false.
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.floatingPermission))
                            .show();
                    */
                } else {
                    getActivity().startService(new Intent(getActivity(), BubbleService.class));
                }
            }
        }
    }
}