package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {
    long lastClickTimeStamp;
    String lastClickBtn = "";
    boolean status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {
        int id = v.getId();
        long current = System.currentTimeMillis();
        WeatherTask w = new WeatherTask();
        switch (id) {
            case R.id.btBangkok:
                if (!lastClickBtn.equals("bkk") || (current - lastClickTimeStamp > 60000)) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    Log.d("tag", String.valueOf(current - lastClickTimeStamp));
                    lastClickTimeStamp = current;
                    lastClickBtn = "bkk";
                }

                break;

            //Piyapan//
            case R.id.btNon:
                if (!lastClickBtn.equals("non") || current - lastClickTimeStamp > 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    lastClickTimeStamp = current;
                    lastClickBtn = "non";
                }

                break;

            case R.id.btPathum:
                if (!lastClickBtn.equals("pathum") || current - lastClickTimeStamp > 60000) {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    lastClickTimeStamp = current;
                    lastClickBtn = "pathum";
                }
                break;
            }
        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;

        String Weather;
        double temp;
        double tempMin;
        double tempMax;
        int humid;
        double windSpeed;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    //Piyapan
                    windSpeed = jWind.getDouble("speed");
                    Weather = jWeather.getJSONArray("weather").getJSONObject(0).getString("main");
                    temp = jWeather.getJSONObject("main").getDouble("temp");
                    tempMin = jWeather.getJSONObject("main").getDouble("temp_min");
                    tempMax = jWeather.getJSONObject("main").getDouble("temp_max");
                    temp = temp - 273.15;
                    tempMin = tempMin - 273.15;
                    tempMax = tempMax - 273.15;
                    humid = jWeather.getJSONObject("main").getInt("humidity");
                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind , tvTemp , tvHumid;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);

            if (result) {
                tvTitle.setText(title);
                tvWeather.setText(Weather);
                tvTemp.setText(String.format("%.2f",temp)+"(max = " + Double.toString(tempMax) +" ,min = " + Double.toString(tempMin) + ")");
                tvWind.setText(String.format("%.1f", windSpeed));
                tvHumid.setText(Integer.toString(humid));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}
