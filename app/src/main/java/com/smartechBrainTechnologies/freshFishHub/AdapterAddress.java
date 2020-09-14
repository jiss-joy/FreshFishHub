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

    private Context context;
    private ArrayList<ModelAddress> addressList;
    private OnOptionsClickListener onOptionsClickListener;

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
        holder.address_building.setText(addressList.get(position).getAddressBuilding());
        holder.address_area.setText(addressList.get(position).getAddressArea());
        holder.address_city.setText(addressList.get(position).getAddressCity() + ",");
        holder.address_pin.setText(addressList.get(position).getAddressPin());
        if (addressList.get(position).getAddressLandmark().equals("")) {
            holder.address_landmark.setVisibility(View.GONE);
        } else {
            holder.address_landmark.setText(addressList.get(position).getAddressLandmark());
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

        TextView address_name, address_building, address_area, address_city, address_landmark,
                address_state, address_pin, address_status;

        ImageButton optionsBTN;


        public MyViewHolder(@NonNull View itemView, OnOptionsClickListener onOptionsClickListener) {
            super(itemView);
            address_name = itemView.findViewById(R.id.address_name);
            address_building = itemView.findViewById(R.id.address_building);
            address_area = itemView.findViewById(R.id.address_area);
            address_city = itemView.findViewById(R.id.address_city);
            address_landmark = itemView.findViewById(R.id.address_landmark);
            address_state = itemView.findViewById(R.id.address_state);
            address_pin = itemView.findViewById(R.id.address_pin);
            address_status = itemView.findViewById(R.id.address_status);
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
