package com.npes87184.s2tdroid.donate;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


/**
 * Created by npes87184 on 2015/5/17.
 */
public class AboutFragment extends Fragment {

    private View v;
    public static AboutFragment newInstance() {
        AboutFragment aboutFragment = new AboutFragment();
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

        Element versionElement = new Element();
        versionElement.setTitle(getString(R.string.version_tag) + getString(R.string.version_name));
        Element authorElement = new Element();
        authorElement.setTitle(getString(R.string.author_tag) + getString(R.string.author_name));

        Element emailElement = new Element();
        emailElement.setTitle(getString(R.string.contact));
        emailElement.setIcon(R.drawable.about_icon_email);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "npes87184@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mail_body));
        emailElement.setIntent(emailIntent);

        Element webElement = new Element();
        webElement.setTitle(getString(R.string.web_tag));
        webElement.setIcon(R.drawable.about_icon_link);
        String url = "https://npes87184.github.io";
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(url));
        webElement.setIntent(webIntent);

        Element playStoreElement = new Element();
        playStoreElement.setTitle(getString(R.string.rate));
        playStoreElement.setIcon(R.drawable.about_icon_google_play);
        Uri uri = Uri.parse("market://details?id=com.npes87184.s2tdroid.donate");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        playStoreElement.setIntent(goToMarket);

        View aboutPage = new AboutPage(getActivity())
                .setDescription(getString(R.string.app_name))
                .setImage(R.drawable.ic_launcher2)
                .addItem(versionElement)
                .addItem(authorElement)
                .addItem(emailElement)
                .addItem(webElement)
                .addItem(playStoreElement)
                .addGitHub("npes87184/S2TDroid")
                .create();
        return aboutPage;
    }
}
