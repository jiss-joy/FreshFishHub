package com.smartechBrainTechnologies.freshFishHub;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jackandphantom.circularimageview.RoundedImage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterBestDealsFish extends RecyclerView.Adapter<AdapterBestDealsFish.MyViewHolder> {

    private Context context;
    private ArrayList<ModelBestDealsFish> BestDeals;
    private OnBestDealListener onBestDealListener;

    public AdapterBestDealsFish(Context context, ArrayList<ModelBestDealsFish> BestDeals, OnBestDealListener onBestDealListener) {
        this.context = context;
        this.BestDeals = BestDeals;
        this.onBestDealListener = onBestDealListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterBestDealsFish.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_best_deals, parent, false), onBestDealListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(BestDeals.get(position).getFishImage()).fit().into(holder.fish_image);
        holder.fish_name.setText(BestDeals.get(position).getFishName());
        holder.fish_new_price.setText("₹" + BestDeals.get(position).getFishNewPrice() + "/KG");
        holder.fish_old_price.setText("₹" + BestDeals.get(position).getFishOldPrice() + "/KG");
        holder.fish_old_price.setPaintFlags(holder.fish_old_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public int getItemCount() {
        return BestDeals.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RoundedImage fish_image;
        TextView fish_name, fish_new_price, fish_old_price;
        OnBestDealListener onBestDealListener;

        public MyViewHolder(@NonNull View itemView, OnBestDealListener onBestDealListener) {
            super(itemView);
            fish_image = itemView.findViewById(R.id.best_deal_pic);
            fish_name = itemView.findViewById(R.id.best_deal_name);
            fish_new_price = itemView.findViewById(R.id.best_deal_new_price);
            fish_old_price = itemView.findViewById(R.id.best_deal_old_price);
            this.onBestDealListener = onBestDealListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBestDealListener.onBestDealClick(getAdapterPosition());
        }
    }

    public interface OnBestDealListener {
        void onBestDealClick(int position);
    }
}
