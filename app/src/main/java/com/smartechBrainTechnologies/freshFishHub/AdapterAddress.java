package com.smartechBrainTechnologies.freshFishHub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterAddress extends RecyclerView.Adapter<AdapterAddress.MyViewHolder> {

    private final Context context;
    private final ArrayList<ModelAddress> addressList;
    private final OnOptionsClickListener onOptionsClickListener;

    private int mPosition;

    public AdapterAddress(Context context, ArrayList<ModelAddress> addressList, OnOptionsClickListener onOptionsClickListener) {
        this.context = context;
        this.addressList = addressList;
        this.onOptionsClickListener = onOptionsClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterAddress.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_address, parent, false), onOptionsClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.address_name.setText(addressList.get(position).getAddressName());
        holder.address.setText(addressList.get(position).getAddress());
        holder.address_contact.setText(addressList.get(position).getAddressContact());
        if (addressList.get(position).getAddressDeliveryStatus().equals("No Delivery")) {
            holder.no_delviery.setVisibility(View.VISIBLE);
        } else {
            holder.no_delviery.setVisibility(View.GONE);
        }
        if (addressList.get(position).getAddressStatus().equals("Default")) {
            holder.address_status.setText(addressList.get(position).getAddressStatus());
        } else {
            holder.address_status.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    @Override
    public int getItemViewType(int position) {
        mPosition = position;
        return super.getItemViewType(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView address_name, address_contact, address, address_status, no_delviery;

        ImageButton optionsBTN;


        public MyViewHolder(@NonNull View itemView, OnOptionsClickListener onOptionsClickListener) {
            super(itemView);
            address_name = itemView.findViewById(R.id.address_name);
            address_contact = itemView.findViewById(R.id.address_contact);
            address = itemView.findViewById(R.id.address);
            address_status = itemView.findViewById(R.id.address_status);
            no_delviery = itemView.findViewById(R.id.no_delivery_tv);
            no_delviery.setVisibility(View.GONE);
            optionsBTN = itemView.findViewById(R.id.address_options_btn);

            optionsBTN.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onOptionsClickListener.onOptionsClick(getAdapterPosition(), v);
        }

    }

    public interface OnOptionsClickListener {
        void onOptionsClick(int position, View v);
    }

}
