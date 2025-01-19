package com.example.demo;

import static com.example.demo.MainActivity.newDate;
import static com.example.demo.MainActivity.price;
import static com.example.demo.MainActivity.productName;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demo.databinding.ActivityGraphBinding;
import com.example.demo.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphActivity extends AppCompatActivity {

    static int dbPrice;
    MainActivity mn = new MainActivity();

    private ActivityGraphBinding binding;
    private LineChart chart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        binding = ActivityGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        Button buttonCopy = binding.buttonCopy;
        chart = findViewById(R.id.lineChart);


        // Configure chart appearance and settings
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getDescription().setEnabled(false);

        // Customize the X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // X-axis value formatter

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            private final long startDate = getMillisFromString(newDate);

            @Override
            public String getFormattedValue(float value) {
                if (value == 0) {
                    System.out.println("value 0 değeri");
                    return dateFormat.format(new Date(startDate));
                } else if (Math.abs(value - 1) < 0.01) {
                    long millis = (long) value + startDate;
                    Date date = new Date(millis);
                    System.out.println("value 1 değeri");
                    return dateFormat.format(date);
                } else {
                    System.out.println("value 2 değeri");
                    return "";
                }
            }
        });


        // Y-axis setup and configuration
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawTopYLabelEntry(true);
        yAxis.setGranularity(1); // Minimum interval on the Y-axis
        yAxis.setTextSize(12f);

        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);

        // Y-axis value formatter
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + " ₺";
            }
        });

        List<CustomEntry> entries = new ArrayList<>();

        mn.db = FirebaseDatabase.getInstance();
        mn.reference = mn.db.getReference("Detected Text");
        mn.reference = mn.db.getReference("Date");
        mn.reference.push().setValue(newDate);


        mn.reference = mn.db.getReference("Price");
        mn.reference.push().setValue(price);


        mn.reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //newDate = snapshot.getValue(String.class);

                    Long longVal = snapshot.getValue(Long.class);
                    newDate = longVal.toString();


                    dbPrice = snapshot.getValue(Integer.class);
                    price = dbPrice;

                    entries.add(new CustomEntry(newDate, dbPrice));


                }
                List<Entry> chartEntries = new ArrayList<>();
                for (CustomEntry entry : entries) {
                    chartEntries.add(new Entry(chartEntries.size(), entry.getYValue()));
                }

                LineDataSet dataSet = new LineDataSet(chartEntries, productName);


                dataSet.setCircleRadius(6f);
                dataSet.setValueTextSize(10f);

                // Value formatter for data labels
                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return value + " ₺";
                    }
                });

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet);

                LineData lineData = new LineData(dataSets);

                chart.setData(lineData);
                dataSet.notifyDataSetChanged();
                lineData.notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Veri çekme hatası: " + error.getMessage());
            }
        });


    }


    private long getMillisFromString(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

}