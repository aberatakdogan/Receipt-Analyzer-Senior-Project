package com.example.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.demo.databinding.ActivityMainBinding;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase db;
    DatabaseReference reference;
    TextBlock textBlock;
    Button button_capture;
    Button button_copy;


    TextView textView_data;
    TextView textView_data2;
    TextView textView_data3;
    Bitmap bitmap;

    ImageView imageView;

    static String updatedMounth;
    ImageView imageView2;
    static String dbDate;

    RadioButton radioButton;
    static int price;

    static String productName = "";
    static String newDate = "";
    private static final int REQUEST_CAMERA_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());


        setContentView(binding.getRoot());

        setContentView(R.layout.activity_main);


        button_capture = findViewById(R.id.button_capture);
        button_copy = findViewById(R.id.button_copy);
        textView_data = findViewById(R.id.text_data);
        imageView = findViewById(R.id.image_view);
        textView_data2 = findViewById(R.id.text_data2);
        imageView2 = findViewById(R.id.image_view2);
        radioButton = findViewById(R.id.radio_button);
        textView_data3 = findViewById(R.id.text_data3);


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);

        }


        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (radioButton.isChecked()) {
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Önce marketi seçiniz!", Toast.LENGTH_SHORT).show();

                }


            }
        });


        button_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveTextToDB();

            }
        });


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri uri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    @SuppressLint("ResourceAsColor")
    private void getTextFromImage(Bitmap bitmap) {
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();


        if (!recognizer.isOperational()) {
            Toast.makeText(MainActivity.this, "Error Occurred!!", Toast.LENGTH_SHORT).show();

        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < textBlockSparseArray.size(); i++) {
                textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
                System.out.println("yazı:" + textBlock.getValue());
            }


            textView_data.setText(stringBuilder.toString());


            textView_data.setTextColor(Color.GRAY);


            //String uid = textBlock.getValue();
            //System.out.println("okutulan yazı: "+uid);


            button_capture.setText("Retake");
            imageView.setVisibility(View.GONE);
            textView_data2.setVisibility(View.GONE);
            imageView2.setVisibility(View.GONE);
            radioButton.setVisibility(View.GONE);
            button_copy.setVisibility(View.VISIBLE);
            button_capture.setTextColor(Color.WHITE);
            textView_data3.setVisibility(View.VISIBLE);

        }

    }

    void saveTextToDB() {

        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();

        String enableDetectButton = binding.buttonCopy.getText().toString();

        Users user = new Users(textBlock.getValue());
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Detected Text");

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < textBlockSparseArray.size(); i++) {

            textBlock = textBlockSparseArray.valueAt(i);
            stringBuilder.append(textBlock.getValue());
            stringBuilder.append("\n");

            /*
            System.out.println("0.index: " + textBlockSparseArray.valueAt(0));
            System.out.println("1.index: " + textBlockSparseArray.valueAt(1));
*/

            reference.push().setValue(textBlock.getValue());
            System.out.println("MEVCUT VERİLER: " + textBlock.getValue());


            //TARİH
            TextBlock tempDate = textBlockSparseArray.valueAt(0);

            String[] desiredDate = tempDate.getValue().split("\n", 2);

            System.out.println("İSTENİLEN1: " + desiredDate[0]);

            String date = desiredDate[0];

            int size = date.length();
            int num = date.indexOf(":");
            newDate = date.substring(num + 2, size);


            // reference = db.getReference("Date");
            // reference.push().setValue(newDate);


            //TARİH


            //FİYAT
            TextBlock tempPrice = textBlockSparseArray.valueAt(0);

            int num2 = tempPrice.getValue().indexOf("*");
            int size2 = tempPrice.getValue().length();
            String newPrice = tempPrice.getValue().substring(num2 + 1, size2);
            System.out.println("Fiyat şudur: " + newPrice);


            int index = newPrice.indexOf(",");
            price = Integer.parseInt(newPrice.substring(0, index));

            // reference = db.getReference("Price");
            // reference.push().setValue(price);


            //FİYAT


            //ÜRÜN İSMİ
            TextBlock tempProduct = textBlockSparseArray.valueAt(0);

            String[] desiredProduct = tempProduct.getValue().split("\n");

            System.out.println("Array 3.indexi: " + desiredProduct[3]);


            productName = desiredProduct[3];
            //int indexProduct = desiredProduct[3].indexOf(" ");
            //String finalProduct = desiredProduct[3].substring(0, indexProduct);
            System.out.println("ProductName: " + productName);

            reference = db.getReference("Product Name");
            reference.push().setValue(productName);
            //ÜRÜN İSMİ


            startActivity(new Intent(MainActivity.this, com.example.demo.GraphActivity.class));


        }


        textView_data.setText(textBlock.getValue());


        Toast.makeText(MainActivity.this, "veritabanı oluşturuldu", Toast.LENGTH_SHORT);


    }


}


