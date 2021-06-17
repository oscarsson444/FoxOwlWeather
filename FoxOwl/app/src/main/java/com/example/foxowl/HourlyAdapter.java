package com.example.foxowl;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private List<List<String>> localDataSet; //Contains all the 3-data pairs which will fill frames

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView imageView;
        private final TextView descText;
        private final TextView timeTextView;
        private final TextView tempTextView;

        public ViewHolder(View view){
            super(view);

            imageView = (ImageView) view.findViewById(R.id.hourlyImage);
            timeTextView = (TextView) view.findViewById(R.id.hourlyTimeView);
            tempTextView = (TextView) view.findViewById(R.id.tempTextView);
            descText = (TextView) view.findViewById(R.id.descText);
        }

        public ImageView getImageView(){
            return imageView;
        }

        public TextView getTimeTextView(){
            return timeTextView;
        }

        public TextView getTempTextView(){
            return tempTextView;
        }

        public TextView getDescText() {
            return descText;
        }
    }

    public HourlyAdapter(List<List<String>> dataSet){
        localDataSet = dataSet;
    }

    @Override
    public HourlyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.hourly_frame, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.getTimeTextView().setText(localDataSet.get(position).get(2) + ":00");
        viewHolder.getTempTextView().setText(localDataSet.get(position).get(1) + "°C");
        viewHolder.getDescText().setText(localDataSet.get(position).get(3));
        String mainWeather = localDataSet.get(position).get(0).toLowerCase();
        if(mainWeather.equals("clear")){
            viewHolder.getImageView().setImageResource(R.drawable.clear);
        }
        else if(mainWeather.equals("clouds")){
            viewHolder.getImageView().setImageResource(R.drawable.clouds);
        }

    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
