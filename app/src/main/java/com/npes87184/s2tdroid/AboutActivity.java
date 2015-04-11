package com.npes87184.s2tdroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;

/**
 * Created by npes87184 on 2015/4/11.
 */
public class AboutActivity extends Activity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MaterialListView mListView = (MaterialListView) findViewById(R.id.material_listview);
        mListView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {
                // Do whatever you want here
            }
        });

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView view, int position) {
                if(view.getTag().toString().equals("contact")) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "npes87184@gmail.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.subject));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mail_body));
                    startActivity(emailIntent);
                } else if(view.getTag().toString().equals("code")) {
                    String url = "https://github.com/npes87184/S2TDroid";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {

            }
        });

        SmallImageCard code = new SmallImageCard(this);
        code.setDescription(R.string.code_detail);
        code.setTitle(R.string.code);
        code.setTag("code");
        mListView.add(code);

        SmallImageCard library = new SmallImageCard(this);
        library.setDescription("ExFilePicker, JNovelDownloader and Materiallist");
        library.setTitle("Library");
        library.setTag("library");
        mListView.add(library);

        SmallImageCard contact = new SmallImageCard(this);
        contact.setDescription(getString(R.string.contact_detail));
        contact.setTitle(getString(R.string.contact));
        contact.setDrawable(R.drawable.npes);
        contact.setTag("contact");
        mListView.add(contact);

    }

}
