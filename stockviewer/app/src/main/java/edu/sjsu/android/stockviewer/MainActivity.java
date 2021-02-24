/*
    Code Attributions:
    - Some Async guidance: https://www.tutorialspoint.com/android-asynctask-example-and-explanation
    - For 2-line auto-complete: https://stackoverflow.com/questions/16062569/how-to-construct-and-display-the-info-in-simple-list-item-2
    - Shared Preferences: https://www.youtube.com/watch?v=fJEFZ6EOM9o
    - Get current date: http://www.java2s.com/example/android/java.util/get-current-date-in-yyyymmdd-format-using-simpledateformat.html
    - Removing all items from ReyclerView: https://stackoverflow.com/questions/29978695/remove-all-items-from-recyclerview
    - Setting timer: https://stackoverflow.com/questions/4044726/how-to-set-a-timer-in-java
    - Checking switch state: https://stackoverflow.com/questions/11278507/android-widget-switch-on-off-event-listener
*/

package edu.sjsu.android.stockviewer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "favoriteStocks";
    private static final int AUTOCOMPLETE_MIN_CHARS = 2;

    /* Original API token */
    public static final String TIINGO_API_TOKEN = "3aaaecebf5ee64d96d32b74fa8901f2c47e3aa24";

    /* New API token due to need for more hourly requests */
    // public static final String TIINGO_API_TOKEN = "97c908e50279c366ee547dc0a5e2527857bb9d5f";

    private AutoCompleteTextView autoText;
    private ArrayList<List<String>> stockDetails = new ArrayList<>();

    private Button refresh;
    private Switch autoSwitch;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public static final SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AtomicBoolean autoRefreshChecked = new AtomicBoolean(false);

        recyclerView = (RecyclerView) findViewById(R.id.favRecycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        refresh = findViewById(R.id.refreshButton);
        autoSwitch = findViewById(R.id.autoRefreshSwitch);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(autoRefreshChecked.get()) {
                    clearRefresh();
                    refreshFavs();
                }
            }
        }, 10*1000, 10*1000);

        autoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                autoRefreshChecked.set(true);
            } else {
                autoRefreshChecked.set(false);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if(sharedPreferences.getAll().size() != 0) {
            for(String s: sharedPreferences.getAll().keySet()) {
                new fetchStockFavorites().execute(s);
            }
        }

        autoText = findViewById(R.id.autoCompleteTextView);
        autoText.setThreshold(AUTOCOMPLETE_MIN_CHARS);
        autoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new fetchStockNames().execute(autoText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.getQuote:
                if(autoText.getText().length() == 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage("Please input a stock name.");
                    alertDialogBuilder.setPositiveButton("Ok", null);
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else {
                    new fetchStockInfo().execute(autoText.getText().toString());
                }
            case R.id.clearButton:
                autoText.setText("");
                break;
            case R.id.refreshButton:
                clearRefresh();
                Toast.makeText(this,
                        "Refreshing favorite stocks...", Toast.LENGTH_SHORT).show();
                refreshFavs();
                break;
            case R.id.clearFavsButton:
                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Once favorites are cleared, they cannot be recovered")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences sharedPreferences =
                                        getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                sharedPreferences.edit().clear().commit();
                                clearRefresh();
                                refreshFavs();

                                FavStockAdapter favStockAdp = new FavStockAdapter (MainActivity.this, stockDetails);
                                recyclerView.setAdapter(favStockAdp);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;
        }
    }

    class fetchStockInfo extends AsyncTask<String, Integer, String> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        String progMSG = "Fetching data...";

        @Override
        protected String doInBackground(String... strings) {
            String rawURL = "https://api.tiingo.com/iex/?tickers=" +
                    strings[0] + "&token=" + TIINGO_API_TOKEN;
            String infoURL = "https://api.tiingo.com/tiingo/daily/" + strings[0]
                    + "?token=" + TIINGO_API_TOKEN;
            StringBuilder stocks = new StringBuilder();
            try {
                URL url = null;
                URL url1 = new URL(rawURL);
                URL url2 = new URL(infoURL);
                int httpresponse;

                for(int i = 0; i < 2; i++) {
                    switch(i) {
                        case 0:
                            url = url1;
                            break;
                        case 1:
                            url = url2;
                            break;
                    }

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
                        return null;
                    }
                    if(i != 1) {
                        stocks.append(",");
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String mergeJSON = stocks.toString().replaceAll("\\[|\\]|\\{|\\}", "");
            mergeJSON = "[{" + mergeJSON + "}]";

            return mergeJSON;
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
            String stockTimestamp;
            String stockName;
            String stockDesc;
            String stockSymbol;
            String stockLast;
            String stockPrevClose;
            String stockOpenPrice;
            String stockHighPrice;
            String stockLowPrice;
            String stockMidPrice;
            String stockVol;
            String stockBidSize;
            String stockBidPrice;
            String stockAskSize;
            String stockAskPrice;
            try {
                if(result == null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setMessage("Stock name gave no results, try again.");
                    alertDialogBuilder.setPositiveButton("Ok", null);
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else {
                    JSONArray data = new JSONArray(result);
                    JSONObject stock_data = data.getJSONObject(0);
                    ArrayList<String> stockDataJSON = new ArrayList<String>();
                    stockName = stock_data.getString("name");
                    stockDesc = stock_data.getString("description");
                    stockSymbol = stock_data.getString("ticker");
                    stockTimestamp = "Timestamp: " + stock_data.getString("timestamp");
                    stockLast = "Last stock: $" + stock_data.getString("last");
                    stockPrevClose = "Previous close: " + stock_data.getString("prevClose");
                    stockOpenPrice = "Open Price: $" + stock_data.getString("open");
                    stockHighPrice = "High Price; $" + stock_data.getString("high");
                    stockLowPrice = "Low Price: $" + stock_data.getString("low");
                    if(stock_data.getString("mid").equals("null")) {
                        stockMidPrice = "Mid Price: -";
                    } else {
                        stockMidPrice = "Mid Price: $" + stock_data.getString("mid");
                    }
                    stockVol = "Volume: " + stock_data.getString("volume");
                    if(stock_data.getString("bidSize").equals("null")) {
                        stockBidSize = "Bid Size: -";
                    } else {
                        stockBidSize = "Bid Size: " + stock_data.getString("bidSize");
                    }
                    if(stock_data.getString("bidPrice").equals("null")) {
                        stockBidPrice = "Bid Price: -";
                    } else {
                        stockBidPrice = "Bid Price: $" + stock_data.getString("bidPrice");
                    }
                    if(stock_data.getString("askSize").equals("null")) {
                        stockAskSize = "Ask Size: -";
                    } else {
                        stockAskSize = "Ask Size: " + stock_data.getString("askSize");
                    }
                    if(stock_data.getString("askPrice").equals("null")) {
                        stockAskPrice = "Ask Price: -";
                    } else {
                        stockAskPrice = "Ask Price: $" + stock_data.getString("askPrice");
                    }

                    Intent intent = new Intent(MainActivity.this, StockDetails.class);
                    Collections.addAll(stockDataJSON, stockSymbol, stockTimestamp, stockLast,
                            stockPrevClose, stockOpenPrice, stockHighPrice, stockLowPrice,
                            stockMidPrice, stockVol, stockBidSize, stockBidPrice, stockAskSize,
                            stockAskPrice, stockName, stockDesc);
                    intent.putStringArrayListExtra("stockData", stockDataJSON);
                    MainActivity.this.startActivity(intent);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class fetchStockNames extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String rawURL = "https://api.tiingo.com/tiingo/utilities/search?query=" +
                    strings[0] + "&token=" + TIINGO_API_TOKEN;
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

                } else {
                    throw new RuntimeException("Request failed, HTTP Code:" + httpresponse);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress(100);
            return stocks.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ArrayList<String> stockTickers = new ArrayList<String>();
            ArrayList<String> stockNames = new ArrayList<String>();
            try {
                String stockTitle;
                String stockSymbol;
                JSONArray data = new JSONArray(result);
                for(int i = 0; i < data.length(); i++) {
                    JSONObject stock_data = data.getJSONObject(i);
                    stockTitle = stock_data.getString("name");
                    stockSymbol = stock_data.getString("ticker");
                    stockTickers.add(stockSymbol);
                    stockNames.add(stockTitle);
                }
            } catch(JSONException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
            ArrayAdapter stockAuto = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, stockTickers) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);
                    text1.setText(stockTickers.get(position));
                    text2.setText(stockNames.get(position));
                    return view;
                }
            };
            autoText.setAdapter(stockAuto);
        }
    }

    class fetchStockFavorites extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            String capURL = "https://api.tiingo.com/tiingo/fundamentals/" + params[0]
                    + "/daily?token=" + TIINGO_API_TOKEN + "&startDate=" + getDate();
            String infoURL = "https://api.tiingo.com/tiingo/daily/" + params[0]
                    + "?token=" + TIINGO_API_TOKEN;
            String priceURL = "https://api.tiingo.com/iex/?tickers=" +
                    params[0] + "&token=" + TIINGO_API_TOKEN;
            StringBuilder stocks = new StringBuilder();
            try {
                URL url = null;
                URL url1 = new URL(capURL);
                URL url2 = new URL(infoURL);
                URL url3 = new URL(priceURL);
                int httpresponse;

                for(int i = 0; i < 3; i++) {
                    switch(i) {
                        case 0:
                            url = url1;
                            break;
                        case 1:
                            url = url2;
                            break;
                        case 2:
                            url = url3;
                            break;
                    }
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
                    }
                    else {
                        System.out.println("Request failed, HTTP Code:"
                                + httpresponse + " with URL -> " + url
                                + "\nStocks limited to only the DOW30.");
                    }
                    if(i != 2) {
                        stocks.append(",");
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String mergeJSON = stocks.toString().replaceAll("\\[|\\]|\\{|\\}", "");
            mergeJSON = "[{" + mergeJSON + "}]";

            return mergeJSON;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                String marketCap = "";
                String ticker = "";
                String name = "";
                String price = "";
                String compare = "";

                JSONArray data = new JSONArray(result);
                ArrayList<String> favStockData = new ArrayList<>();
                JSONObject stock_data = data.getJSONObject(0);

                marketCap = stock_data.getString("marketCap");
                ticker = stock_data.getString("ticker");
                name = stock_data.getString("name");
                price = stock_data.getString("last");
                compare = stock_data.getString("open");

                if(stock_data.toString().equals("[{,,}]")) {
                    Toast.makeText(MainActivity.this,
                            "Hourly limit to query API reached, please wait.",
                            Toast.LENGTH_LONG).show();
                    throw new JSONException("Hourly limit reached.");
                }

                Collections.addAll(favStockData, name, ticker, price, marketCap, compare);
                addToFavs(favStockData);

                FavStockAdapter favStockAdp = new FavStockAdapter (MainActivity.this, stockDetails);
                recyclerView.setAdapter(favStockAdp);
            } catch(JSONException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    public void addToFavs(ArrayList<String> input) {
        this.stockDetails.add(input);
    }

    public void clearRefresh() {
        this.stockDetails.clear();
    }

    public void refreshFavs() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        for(String s: sharedPreferences.getAll().keySet()) {
            new fetchStockFavorites().execute(s);
        }
    }

    public static String getDate() {
        Date d = new Date(System.currentTimeMillis());
        return ymd.format(d);
    }
}