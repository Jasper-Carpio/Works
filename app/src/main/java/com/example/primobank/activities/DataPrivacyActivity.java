package com.example.primobank.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.primobank.R;

public class DataPrivacyActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private TextView textViewDataPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_privacy);
        this.getSupportActionBar().hide();

        imageViewBack = (ImageView) findViewById(R.id.imageViewBack);
        textViewDataPrivacy = (TextView) findViewById(R.id.textViewDataPrivacy);

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DataPrivacyActivity.this, EnterMobileNumberActivity.class);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewDataPrivacy.setText(Html.fromHtml(getDataPrivacy(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            textViewDataPrivacy.setText(Html.fromHtml(getDataPrivacy()));
        }
    }

    private String getDataPrivacy() {
        return
                "<html>" +
                        "<body>" +
                            "<h1>Primo Bank, Data Privacy Notice</h1>" +
                            "<p>We, in Primo Bank (“Primo Bank”), value and respect the privacy as well as the security and protection of our customers’ personal data including the personal data of our partners and employees.</p>" +
                            "<p>This Policy sets out to inform our customers and other owners of personal data of our adherence to the privacy principle of: transparency, legitimate purpose, and proportionality, and such other relevant requirements in the collection, processing, storage, transmission and retention of personal data as required by applicable privacy laws that specifically include the Philippine Data Privacy Act of 2012 (“DPA”), and its implementing rules and regulations (“DPA IRR”). This also outlines how GCash collects and processes your personal information during the use of GCash’s platforms such as but not limited to the GCash  app and website.</p>" +
                            "<p>We may update this Privacy Policy to reflect needed changes in our policy to comply with the law. In such cases, we encourage you to check for updates on our Privacy Policy, if notified. This is available oin our website or app for your information and reference.</p>" +
                        "</body>" +
                "</html>";
    }
}