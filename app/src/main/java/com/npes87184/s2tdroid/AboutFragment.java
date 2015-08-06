package com.npes87184.s2tdroid;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class AboutFragment extends Fragment {

    private View v;
    public static AboutFragment newInstance(int index) {
        AboutFragment aboutFragment = new AboutFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("home", index);
        aboutFragment.setArguments(args);

        return aboutFragment;
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
        v = inflater.inflate(R.layout.about, container, false);
        MaterialListView mListView = (MaterialListView) v.findViewById(R.id.material_listview);
        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {
                // Do whatever you want here
            }
        });

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView view, int position) {
                if (view.getTag().toString().equals("contact")) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "npes87184@gmail.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.subject));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mail_body));
                    startActivity(emailIntent);
                } else if (view.getTag().toString().equals("code")) {
                    String url = "https://github.com/npes87184/S2TDroid";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else if (view.getTag().toString().equals("rate")) {
                    String url = "https://play.google.com/store/apps/details?id=com.npes87184.s2tdroid";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } else if (view.getTag().toString().equals("library")) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Licence");

                    WebView wv = new WebView(getActivity());
                    wv.loadUrl("file:///android_asset/licence.html");
                    wv.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);

                            return true;
                        }
                    });

                    alert.setView(wv);
                    alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {

            }
        });

        SmallImageCard code = new SmallImageCard(getActivity());
        code.setDescription(R.string.code_detail);
        code.setTitle(R.string.code);
        code.setTag("code");
        mListView.add(code);

        SmallImageCard rate = new SmallImageCard(getActivity());
        rate.setDescription(R.string.rate_detail);
        rate.setTitle(R.string.rate);
        rate.setTag("rate");
        mListView.add(rate);

        SmallImageCard library = new SmallImageCard(getActivity());
        library.setDescription("sweet-alert-dialog, ExFilePicker, JNovelDownloader and Materiallist");
        library.setTitle("Library");
        library.setTag("library");
        mListView.add(library);

        SmallImageCard contact = new SmallImageCard(getActivity());
        contact.setDescription(getString(R.string.contact_detail));
        contact.setTitle(getString(R.string.contact));
        contact.setDrawable(R.drawable.npes);
        contact.setTag("contact");
        mListView.add(contact);

        return v;
    }
}
