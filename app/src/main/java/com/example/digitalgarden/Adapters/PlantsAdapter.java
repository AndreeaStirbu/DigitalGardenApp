package com.example.digitalgarden.Adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import com.example.digitalgarden.Model.Plant;
import com.example.digitalgarden.R;
import com.squareup.picasso.Picasso;

/**
 * @Author: Andreea Stirbu
 * @Since: 27/03/2020.
 *
 * Plant Adapter that binds data to each card from recycler view
 */
public class PlantsAdapter extends RecyclerView.Adapter<PlantsAdapter.MyViewHolder> {
    private ArrayList<Plant> mArrayList;
    private Context mContext;
    private OnPlantListener mOnPlantListener;

    public PlantsAdapter(ArrayList<Plant> mArrayList, Context mContext, OnPlantListener mOnPlantListener) {
        this.mArrayList = mArrayList;
        this.mContext = mContext;
        this.mOnPlantListener = mOnPlantListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_plant,parent,false);
        return new MyViewHolder(itemView, mOnPlantListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Plant plant = mArrayList.get(position);
        holder.plantName.setText(plant.getPlantName());
        holder.setImageSrc(mContext, plant.getImageSrc(), holder.card);

        // Set the "Needs Water" icon
        if(plant.getNeedsWater()) {
            holder.waterSign.setVisibility(View.VISIBLE);
        }
        else {
            holder.waterSign.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        int arr;
        if(mArrayList.size()==0){
            arr = 0;
        }
        else{
            arr = mArrayList.size();
        }
        return arr;
    }

    /**
     * Class that holds details for each instance of the card
     */
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Variables of the card that holds a plant
        TextView plantName;
        ImageView plantThumbnail;
        ImageView waterSign;
        CardView card;
        OnPlantListener onPlantListener;

        public MyViewHolder(View itemView, OnPlantListener onPlantListener) {
            super(itemView);

            // Initialise UI
            plantName = itemView.findViewById(R.id.plantName);
            plantThumbnail = itemView.findViewById(R.id.plantThumbnail);
            waterSign = itemView.findViewById(R.id.water_sign);
            card = itemView.findViewById(R.id.card_plant);

            // Hide card. (Show it once the image has loaded)
            card.setVisibility(View.GONE);

            // Bind the onPlantListener listener for each card
            this.onPlantListener = onPlantListener;

            // Set the whole card to be clickable
            itemView.setOnClickListener(this);
        }

        // Load the image into the card's Image view
        public void setImageSrc(Context context, String imageSrc, final CardView card){
            plantThumbnail = itemView.findViewById(R.id.plantThumbnail);
            Picasso.with(context).load(imageSrc)
                                 .resize(1000, 1000)
                                 .centerInside()
                                 .into(plantThumbnail, new com.squareup.picasso.Callback() {
                                     @Override
                                     public void onSuccess() {
                                         // Set the card to be visible only after the image has loaded
                                        card.setVisibility(View.VISIBLE);
                                     }

                                     @Override
                                     public void onError() {}
                                 });
        }

        // When card is clicked call OnPlantClick and perform action
        @Override
        public void onClick(View v) {
            onPlantListener.onPlantClick(getAdapterPosition());
        }
    }

    /**
     * Interface that implements onPlantClick listener
     */
    public interface OnPlantListener {
        void onPlantClick(int position);
    }
}