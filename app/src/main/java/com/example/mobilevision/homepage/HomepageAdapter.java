package com.example.mobilevision.homepage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilevision.R;
import com.example.mobilevision.database.Bills;

import java.util.ArrayList;

public class HomepageAdapter extends RecyclerView.Adapter<HomepageAdapter.HomepageAdapterViewHolder> {

    private ArrayList<Bills> prices;
    private Context context;

    public HomepageAdapter(ArrayList<Bills> prices, Context context) {
        this.prices = prices;
        this.context = context;
    }


    @NonNull
    @Override
    public HomepageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_homepage_prices, parent, false);

        return new HomepageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomepageAdapterViewHolder holder, int position) {
        holder.price.setText(prices.get(position).getPrice());
        holder.date.setText(prices.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return prices.size();
    }

    public class HomepageAdapterViewHolder extends RecyclerView.ViewHolder{
        private TextView price, date;
        public HomepageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            price = itemView.findViewById(R.id.homepage_tv_price);
            date = itemView.findViewById(R.id.homepage_tv_date);
        }

    }
}
