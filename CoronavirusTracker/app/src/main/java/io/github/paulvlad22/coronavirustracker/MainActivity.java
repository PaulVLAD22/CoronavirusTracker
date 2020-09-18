package io.github.paulvlad22.coronavirustracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.*;

import com.squareup.okhttp.*;


public class MainActivity extends AppCompatActivity {
    static Response responseAux;// static so it can be accesed in both threads

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void getApiInfo(View v){
        EditText editTextCountry =findViewById(R.id.countryText);
        String country = editTextCountry.getText().toString(); // capital letters don't matter
        //got the country entered
        System.out.println(country);

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("https://covid-193.p.rapidapi.com/statistics?country="+country)
                .get()
                .addHeader("x-rapidapi-host", "covid-193.p.rapidapi.com")
                .addHeader("x-rapidapi-key", ApiKey.apiKey)
                .build();

        // Making a thread to get the api info
        try {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        responseAux = client.newCall(request).execute();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Connection to API failed",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
            try{
                thread.join();
            }catch (Exception e){
                e.printStackTrace();
            }

            // wait for the request thread to end /-> return to the main thread
            displayInfo(responseAux);

        } catch (Exception e) {
            e.printStackTrace();
        }




    }
    public void displayInfo(Response response){
        //widgets
        TextView newCasesView = findViewById(R.id.newCasesView);
        TextView activeCasesView = findViewById(R.id.activeCasesView);
        TextView criticalCasesView =findViewById(R.id.criticalCasesView);
        TextView recoveredView = findViewById(R.id.recoveredView);

        try {
            //processing received data
            JSONObject jsonObject = new JSONObject(response.body().string());
            System.out.println(response.body().string());
            JSONArray jArrAux = jsonObject.getJSONArray("response");
            JSONObject infoObj = jArrAux.getJSONObject(0);
            System.out.println(infoObj);
            JSONObject casesInfo = infoObj.getJSONObject("cases");
            //starting displaying data into TextViews
            String newCases = casesInfo.get("new").toString().substring(1);
            String activeCases = casesInfo.get("active").toString();
            String criticalCases = casesInfo.get("critical").toString();
            String recoveredCases = casesInfo.get("recovered").toString();
            newCasesView.setText("New Cases(24h) : "+newCases);
            activeCasesView.setText("Active Cases : "+activeCases);
            criticalCasesView.setText("Critical Cases : "+criticalCases);
            recoveredView.setText("Recovered : "+recoveredCases);


        }catch (Exception e){
            Toast.makeText(MainActivity.this, "Wrong Country Name",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


}