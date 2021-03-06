package cl.mzapatae.icaro.Features;
/*
 * Created by miguelost on 05-02-15.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import cl.mzapatae.icaro.Activities.Icaro;
import cl.mzapatae.icaro.ApiClient.ApiResponseHandler;
import cl.mzapatae.icaro.ApiClient.ApiWeather;
import cl.mzapatae.icaro.R;
import cl.mzapatae.icaro.Utils.GPSTracker;
import cl.mzapatae.icaro.Utils.LocalStorage;
import cl.mzapatae.icaro.Utils.ProgressBar;

public class Weather extends Fragment {
    FrameLayout layout;
    ImageView icon;
    TextView temperature;
    TextView status;
    TextView temperatureMin;
    TextView temperatureMax;
    TextView humidity;
    TextView city;
    TextView country;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        Bundle bundle = this.getArguments();
        rootView = inflater.inflate(R.layout.fragment_weather, container, false);

        Toolbar mToolbarCard = (Toolbar) rootView.findViewById(R.id.toolbar_weather);
        layout = (FrameLayout) rootView.findViewById(R.id.weather_layout);
        icon = (ImageView) rootView.findViewById(R.id.weather_icon);
        temperature = (TextView) rootView.findViewById(R.id.weather_temperature);
        status = (TextView) rootView.findViewById(R.id.weather_status);
        temperatureMin = (TextView) rootView.findViewById(R.id.weather_tempmin);
        temperatureMax = (TextView) rootView.findViewById(R.id.weather_tempmax);
        humidity = (TextView) rootView.findViewById(R.id.weather_humidity);
        city = (TextView) rootView.findViewById(R.id.weather_city);
        country = (TextView) rootView.findViewById(R.id.weather_country);
        mToolbarCard.setTitle(getResources().getString(R.string.weather_toolbar_title));

        layout.setVisibility(View.INVISIBLE);
        if (bundle != null) {
            setWeather(bundle.getString("city"));
        } else {
            GPSTracker gps = new GPSTracker(getActivity());
            if (gps.canGetLocation()) {
                setWeather(gps.getLatitude(), gps.getLongitude());
            }
        }
        return rootView;
    }

    private void setWeather(double latitude, double longitude) {
        try {
            ApiWeather client = new ApiWeather();
            ApiWeather.setContext(getActivity());
            client.retrieveWeather(latitude, longitude, new responseHandler());
        } catch (Exception e) {
            Log.e("API Error", Log.getStackTraceString(e));
        }
    }

    private void setWeather(String city) {
        try {
            ApiWeather client = new ApiWeather();
            ApiWeather.setContext(getActivity());
            client.retrieveWeather(city, new responseHandler());
        } catch (Exception e) {
            Log.e("API Error", Log.getStackTraceString(e));
        }
    }

    private void setImage(int rawImage) {
        icon.setImageResource(rawImage);
    }

    private class responseHandler extends ApiResponseHandler {
        @Override
        public void onStart() {
            ProgressBar.showLoadProgressBar(getActivity());
        }

        @Override
        public void onFinish() {
            ProgressBar.dismissLoadProgressBar();
        }

        @Override
        public void onSuccess(HashMap<String, String> dataReturned) {
            temperature.setText(dataReturned.get("temperature") + "º");
            status.setText(dataReturned.get("status").toUpperCase());
            temperatureMin.setText(getActivity().getString(R.string.temperature_min) +
                    dataReturned.get("temperatureMin") + "º");
            temperatureMax.setText(getActivity().getString(R.string.temperature_max) +
                    dataReturned.get("temperatureMax") + "º");
            humidity.setText(getActivity().getString(R.string.humidity) + dataReturned.get("humidity") + "%");
            city.setText(dataReturned.get("city"));
            country.setText(dataReturned.get("country"));

            speak(dataReturned.get("status").toUpperCase() + " con una temperatura de " + dataReturned.get("temperature") + " grados");

            if (dataReturned.get("code").equals("01d") || dataReturned.get("code").equals("01n")) {   //sky clear - despejado
                setImage(R.raw.sun);
            }
            if (dataReturned.get("code").equals("02d") || dataReturned.get("code").equals("02n")) {  //few clouds - pocas nubes
                setImage(R.raw.broken_clouds);
            }
            if (dataReturned.get("code").equals("03d") || dataReturned.get("code").equals("03n")) {  //scattered clouds - nubes entrelazadas
                setImage(R.raw.cloud);
            }
            if (dataReturned.get("code").equals("04d") || dataReturned.get("code").equals("04n")) {  //broken clouds - nublado
                setImage(R.raw.cloud);
            }
            if (dataReturned.get("code").equals("09d") || dataReturned.get("code").equals("09n")) {  //shower rain - llovizna
                setImage(R.raw.dizzle);
            }
            if (dataReturned.get("code").equals("10d") || dataReturned.get("code").equals("10n")) {  //rain - lluvia
                setImage(R.raw.rain);
            }
            if (dataReturned.get("code").equals("11d") || dataReturned.get("code").equals("11n")) {  //thunderstorm - tormenta
                setImage(R.raw.storm);
            }
            if (dataReturned.get("code").equals("13d") || dataReturned.get("code").equals("13n")) {  //snow - nieve
                setImage(R.raw.snow);
            }
            if (dataReturned.get("code").equals("50d") || dataReturned.get("code").equals("50n")) {  //mist - neblina
                setImage(R.raw.fog);
            }
            layout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError() {
            Log.d("Icaro Weather", "Failed to access API Service");
        }

        private void speak(String text) {
            LocalStorage.initLocalStorage(getActivity());
            if (LocalStorage.getAllowVoiceScreen()) {
                Icaro.speaker.pause(Icaro.SHORT_DURATION);
                Icaro.speaker.speak(text);

            }
        }
    }
}
