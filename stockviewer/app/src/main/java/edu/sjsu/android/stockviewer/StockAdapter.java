package edu.sjsu.android.stockviewer;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.*;
import android.widget.*;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder>{

    ArrayList<String> stockData;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView stockTitle;
        TextView stockName;
        TextView stockDesc;
        TextView stockTimestamp;
        TextView stockLast;
        TextView stockPrevClose;
        TextView stockOpenPrice;
        TextView stockHighPrice;
        TextView stockLowPrice;
        TextView stockMidPrice;
        TextView stockVol;
        TextView stockBidSize;
        TextView stockBidPrice;
        TextView stockAskSize;
        TextView stockAskPrice;

        ConstraintLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stockTitle = itemView.findViewById(R.id.txtStockName);
            stockName = itemView.findViewById(R.id.txtStockFullName);
            stockDesc = itemView.findViewById(R.id.txtStockDescription);
            stockTimestamp = itemView.findViewById(R.id.txtStockDate);
            stockLast = itemView.findViewById(R.id.txtStockLast);
            stockPrevClose = itemView.findViewById(R.id.txtStockPrevClose);
            stockOpenPrice = itemView.findViewById(R.id.txtStockOpenPrice);
            stockHighPrice = itemView.findViewById(R.id.txtStockHighPrice);
            stockLowPrice = itemView.findViewById(R.id.txtStockLowPrice);
            stockMidPrice = itemView.findViewById(R.id.txtStockMidPrice);
            stockVol = itemView.findViewById(R.id.txtStockVol);
            stockBidSize = itemView.findViewById(R.id.txtStockBidSize);
            stockBidPrice = itemView.findViewById(R.id.txtStockBidPrice);
            stockAskSize = itemView.findViewById(R.id.txtStockAskSize);
            stockAskPrice = itemView.findViewById(R.id.txtStockAskPrice);
        }
    }

    public StockAdapter(Context ct, ArrayList<String> stockDataList) {
        context = ct;
        stockData = stockDataList;
    }

    @Override
    public StockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.stockrecycler, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.stockTitle.setText(stockData.get(position));
        holder.stockName.setText(stockData.get(position + 13));
        holder.stockDesc.setText(stockData.get(position + 14));
        holder.stockTimestamp.setText(stockData.get(position + 1));
        holder.stockLast.setText(stockData.get(position + 2));
        holder.stockPrevClose.setText(stockData.get(position + 3));
        holder.stockOpenPrice.setText(stockData.get(position + 4));
        holder.stockHighPrice.setText(stockData.get(position + 5));
        holder.stockLowPrice.setText(stockData.get(position + 6));
        holder.stockMidPrice.setText(stockData.get(position + 7));
        holder.stockVol.setText(stockData.get(position + 8));
        holder.stockBidSize.setText(stockData.get(position + 9));
        holder.stockBidPrice.setText(stockData.get(position + 10));
        holder.stockAskSize.setText(stockData.get(position + 11));
        holder.stockAskPrice.setText(stockData.get(position + 12));
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
