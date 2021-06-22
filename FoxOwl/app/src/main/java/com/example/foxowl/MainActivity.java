package com.example.foxowl;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQ_CODE = 1;
    private String cityString;
    private TextView cityName;
    private TextView tempText;
    private TextView descText;
    private ImageView dailyWeatherImage;
    private List<List<String>> hourlyData;
    private List<List<String>> weeklyData;
    private HourlyAdapter hourlyAdapter;
    private WeeklyAdapter weeklyAdapter;
    private FusedLocationProviderClient locationProvider;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        //Initiates the weekly RecyclerView
        RecyclerView weeklyRecycler = findViewById(R.id.weeklyRecycler);
        weeklyData = new ArrayList<>();
        weeklyAdapter = new WeeklyAdapter(weeklyData);
        RecyclerView.LayoutManager weeklyLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        weeklyRecycler.setLayoutManager(weeklyLayoutManager);
        weeklyRecycler.setAdapter(weeklyAdapter);

        //Initiates the daily RecyclerView
        RecyclerView hourlyRecycler = findViewById(R.id.hourlyRecycler);
        hourlyData = new ArrayList<>();
        hourlyAdapter = new HourlyAdapter(hourlyData);
        RecyclerView.LayoutManager dailyLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        hourlyRecycler.setLayoutManager(dailyLayoutManager);
        hourlyRecycler.setAdapter(hourlyAdapter);

        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        cityName = findViewById(R.id.cityNameTextView);
        tempText = findViewById(R.id.tempTextView);
        descText = findViewById(R.id.descriptionTextView);
        dailyWeatherImage = findViewById(R.id.weatherIcon);

        /* Used for first design

        ConstraintLayout expandableLayout = findViewById(R.id.expandableLayout);
        Button arrowBtn = findViewById(R.id.arrowBtn);
        CardView dailyCardView = findViewById(R.id.dailyCardView);


        arrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expandableLayout.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(dailyCardView, new AutoTransition());
                    expandableLayout.setVisibility(View.VISIBLE);
                    arrowBtn.setBackgroundResource(R.drawable.arrow_up);
                } else {
                    TransitionManager.beginDelayedTransition(dailyCardView, new AutoTransition());
                    expandableLayout.setVisibility(View.GONE);
                    arrowBtn.setBackgroundResource(R.drawable.arrow_down);
                }
            }
        });

         */

        getLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        MenuItem searchIcon = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchIcon.getActionView();
        searchView.setQueryHint("Search Location!");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast toast = Toast.makeText(getApplicationContext(), "text submit!", Toast.LENGTH_SHORT);
                toast.show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationProvider.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    setWeatherData(location);
                } else {
                    requestNewLocationData();
                }
            }
        });
    }

    private void setWeatherData(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude + "&appid=ca5643b6c0e9ef9932984ef63496f38f";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(response);
                            cityString = jsonResponse.getString("name");
                            dataRequest(latitude, longitude);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "Could not get location name!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        queue.add(stringRequest);
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                setWeatherData(location);
            }
        };
        // Initializing LocationRequest
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(50);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(100);

        // setting LocationRequest
        // on FusedLocationClient
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void getLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Permission Denied, this app is now useless!", Toast.LENGTH_SHORT);
                    toast.show();
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLocationPermission();
    }

    private void dataRequest(double latitude, double longitude){
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=" + String.valueOf(latitude)
                + "&lon=" + String.valueOf(longitude) + "&exclude=minutely&appid=ca5643b6c0e9ef9932984ef63496f38f&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(response);
                            parseCurrentWeatherData(jsonResponse);
                            parseHourlyWeatherData(jsonResponse);
                            parseWeeklyWeather(jsonResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(), "API call failed!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        queue.add(stringRequest);
    }

    private void parseHourlyWeatherData(JSONObject jsonResponse){
        try {
            hourlyData.clear();
            JSONArray hourlyList = jsonResponse.getJSONArray("hourly");
            LocalDateTime now = LocalDateTime.now();
            int currentHour = now.getHour() - 1;
            for (int i = 0; i < 24; i++) {
                JSONObject hour = hourlyList.getJSONObject(i);
                String temp = hour.getString("temp");
                float tempFloat = Float.parseFloat(temp);
                temp = String.valueOf((int) tempFloat);
                currentHour = (currentHour+1)%24;

                JSONArray weatherArray = hour.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String main = weatherObject.getString("main");
                String description = weatherObject.getString("description");
                description = description.substring(0,1).toUpperCase() + description.substring(1).toLowerCase();

                List<String> data = new ArrayList<>();
                data.add(main);
                data.add(temp);
                data.add(String.valueOf(currentHour));
                data.add(description);
                hourlyData.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hourlyAdapter.notifyDataSetChanged();
    }

    private void parseCurrentWeatherData(JSONObject jsonResponse){
        try {
            JSONObject currentWeather = jsonResponse.getJSONObject("current");
            String temp = currentWeather.getString("temp");
            float tempFloat = Float.parseFloat(temp);
            temp = String.valueOf((int) tempFloat);

            JSONArray weatherArray = currentWeather.getJSONArray("weather");
            JSONObject weatherObject = weatherArray.getJSONObject(0);
            String main = weatherObject.getString("main");
            String description = weatherObject.getString("description");
            description = description.substring(0,1).toUpperCase() + description.substring(1).toLowerCase();

            cityName.setText(cityString);
            tempText.setText(temp + "°C");
            descText.setText(description);

            if(main.toLowerCase().equals("clear")){
                dailyWeatherImage.setImageResource(R.drawable.clear);
            }
            else if(main.toLowerCase().equals("clouds")){
                dailyWeatherImage.setImageResource(R.drawable.clouds);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Första elementet i arrayen är dagens väder vid klockan 12
    private void parseWeeklyWeather(JSONObject jsonResponse){
        try {
            weeklyData.clear();
            JSONArray weekList = jsonResponse.getJSONArray("daily");
            LocalDateTime now = LocalDateTime.now();
            DayOfWeek currentDay = now.getDayOfWeek();
            for (int i = 0; i < 7; i++) {
                JSONObject day = weekList.getJSONObject(i);
                JSONObject temps = day.getJSONObject("temp");
                String maxTemp = temps.getString("max");
                String minTemp = temps.getString("min");
                float tempFloat = Float.parseFloat(maxTemp);
                maxTemp = String.valueOf((int) tempFloat);
                tempFloat = Float.parseFloat(minTemp);
                minTemp = String.valueOf((int) tempFloat);

                JSONArray weatherArray = day.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String main = weatherObject.getString("main");
                String description = weatherObject.getString("description");
                description = description.substring(0,1).toUpperCase() + description.substring(1).toLowerCase();
                String dayName = currentDay.plus(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                List<String> data = new ArrayList<>();
                data.add(main);
                data.add(maxTemp);
                data.add(minTemp);
                data.add(dayName);

                weeklyData.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        weeklyAdapter.notifyDataSetChanged();
    }
}