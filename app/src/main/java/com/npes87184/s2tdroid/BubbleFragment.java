package com.npes87184.s2tdroid;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by npes87184 on 2015/8/14.
 */
public class BubbleFragment extends Fragment {

    private View v;

    public static BubbleFragment newInstance(int index) {
        BubbleFragment bubbleFragment = new BubbleFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("home", index);
        bubbleFragment.setArguments(args);

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
                getActivity().startService(new Intent(getActivity(), BubbleService.class));
                getActivity().finish();
            }
        });
        TextView textView = (TextView) v.findViewById(R.id.textView2);
        final float scale = getResources().getDisplayMetrics().density;
        int size = (int)(7 * scale);
        textView.setTextSize(size);

        return v;
    }

}