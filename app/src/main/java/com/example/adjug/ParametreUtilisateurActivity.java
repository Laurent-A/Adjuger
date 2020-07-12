package com.example.adjug;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ParametreUtilisateurActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner1;
    private Button btnSubmit;
    private TextView valeurEnchereChoix;
    private String pseudoActif;
    private Switch switch1;
    private CharSequence charSequenceValeur;
    //private SharedPreferences preferences= getPreferences(MODE_PRIVATE) ;
    public static final String PREFS_NAME = "MonFichierEnchere";
    private TextView textView4;
    private Button readButton;
    private Button goToButton;
    private Integer plafondFinal;
    private SeekBar seekBar;
    private TextView enchereView;
    private Button buttonDecrease;
    private Button buttonIncrease;
    private static int DELTA_VALUE = 5;
    int progress = 0;

    private static final String LOGTAG = "SeekBarDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parametre_utilisateur);
        this.readButton = (Button) this.findViewById(R.id.button_read);
        this.textView4 = (TextView) this.findViewById(R.id.textView4);

        addItemsOnSpinner1();
        addItemSwitch();
        seekbarPlay();
        onEnregistre();

        this.readButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                readData();
            }
        });
        goToEnchere();
    }

    // SWITCH
    private void addItemSwitch(){
        switch1 = (Switch) findViewById(R.id.switch1);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    valeurEnchereChoix = findViewById(R.id.textView3);
                    valeurEnchereChoix.setText("YOLO !");
                    spinner1.setEnabled(false);
                } else {
                    valeurEnchereChoix.setText(charSequenceValeur.toString());
                    spinner1.setEnabled(true);
                }
            }
        });
    }

    // SPINNER
    private void addItemsOnSpinner1() {
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.valeur_enchere, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        charSequenceValeur = (CharSequence) parent.getItemAtPosition(pos);
        valeurEnchereChoix = findViewById(R.id.textView3);
        valeurEnchereChoix.setText(charSequenceValeur.toString());
        System.out.println("item : " + charSequenceValeur.toString());
    }

    public void onNothingSelected(AdapterView<?> parent) {
        valeurEnchereChoix = findViewById(R.id.textView3);
        valeurEnchereChoix.setText("valeur");
    }

    // BUTTON ENREGISTRER PERIMETRE
    public void onEnregistre(){
        final Button button = findViewById(R.id.btnSubmit);
        final EditText pseudo = (EditText) findViewById(R.id.pseudo);
        final SharedPreferences preferences=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pseudoActif = pseudo.getText().toString();
                if(pseudoActif == ""){
                    button.setEnabled(false);
                } else {
                    Toast.makeText(getApplicationContext(), "paramètres enregistrés", Toast.LENGTH_SHORT).show();
                    try {
                        saveInterne();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveInterne() throws IOException {
        String eol = System.getProperty("line.separator");
        FileOutputStream out = this.openFileOutput(PREFS_NAME, MODE_PRIVATE);
        plafondEnchere();
        try {
        String pseudo=pseudoActif + "/";
        String enchere=progress+ "/";
        String plafondResultat=plafondFinal+"";
        out.write(pseudo.getBytes());
        out.write(enchere.getBytes());
        out.write(plafondResultat.getBytes());
        out.close();
        } catch (Exception e) {
            Toast.makeText(this,"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    // LECTURE DES DONNEES
    private void readData() {
        try {
            // Open stream to read file.
            FileInputStream in = this.openFileInput(PREFS_NAME);

            BufferedReader br= new BufferedReader(new InputStreamReader(in));

            StringBuilder sb= new StringBuilder();
            String s= null;
            while((s= br.readLine())!= null)  {
                sb.append(s).append("\n");
            }
            textView4.setText(sb);

        } catch (Exception e) {
            Toast.makeText(this,"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void goToEnchere(){
        this.goToButton= (Button) this.findViewById(R.id.button_Enchere);
        this.goToButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent salleEnchere = new Intent(ParametreUtilisateurActivity.this, EnchereActivity.class);
                startActivity(salleEnchere);
            }
        });
    }

    private void plafondEnchere(){
        String plafond = charSequenceValeur.toString();
        switch(plafond) {
            case "Valeur":
                plafondFinal = 1;
                break;
            case "X2":
                plafondFinal = 2;
                break;
            case "X3":
                plafondFinal = 3;
                break;
            case "X4":
                plafondFinal = 4;
                break;
            case "X5":
                plafondFinal = 5;
                break;
            default:
                // code block
        }
    }

    // SEEKBAR
    private void seekbarPlay(){
        this.seekBar = (SeekBar) findViewById(R.id.seekBar );
        this.enchereView = (TextView) findViewById(R.id.enchereView);

        this.buttonDecrease= (Button) findViewById(R.id.button_decrease);
        this.buttonIncrease= (Button) findViewById(R.id.button_increase);

        this.seekBar.setMax(100);
        this.seekBar.setProgress(15);

        this.enchereView.setText("Progress: " + seekBar.getProgress() + " %");
        //
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            // When Progress value changed.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                enchereView.setText("Enchere: " + progressValue + " %");
            }

            // Notification that the user has started a touch gesture.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // Notification that the user has finished a touch gesture
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                enchereView.setText("Enchere: " + progress + " %");
            }
        });

        this.buttonDecrease.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                decreateProgress();
            }
        });

        this.buttonIncrease.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                increateProgress();
            }
        });
    }

        private void decreateProgress()  {
            int progress= this.seekBar.getProgress();
            if(progress - DELTA_VALUE < 0)  {
                this.seekBar.setProgress(0);
            } else  {
                this.seekBar.setProgress(progress - DELTA_VALUE);
            }
            enchereView.setText("Progress: " + seekBar.getProgress()+ "%");
        }

        private void increateProgress()  {
            int progress= this.seekBar.getProgress();
            if(progress + DELTA_VALUE > this.seekBar.getMax())  {
                this.seekBar.setProgress(0);
            }else {
                this.seekBar.setProgress(progress + DELTA_VALUE);
            }
            enchereView.setText("Progress: " + seekBar.getProgress()+ "%");
        }

}