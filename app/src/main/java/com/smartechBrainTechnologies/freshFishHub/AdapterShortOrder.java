package com.smartechBrainTechnologies.freshFishHub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShortOrder extends RecyclerView.Adapter<AdapterShortOrder.MyViewHolder> {

    private Context context;
    private ArrayList<ModelShortOrder> OrderList;

    public AdapterShortOrder(Context context, ArrayList<ModelShortOrder> orderList) {
        this.context = context;
        OrderList = orderList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterShortOrder.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_short_order, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(OrderList.get(position).getOrderFishImage()).into(holder.order_fish_image);
        holder.order_fish_name.setText(OrderList.get(position).getOrderFishName());
        holder.order_fish_qty.setText(OrderList.get(position).getOrderFishQty() + "KG");
        String totalPrice = String.valueOf(Float.parseFloat(OrderList.get(position).getOrderFishQty()) * Float.parseFloat(OrderList.get(position).getOrderFishPrice()));
        holder.order_total_price.setText("₹" + totalPrice);
        holder.order_status.setText(OrderList.get(position).getOrderStatus());
    }


    @Override
    public int getItemCount() {
        return OrderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView order_fish_image;
        TextView order_fish_name, order_total_price, order_fish_qty, order_status;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            order_fish_image = itemView.findViewById(R.id.short_order_pic);
            order_fish_name = itemView.findViewById(R.id.short_order_name);
            order_total_price = itemView.findViewById(R.id.short_order_total_price);
            order_fish_qty = itemView.findViewById(R.id.short_order_qty);
            order_status = itemView.findViewById(R.id.short_order_status);

        }
    }
}
