package com.example.foxowl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeeklyAdapter extends RecyclerView.Adapter<WeeklyAdapter.ViewHolder> {

    private List<List<String>> localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView weekDayText;
        private final ImageView weatherIcon;
        private final TextView maxTempText;
        private final TextView minTempText;

        public ViewHolder(View view){
            super(view);

            weekDayText = view.findViewById(R.id.weekDayText);
            weatherIcon = view.findViewById(R.id.weekDayImage);
            maxTempText = view.findViewById(R.id.maxTempText);
            minTempText = view.findViewById(R.id.minTempText);
        }

        public TextView getWeekDayText() {
            return weekDayText;
        }

        public ImageView getWeatherIcon() {
            return weatherIcon;
        }

        public TextView getMaxTempText() {
            return maxTempText;
        }

        public TextView getMinTempText() {
            return minTempText;
        }
    }

    public WeeklyAdapter(List<List<String>> data){
        localDataSet = data;
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.weekly_frame, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeeklyAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getWeekDayText().setText(localDataSet.get(position).get(3));
        viewHolder.getMaxTempText().setText(localDataSet.get(position).get(1) + "°C");
        viewHolder.getMinTempText().setText(localDataSet.get(position).get(2) + "°C");
        String mainWeather = localDataSet.get(position).get(0).toLowerCase();
        if(mainWeather.equals("clear")){
            viewHolder.getWeatherIcon().setImageResource(R.drawable.clear);
        }
        else if(mainWeather.equals("clouds")){
            viewHolder.getWeatherIcon().setImageResource(R.drawable.clouds);
        }
        else if(mainWeather.equals("rain")){
            viewHolder.getWeatherIcon().setImageResource(R.drawable.rain_v3);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
