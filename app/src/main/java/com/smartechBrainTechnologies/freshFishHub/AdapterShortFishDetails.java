package com.smartechBrainTechnologies.freshFishHub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jackandphantom.circularimageview.RoundedImage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterShortFishDetails extends RecyclerView.Adapter<AdapterShortFishDetails.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<ModelShortFishDetails> FishList;
    private ArrayList<ModelShortFishDetails> FishListFull;
    private OnFishSelectedListener onFishSelectedListener;

    public AdapterShortFishDetails(Context context, ArrayList<ModelShortFishDetails> fishList, OnFishSelectedListener onFishSelectedListener) {
        this.context = context;
        this.FishList = fishList;
        FishListFull = new ArrayList<>(fishList);
        this.onFishSelectedListener = onFishSelectedListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_short_fish_details, parent, false), onFishSelectedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(FishList.get(position).getFishImage()).fit().into(holder.fish_image);
        holder.fish_name.setText(FishList.get(position).getFishName());
        holder.fish_price.setText("₹" + FishList.get(position).getFishPrice() + "/kg");
    }

    @Override
    public int getItemCount() {
        return FishList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Filter getFilter() {
        return shortFishDetailsFilter;
    }

    private Filter shortFishDetailsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ModelShortFishDetails> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.clear();
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ModelShortFishDetails fish : FishListFull) {
                    if (fish.getFishName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(fish);
                    }
                }
            }

            FilterResults results = new FilterResults();
            if (filteredList.isEmpty() && constraint == null) {

            } else if (filteredList.isEmpty()) {
                Toast.makeText(context, "No matches Found", Toast.LENGTH_SHORT).show();
            }

            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            FishList.clear();
            FishList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RoundedImage fish_image;
        TextView fish_name, fish_price;
        OnFishSelectedListener onFishSelectedListener;

        public MyViewHolder(@NonNull View itemView, OnFishSelectedListener onFishSelectedListener) {
            super(itemView);
            fish_image = itemView.findViewById(R.id.short_fish_details_pic);
            fish_name = itemView.findViewById(R.id.short_fish_details_name);
            fish_price = itemView.findViewById(R.id.short_fish_details_price);
            this.onFishSelectedListener = onFishSelectedListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onFishSelectedListener.onFishClick(getAdapterPosition());
        }
    }

    public interface OnFishSelectedListener {
        void onFishClick(int position);
    }
}
