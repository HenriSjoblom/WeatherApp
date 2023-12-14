package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

//Päivittää säätiedot sijainnin tai annetun kaupungin perusteella, jotka
//valitaan asetuksista
public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    //Valittu kaupunki. Käytetään datan hakuun, jos sijainti ei valittuna
    private String city;
    //Käytetäänkö sijaintia datan hakuun
    private Boolean useLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Jos intentissä mukana arvoja, päivitetään arvot ja päivitetään
        //säätiedot
        if (extras != null) {
            TextView cityTextView = findViewById(R.id.cityNameTextView);
            city = extras.getString("CITY");
            cityTextView.setText(city);
            useLocation = extras.getBoolean("USE_LOCATION");
            getWeatherData(findViewById(android.R.id.content));
        }
    }

    public void getWeatherData(View view) {

        //Haetaan säätiedot sijainnin tai annetun kaupungin perusteella.
        if (useLocation) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat=" +
                            latitude + "&lon=" + longitude +
                            "&appid=6c433438776b5be4ac86001dc88de74d&units=metric";
                    fetchWeatherData(WEATHER_URL);
                    locationManager.removeUpdates(this);
                }
            });

        } else {
            String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                    "&appid=6c433438776b5be4ac86001dc88de74d&units=metric";
            fetchWeatherData(WEATHER_URL);
        }
    }

    private void fetchWeatherData(String WEATHER_URL) {
        //Haetaan säätiedot Volley -kirjastolla
        StringRequest request = new StringRequest(Request.Method.GET, WEATHER_URL, response -> {
            //Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            parseWeatherJsonAndUpdateUi(response);

        }, error -> {
            Toast.makeText(this, R.string.networkError, Toast.LENGTH_LONG).show();
        });
        Volley.newRequestQueue(this).add(request);
        //Toast.makeText(this, "FETCHING DATA", Toast.LENGTH_SHORT).show();
    }

    private void parseWeatherJsonAndUpdateUi(String response) {
        //Parsitaan JSON ja päivitetään näytölle kaupunki, lämpötilä, säätila, kosteus ja
        // tuulen nopeus.
        try {
            JSONObject weatherJSON = new JSONObject(response);
            double temperature = weatherJSON.getJSONObject("main").getDouble("temp");
            String temperatureStr = "" + temperature + "°C";
            double wind = weatherJSON.getJSONObject("wind").getDouble("speed");
            String windStr = "" + wind + " m/s";
            String weather = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
            double humidity = weatherJSON.getJSONObject("main").getDouble("humidity");
            String humidityStr = "" + humidity + "%";
            city = weatherJSON.getString("name");

            TextView temperatureTextView = findViewById(R.id.temperatureValueTextView);
            temperatureTextView.setText(temperatureStr);
            TextView windTextView = findViewById(R.id.windValueTextView);
            windTextView.setText(windStr);
            TextView weatherTextView = findViewById(R.id.weatherValueTextView);
            weatherTextView.setText(weather);
            TextView humidityTextView = findViewById(R.id.humidityValueTextView);
            humidityTextView.setText(humidityStr);

            TextView cityTextView = findViewById(R.id.cityNameTextView);
            cityTextView.setText(city);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public void openSettings(View view) {
        //Siirrytään asetukset activityyn.
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("USE_LOCATION", useLocation);
        intent.putExtra("CITY", city);
        startActivity(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //Tallennetaan muistiin kaupunki, käytetäänkö sijaintia, lämpötila, säänkuva,
        // kosteus ja tuulen nopeus.
        bundle.putString("CITY", city);
        bundle.putString("USE_LOCATION", String.valueOf(useLocation));
        TextView temperatureTextView = findViewById(R.id.temperatureValueTextView);
        bundle.putString("TEMPERATURE", temperatureTextView.getText().toString());
        TextView weatherTextView = findViewById(R.id.weatherValueTextView);
        bundle.putString("WEATHER", weatherTextView.getText().toString());
        TextView humidityTextView = findViewById(R.id.humidityValueTextView);
        bundle.putString("HUMIDITY", humidityTextView.getText().toString());
        TextView windTextView = findViewById(R.id.windValueTextView);
        bundle.putString("WIND", windTextView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        // Luetaan kaupunki, käytetäänkö sijaintia, lämpötila, säänkuva,
        // kosteus ja tuulen nopeus.
        city = bundle.getString("CITY", "");
        useLocation = bundle.getBoolean("USE_LOCATION", false);
        TextView temperatureTextView = findViewById(R.id.temperatureValueTextView);
        String temperature = bundle.getString("TEMPERATURE", "");
        temperatureTextView.setText(temperature);
        TextView weatherTextView = findViewById(R.id.weatherValueTextView);
        String weather = bundle.getString("WEATHER", "");
        weatherTextView.setText(weather);
        TextView humidityTextView = findViewById(R.id.humidityValueTextView);
        String humidity = bundle.getString("HUMIDITY", "");
        humidityTextView.setText(humidity);
        TextView windTextView = findViewById(R.id.windValueTextView);
        String wind = bundle.getString("WIND", "");
        windTextView.setText(wind);
    }
}