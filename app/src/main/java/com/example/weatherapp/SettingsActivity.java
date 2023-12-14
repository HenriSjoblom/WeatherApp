package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

//Asetuksista, voidaan valita käytetäänkö datan hakuun annettua kaupunkia vai laitteen
//sijaintia.
public class SettingsActivity extends AppCompatActivity {
    //Käytetäänkö sijaintia vai kaupungin nimeä datan haussa
    private Boolean useLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if ( extras != null ) {
            useLocation = extras.getBoolean("USE_LOCATION", false);
            String city = extras.getString("CITY", "");
            EditText cityEditText = findViewById(R.id.cityInputEditText);
            cityEditText.setText(city);

            SwitchCompat locationSwitch = findViewById(R.id.locationSwitch);
            locationSwitch.setChecked(useLocation);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //Tallennetaan muistiin kaupunki ja käytetäänkö sijaintia
        bundle.putBoolean("USE_LOCATION", useLocation);
        EditText city = findViewById(R.id.cityInputEditText);
        bundle.putString("CITY", String.valueOf(city.getText()));

    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        // Luetaan kaupunki ja käytetäänkö sijaintia
        useLocation = bundle.getBoolean("USE_LOCATION", false);
        String city = bundle.getString("CITY", "");
        EditText cityEditText = findViewById(R.id.cityInputEditText);
        cityEditText.setText(city);

        SwitchCompat locationSwitch = findViewById(R.id.locationSwitch);
        locationSwitch.setChecked(useLocation);
    }

    public void switchChange(View view) {
        //Tarkistetaan uusi swichin arvo ja tarkistetaan lupa laitteen sijaintiin,
        //jos sijainti valittuna.
        SwitchCompat switchView = (SwitchCompat) view;
        boolean switchState = switchView.isChecked();
        if (switchState) {
            useLocation = true;
            checkLocationPermission();
        } else {
            useLocation = false;
        }

    }

    public void checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    public void saveSettings(View view) {
        //Siirrytään MainActivityyn ja tallennetaan mukaan, käytetäänkö siajintia ja
        //kaupungin nimi.
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USE_LOCATION", useLocation);

        EditText city = findViewById(R.id.cityInputEditText);
        intent.putExtra("CITY", city.getText().toString());

        startActivity(intent);

    }
}
