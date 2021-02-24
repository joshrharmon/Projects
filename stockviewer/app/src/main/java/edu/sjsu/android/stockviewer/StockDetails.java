package edu.sjsu.android.stockviewer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class StockDetails extends MainActivity {

    private static final String TIINGO_API_TOKEN = "3aaaecebf5ee64d96d32b74fa8901f2c47e3aa24";

    Button dateSelectButton;
    Button favoriteStockButton;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private StockAdapter stockAdp;

    ArrayList<String> stockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);


        recyclerView = (RecyclerView) findViewById(R.id.stockRecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        getData();

        dateSelectButton = findViewById(R.id.select_date_button);
        favoriteStockButton = findViewById(R.id.favorite_button);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.select_date_button:
                if(getIntent().hasExtra("histStockData")) {
                    Toast.makeText(StockDetails.this,
                            "You're already viewing it, silly!", Toast.LENGTH_SHORT).show();
                } else {
                    new fetchHistStockInfo().execute(stockData.get(0));
                }
                break;
            case R.id.favorite_button:
                stockData = getIntent().getStringArrayListExtra("stockData");
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                if(sharedPreferences.contains(stockData.get(0))) {
                    Toast.makeText(StockDetails.this,
                            "You've already added it!", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(stockData.get(0), stockData.get(0));
                    editor.commit();
                    Toast.makeText(StockDetails.this,
                            "Successfully added to favorites", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getData() {
        Intent intent = getIntent();
        if(intent.hasExtra("histStockData")) {
            HistStockAdapter histStockAdp = new HistStockAdapter
                    (this, getIntent().getStringArrayListExtra("histStockData"));
            recyclerView.setAdapter(histStockAdp);
            stockData = getIntent().getStringArrayListExtra("histStockData");
        }
        else if(intent.hasExtra("stockData")) {
            stockAdp = new StockAdapter(this,
                    getIntent().getStringArrayListExtra("stockData"));
            recyclerView.setAdapter(stockAdp);
            stockData = getIntent().getStringArrayListExtra("stockData");
        }
        else {
            Toast.makeText(this, "Data missing", Toast.LENGTH_SHORT).show();
        }
    }

    class fetchHistStockInfo extends AsyncTask<String, Integer, String> {
        private final ProgressDialog dialog = new ProgressDialog(StockDetails.this);
        String progMSG = "Fetching data...";

        @Override
        protected String doInBackground(String... strings) {
            String rawURL = "https://api.tiingo.com/tiingo/daily/" +
                    strings[0] + "/prices?startDate=2019-01-01" +
                    "&resampleFreq=annually&token=" + TIINGO_API_TOKEN;
            StringBuilder stocks = new StringBuilder();
            try {
                URL url = new URL(rawURL);
                int httpresponse;

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                httpresponse = conn.getResponseCode();
                if(httpresponse == 200) {
                    InputStream input = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(input));
                    String lineReader;
                    while((lineReader = br.readLine()) != null) {
                        stocks.append(lineReader);
                    }
                    publishProgress(1);
                } else {
                    throw new RuntimeException("Request failed, HTTP Code:" + httpresponse);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(stocks.toString());
            return stocks.toString();
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);
            dialog.setMessage(progMSG);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.cancel();
            super.onPostExecute(result);
            String stockSymbol;
            String stockDate;
            String stockHigh;
            String stockLow;
            String stockOpen;
            String stockClose;
            String stockVolume;
            try {
                JSONArray data = new JSONArray(result);
                JSONObject stock_data = data.getJSONObject(0);
                ArrayList<String> stockDataJSON = new ArrayList<String>();

                if(stock_data == null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StockDetails.this);
                    alertDialogBuilder.setMessage("No results found, try again.");
                    alertDialogBuilder.setPositiveButton("Ok", null);
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else {
                    stockSymbol = stockData.get(0);
                    stockDate = "Date: " + stock_data.getString("date");
                    stockHigh = "High: $" + stock_data.getString("high");
                    stockLow = "Low: $" + stock_data.getString("low");
                    stockOpen = "Open; $" + stock_data.getString("open");
                    stockClose = "Close: $" + stock_data.getString("close");
                    stockVolume = "Volume: " + stock_data.getString("volume");

                    Intent intent = new Intent(StockDetails.this, StockDetails.class);
                    Collections.addAll(stockDataJSON, stockSymbol, stockDate, stockClose,
                            stockHigh, stockLow, stockOpen, stockClose, stockVolume);
                    intent.putStringArrayListExtra("histStockData", stockDataJSON);
                    StockDetails.this.startActivity(intent);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }
}