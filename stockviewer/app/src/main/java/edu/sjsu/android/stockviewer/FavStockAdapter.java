package edu.sjsu.android.stockviewer;

/* Code attributions:
*   - Converting scientific notation - https://stackoverflow.com/questions/2546147/java-convert-scientific-notation-to-regular-int
* */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.*;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static edu.sjsu.android.stockviewer.MainActivity.TIINGO_API_TOKEN;
import static java.lang.Math.abs;
import static java.lang.Math.round;

public class FavStockAdapter extends RecyclerView.Adapter<FavStockAdapter.ViewHolder> {
    public static final double MILLION = 999999.0;
    public static final double BILLION = 999999999.0;
    public static final double TRILLION = 999999999999.0;
    public static final int TRILLION_CUTOFF = 12;
    public static final int BILLION_CUTOFF = 12;
    public static final int MILLION_CUTOFF = 9;

    ArrayList<List<String>> stockData;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView FavStockName;
        TextView FavStockTicker;
        TextView FavStockPrice;
        TextView FavStockChange;
        TextView FavStockMarketCap;

        ConstraintLayout favLayoutManager;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            FavStockName = itemView.findViewById(R.id.txtFavStockName);
            FavStockTicker = itemView.findViewById(R.id.txtFavStockTicker);
            FavStockPrice = itemView.findViewById(R.id.txtFavStockPrice);
            FavStockChange = itemView.findViewById(R.id.txtFavStockChange);
            FavStockMarketCap = itemView.findViewById(R.id.txtFavStockMarketCap);
            favLayoutManager = itemView.findViewById(R.id.favLayoutManager);
        }
    }

    public FavStockAdapter(Context ct, ArrayList<List<String>> stockDataList) {
        context = ct;
        stockData = stockDataList;
    }

    @Override
    public FavStockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.fav_stock_recycler, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        double diff = (Double.valueOf(stockData.get(position).get(2))
                - Double.valueOf(stockData.get(position).get(4))) /
                Double.valueOf(stockData.get(position).get(2)) * 10000.0;

        holder.FavStockName.setText(stockData.get(position).get(0));
        holder.FavStockTicker.setText(stockData.get(position).get(1));
        holder.FavStockPrice.setText("$" + stockData.get(position).get(2));
        holder.FavStockMarketCap.setText("Market Cap: $" + numberConv(stockData.get(position).get(3)));
        if(diff > 0) {
            holder.FavStockChange.setText("+" + round(diff)/100.0+ "%");
            holder.FavStockChange.setBackgroundColor(Color.parseColor("#45c65f"));
        } else {
            holder.FavStockChange.setBackgroundColor(Color.parseColor("#c65645"));
            holder.FavStockChange.setText("-" + abs(round(diff))/100.0 + "%");
        }
        holder.favLayoutManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new fetchStockInfo().execute(stockData.get(position).get(1));
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockData.size();
    }

    class fetchStockInfo extends AsyncTask<String, Integer, String> {
        private final ProgressDialog dialog = new ProgressDialog(context);
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
                        throw new RuntimeException("Request failed, HTTP Code:" + httpresponse);
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
                JSONArray data = new JSONArray(result);
                JSONObject stock_data = data.getJSONObject(0);
                ArrayList<String> stockDataJSON = new ArrayList<String>();

                if(stock_data == null || stock_data.getString("ticker").equals("000001")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setMessage("Stock name gave no results, try again.");
                    alertDialogBuilder.setPositiveButton("Ok", null);
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else {
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

                    Intent intent = new Intent(context, StockDetails.class);
                    Collections.addAll(stockDataJSON, stockSymbol, stockTimestamp, stockLast,
                            stockPrevClose, stockOpenPrice, stockHighPrice, stockLowPrice,
                            stockMidPrice, stockVol, stockBidSize, stockBidPrice, stockAskSize,
                            stockAskPrice, stockName, stockDesc);
                    intent.putStringArrayListExtra("stockData", stockDataJSON);
                    context.startActivity(intent);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String numberConv(String num) {
        BigDecimal millComp = new BigDecimal(MILLION);
        BigDecimal billComp = new BigDecimal(BILLION);
        BigDecimal trillComp = new BigDecimal(TRILLION);

        BigDecimal inputnum = new BigDecimal(num);
        String numReadable = inputnum.toString();

        if(inputnum.compareTo(trillComp) == 1) {
            numReadable = numReadable.substring(0, numReadable.length() - TRILLION_CUTOFF) + " Trillion";
        }
        else if(inputnum.compareTo(billComp) == 1) {
            numReadable = numReadable.substring(0, numReadable.length() - BILLION_CUTOFF) + " Billion";
        }
        else if(inputnum.compareTo(millComp) == 1) {
            numReadable = numReadable.substring(0, numReadable.length() - MILLION_CUTOFF) + " Million";
        }
        return numReadable;
    }
}
