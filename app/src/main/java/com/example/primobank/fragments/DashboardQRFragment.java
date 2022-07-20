package com.example.primobank.fragments;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.primobank.R;
import com.example.primobank.activities.EnterMobileNumberActivity;
import com.example.primobank.activities.LogInActivity;
import com.example.primobank.activities.QRCodeScannerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class DashboardQRFragment extends Fragment {

    private ImageView imageViewQRCode;
    private TextView textViewScanQRCode;
    private TextView textViewShareQRCode;

    private FirebaseAuth firebaseAuth;

    public DashboardQRFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_qr, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        imageViewQRCode = (ImageView) view.findViewById(R.id.imageViewQRCode);
        textViewScanQRCode = (TextView) view.findViewById(R.id.textViewScanQRCode);
        textViewShareQRCode = (TextView) view.findViewById(R.id.textViewShareQRCode);

        imageViewQRCode.setDrawingCacheEnabled(true);

        textViewShareQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = imageViewQRCode.getDrawingCache();
                String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "QR Code", null);
                Uri uri = Uri.parse(path);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Share QR Code"));
            }
        });

        textViewScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), QRCodeScannerActivity.class);
                startActivity(intent);
            }
        });

        generateQRCode();

        return view;
    }

    private void generateQRCode() {
        WindowManager manager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(firebaseAuth.getCurrentUser().getPhoneNumber(), null, QRGContents.Type.TEXT, smallerDimension);
        qrgEncoder.setColorBlack(getResources().getColor(R.color.activity_background));
        qrgEncoder.setColorWhite(Color.WHITE);
        imageViewQRCode.setImageBitmap(qrgEncoder.getBitmap());
    }
}