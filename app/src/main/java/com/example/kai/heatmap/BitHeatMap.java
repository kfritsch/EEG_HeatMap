package com.example.kai.heatmap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.apache.commons.math3.complex.Complex;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class BitHeatMap extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bit_heat_map);
        // SET HEATMAP
        ImageView tv1;
        tv1= findViewById(R.id.heat_map);
        Bitmap bmp = genBitMap();
        tv1.setImageBitmap(bmp);

        // SET LINECHART
        LineChart chart = findViewById(R.id.chart);
        // generate sine dummy data
        int[] freqs = {10};
        int fs = 225;
        int secs = 5;
        double[] sines = genSinWaves(freqs, fs, secs);
        // make dft
        Complex[] dft_vals = dft(sines);
        // compute magnitude
        double[] abs_dft_vals = new double[dft_vals.length];
        for (int i=0; i<dft_vals.length; i++){
            abs_dft_vals[i] = dft_vals[i].abs();
        }
        // shift fft to center 0
        double[] shifted_dft = dftShift(abs_dft_vals);
        //
        int offset = (int)shifted_dft.length/2+1;
        int take = shifted_dft.length-offset;
        double freq_steps = Math.floor(fs/2)/(take-1);
        double fr = 0;
        double[] magnitudes = new double[take];
        double [] frequencies = new double [take];
        for (int i=0; i<take; i++){
            magnitudes[i] = shifted_dft[offset+i];
            frequencies[i] = fr;
            fr += freq_steps;
        }
        LineDataSet dataSet = genLineDataSet(frequencies, magnitudes);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
        //dft(a);
    }

    protected Bitmap genBitMap(){
        // You are using RGBA that's why Config is ARGB.8888
        Bitmap bitmap = Bitmap.createBitmap(100, 30, Bitmap.Config.ARGB_8888);
        int[] vector = new int[30*100];
        for (int y=0; y<30; y++){
            for (int x=0; x<100; x++){
                vector[y*100+x] = Color.argb(x*2, y*5,0,0);
            }
        }
        // vector is your int[] of ARGB
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(vector));
        return bitmap;
    }

    protected double[] genSinWaves(int[]freqs, int fs, int sec){
        int N = sec*fs;
        double[] vals = new double[N];
        float y;
        for (int i=0; i<N; i++) {
            y = 0;
            for (int j=0; j<freqs.length; j++){
                y += (float) Math.sin(2*Math.PI*freqs[j]*i/fs);
            }
            vals[i] = y;
        }
        return vals;
    }

    protected LineDataSet genLineDataSet(double[] y){
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<y.length; i++) {
            entries.add(new Entry(i, (float) y[i]));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setDrawCircles(false);
        return dataSet;
    }

    protected LineDataSet genLineDataSet(double[] x, double[] y){
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<y.length; i++) {
            entries.add(new Entry((float) x[i], (float) y[i]));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setDrawCircles(false);
        return dataSet;
    }

    protected Complex[] dft(double []x){
        Complex[] x_dft = new Complex[x.length];
        Complex dft_val;
        Complex addend;
        double real;
        double img;
        double img_exp_val;
        for (int i=0; i<x.length;i++){
            dft_val = new Complex(0,0);
            for (int j=0; j<x.length;j++){
                img_exp_val = -2*Math.PI*i*j/x.length;
                real = Math.cos(img_exp_val);
                img = Math.sin(img_exp_val);
                addend = new Complex(real, img);
                addend = addend.multiply(x[j]);
                dft_val = dft_val.add(addend);
            }
            x_dft[i] = dft_val;
        }
        return x_dft;
    }

    protected double[] dftShift(double []x){
        double[] shifted = new double[x.length];
        int shift = (int) Math.ceil(x.length/2);
        for (int i=0; i<x.length; i++){
            shifted[i] = x[(i+shift)%x.length];
        }
        return shifted;
    }

    protected Complex[] dftShift(Complex []x){
        Complex[] shifted = new Complex[x.length];
        int shift = (int) Math.ceil(x.length/2);
        for (int i=0; i<x.length; i++){
            shifted[i] = x[(i+shift)%x.length];
        }
        return shifted;
    }
}
