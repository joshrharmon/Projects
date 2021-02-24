package edu.sjsu.android.stockviewer;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

public class HistStockAdapter extends RecyclerView.Adapter<HistStockAdapter.ViewHolder>{

    ArrayList<String> stockData;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView HistStockTitle;
        TextView HistStockDate;
        TextView HistStockHigh;
        TextView HistStockLow;
        TextView HistStockOpen;
        TextView HistStockClose;
        TextView HistStockVolume;

        ConstraintLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            HistStockTitle = itemView.findViewById(R.id.txtHistStockTitle);
            HistStockDate = itemView.findViewById(R.id.txtHistStockDate);
            HistStockHigh = itemView.findViewById(R.id.txtHistStockHigh);
            HistStockLow = itemView.findViewById(R.id.txtHistStockLow);
            HistStockOpen = itemView.findViewById(R.id.txtHistStockOpen);
            HistStockClose = itemView.findViewById(R.id.txtHistStockClose);
            HistStockVolume = itemView.findViewById(R.id.txtHistStockVolume);
        }
    }

    public HistStockAdapter(Context ct, ArrayList<String> stockDataList) {
        context = ct;
        stockData = stockDataList;
    }

    @Override
    public HistStockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.stock_hist_recycler, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.HistStockTitle.setText(stockData.get(position));
        holder.HistStockDate.setText(stockData.get(position + 1));
        holder.HistStockHigh.setText(stockData.get(position + 2));
        holder.HistStockLow.setText(stockData.get(position + 3));
        holder.HistStockOpen.setText(stockData.get(position + 4));
        holder.HistStockClose.setText(stockData.get(position + 5));
        holder.HistStockVolume.setText(stockData.get(position + 6));
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
