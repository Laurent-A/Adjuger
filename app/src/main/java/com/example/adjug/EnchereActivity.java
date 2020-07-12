package com.example.adjug;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class EnchereActivity extends AppCompatActivity {
    private static final Object REQUEST_TAG = new Object();
    private static final String URL_PATTERN = "https://adjuge.herokuapp.com/enchere";
    private RequestQueue requestQueue;
    private TextView nomEnchereView;
    private TextView valeurPrixBaseView;
    private TextView valeurPrixActuelview;
    private TextView pseudoBest;
    private TextView dureeRestante;
    private Integer duree;
    private ImageView imageEnchere;
    public static final String PREFS_NAME = "MonFichierEnchere";
    private String monPseudo;
    private Integer tauxEnchere;
    private Integer plafondActif;
    private Float montantSiEnchere;
    private Float montantTotal;
    private Integer valeurPrixActuel;
    private Button encherir;
    private String enchereUrl;
    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enchere);
        nomEnchereView = findViewById(R.id.nomEnchere);
        valeurPrixBaseView = findViewById(R.id.valeurPrixBase);
        valeurPrixActuelview = findViewById((R.id.valeurPrixActuel));
        pseudoBest = findViewById(R.id.pseudoBest);
        dureeRestante= findViewById((R.id.valeurTempsRestant));
        imageEnchere=findViewById(R.id.imageEnchere);
        encherir=findViewById(R.id.boutonEncherir);
        requestQueue = Volley.newRequestQueue(this);
        sendRequest();
        readData();
        onEncherit();
        myHandler = new Handler();
        myHandler.postDelayed(myRunnable,500); // on redemande toute les 500ms
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(duree == 0){
            onCreate(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelRequest();
    }

    private void cancelRequest() {
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    //RECUPERATION DES DONNEE
    private void fillEnchere(JSONObject jsonObject) {
        try {
            String nomEnchere = jsonObject.getString("produit");
            Integer valeurPrixBase = jsonObject.getInt("prix_base");
            valeurPrixActuel = jsonObject.getInt("prix_actuel");
            duree = jsonObject.getInt("temps_restant");
            String pseudoBestAcheteur = jsonObject.getString("acheteur");
            String imageUrl = jsonObject.getString("url_image");
            enchereUrl = jsonObject.getString("url_enchere");

            String msgPrixBase  = String.format(valeurPrixBase.toString());
            String msgPrixActuel  = String.format(valeurPrixActuel.toString());
            String msg = String.format("%s", nomEnchere);
            String msgPseudoBestAcheteur = String.format("%s", pseudoBestAcheteur);
            String msgDuree = secondeToMinute(this.duree).toString();

            nomEnchereView.setText(msg);
            valeurPrixBaseView.setText(msgPrixBase + "€");
            valeurPrixActuelview.setText(msgPrixActuel + "€");
            pseudoBest.setText(msgPseudoBestAcheteur);
            dureeRestante.setText(msgDuree);
            Picasso.get().load(imageUrl).into(imageEnchere);
            calculEnchere();

        } catch (JSONException e) {
            nomEnchereView.setText("Erreur : " + e.getMessage());
        }
    }


    private void sendRequest() {
        cancelRequest();
        nomEnchereView.setText("Chargement...");
        pseudoBest.setText("Personne n'a enchérit");
        String url = String.format(URL_PATTERN);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        fillEnchere(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null
                        && error.networkResponse.statusCode == 404) {
                    nomEnchereView.setText("pas d'enchère trouvé désolé");
                } else {
                    nomEnchereView.setText("Erreur : " + error.getMessage());
                }
            }
        });
        request.setTag(REQUEST_TAG);
        requestQueue.add(request);

    }

    private void calculEnchere(){
        montantSiEnchere =(float) (valeurPrixActuel*tauxEnchere)/100;
        encherir.setText("Encherir de :" + montantSiEnchere.toString() + "€");
    }

    // MISE EN PLACE DU TIMER
    private Object secondeToMinute(Integer duree) {
        Integer minutes = ((duree % 3600) / 60);
        Integer secondes = ((duree % 3600) % 60);
        String msgDuree = String.format("%s min : %s sec", minutes, secondes);
        return msgDuree;
    }

    //RECUPERATION DONNEES FICHIER STORAGE
    private void readData() {
        try {
            // Open stream to read file.
            FileInputStream in = this.openFileInput(PREFS_NAME);

            BufferedReader br= new BufferedReader(new InputStreamReader(in));

            StringBuilder sb= new StringBuilder();
            String s= null;
            String data = null;
            while((s= br.readLine())!= null)  {
                data = sb.append(s).append("\n").toString();
            }
            String[] dataStorage = data.split("/");
            monPseudo = dataStorage[0];
            tauxEnchere = Integer.parseInt(dataStorage[1]);
            plafondActif = Integer.parseInt(dataStorage[2].trim());

            TimerTask task = new TimerTask() {
                public void run() {
                    calculEnchere();
                }

            };

            Timer timer = new Timer("Timer");
            long delay = 2000L;
            timer.schedule(task, delay);

        } catch (Exception e) {
            Toast.makeText(this,"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    // REQUETE ENVOI DONNEE:

    private void postRequest() throws JSONException {
        String url = enchereUrl;
        montantTotal = montantSiEnchere + (float)valeurPrixActuel;
        JSONObject requestPayload = new JSONObject();
        requestPayload.put("pseudo", monPseudo);
        requestPayload.put("prix", montantTotal);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestPayload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", String.valueOf(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //
            }
        });
        this.requestQueue.add(request);
    }

    public void onEncherit() {
        final Button button = findViewById(R.id.boutonEncherir);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    postRequest();
                    Toast.makeText(getApplicationContext(), "Enchere envoyée !", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Rien recu...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            sendRequest();

            myHandler.postDelayed(this,10000);
        }
    };

    public void onPause() {
        super.onPause();
        if(myHandler != null)
            myHandler.removeCallbacks(myRunnable);
    }
}