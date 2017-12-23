package com.carroll.michael.linkbus;

import android.support.v4.widget.SwipeRefreshLayout;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity {

    public final String scheduleURL = "https://apps.csbsju.edu/busschedule/";
    public boolean goreckiToSexton, sextonToGorecki, eastToSexton, goreckiToAlcuin, alcuinToGorecki = false;
    public ArrayList<String> gts, stg, ets, gta, atg;
    public TextView gtsDisplay, stgDisplay, etsDisplay, gtaDisplay, atgDisplay;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //calls refreshContent method
                refreshContent();
            }
        });

        // calls main backend method to compute and display initial listings
        backend();

    }

    public void backend() {
        // Network operations done on separate thread to prevent UI unresponsiveness
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(scheduleURL);

                    //Reads all text returned by server
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    // Initializes ArrayList objects for each bus schedule
                    gts = new ArrayList<String>(); // Gorecki to Sexton
                    stg = new ArrayList<String>(); // Sexton to Gorecki
                    ets = new ArrayList<String>(); // CSB East to Gorecki/Sexton
                    gta = new ArrayList<String>(); // Gorecki to Alcuin
                    atg = new ArrayList<String>(); // Alcuin to Gorecki

                    // reads HTML of URL line by line
                    while ((str = in.readLine()) != null) {
                        // checks if a line of HTML contains "Gorecki to Sexton"
                        if (str.contains("Gorecki to Sexton")) {
                            System.out.println(str);
                            goreckiToSexton = true;
                        }

                        // Gets bus times for Gorecki to Sexton and adds them to gts ArrayList
                        if ((str.contains("PM") || (str.contains("AM"))) && (goreckiToSexton == true) && (sextonToGorecki == false)) {
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            }
                            else {
                                time = time + " AM";
                            }
                            gts.add(time);
                        }

                        if (str.contains("Sexton to Gorecki")) {
                            System.out.println(str);
                            sextonToGorecki = true;
                        }

                        // Gets bus times for Sexton to Gorecki and adds them to stg ArrayList
                        if ((str.contains("PM") || (str.contains("AM"))) && (sextonToGorecki == true) && (eastToSexton == false)) {
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            }
                            else {
                                time = time + " AM";
                            }
                            stg.add(time);
                        }

                        if (str.contains("CSB East to Gorecki/Sexton")) {
                            System.out.println(str);
                            eastToSexton = true;
                        }

                        // Gets bus times for CSB East to Gorecki/Sexton and adds them to stg ArrayList
                        if ((str.contains("PM") || (str.contains("AM"))) && (eastToSexton == true) && (goreckiToAlcuin == false)) {
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            }
                            else {
                                time = time + " AM";
                            }
                            ets.add(time);
                        }

                        if (str.contains("Gorecki to Alcuin")) {
                            System.out.println(str);
                            goreckiToAlcuin = true;
                        }

                        // Gets bus times for Gorecki to Alcuin and adds them to gta ArrayList
                        if ((str.contains("PM") || (str.contains("AM"))) && (goreckiToAlcuin == true) && (alcuinToGorecki == false)) {
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            }
                            else {
                                time = time + " AM";
                            }
                            gta.add(time);
                        }

                        if (str.contains("Alcuin to Gorecki")) {
                            System.out.println(str);
                            alcuinToGorecki = true;
                        }

                        // Gets bus times for Alcuin to Gorecki and adds them to atg ArrayList
                        if ((str.contains("PM") || (str.contains("AM"))) && (alcuinToGorecki == true)){
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            }
                            else {
                                time = time + " AM";
                            }
                            // Ensures Alcuin to Gorecki has the same number of times as Gorecki to Alcuin
                            // Also makes sure program doesn't drift into Crossroads/Cash Wise bus times on the weekend days
                            if (atg.size() < gta.size()) {
                                atg.add(time);
                            }
                        }

                    }
                    in.close();
                    System.out.println("Gorecki to Sexton " + gts); // temporary readout of gts array
                    System.out.println("Sexton to Gorecki " + stg); // temporary readout of stg array
                    System.out.println("CSB East to Gorecki/Sexton " + ets); // temporary readout of ets array
                    System.out.println("Gorecki to Alcuin " + gta); // temporary readout of gta array
                    System.out.println("Alcuin to Gorecki " + atg); // temporary readout of atg array

                    generateUI();

                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }
            }
        }).start();


    }

    public void generateUI()
    {
        // Runs UI updating on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Gorecki to Sexton
                TextView gtsDisplay;
                gtsDisplay = findViewById(R.id.textView14);

                for (String element : gts) {
                    String temp = (String) gtsDisplay.getText();
                    temp = (temp + "\n" + element);
                    gtsDisplay.setText(temp);
                }

                // Sexton to Gorecki
                TextView stgDisplay;
                stgDisplay = findViewById(R.id.textView16);

                for (String element : stg) {
                    String temp = (String) stgDisplay.getText();
                    temp = (temp + "\n" + element);
                    stgDisplay.setText(temp);
                }

                // CSB East to Gorecki/Sexton
                TextView etsDisplay;
                etsDisplay = findViewById(R.id.textView18);

                for (String element : ets) {
                    String temp = (String) etsDisplay.getText();
                    temp = (temp + "\n" + element);
                    etsDisplay.setText(temp);
                }

                // Gorecki to Alcuin
                TextView gtaDisplay;
                gtaDisplay = findViewById(R.id.textView20);

                for (String element : gta) {
                    String temp = (String) gtaDisplay.getText();
                    temp = (temp + "\n" + element);
                    gtaDisplay.setText(temp);
                }

                // Gorecki to Alcuin
                TextView atgDisplay;
                atgDisplay = findViewById(R.id.textView22);

                for (String element : atg) {
                    String temp = (String) atgDisplay.getText();
                    temp = (temp + "\n" + element);
                    atgDisplay.setText(temp);
                }

                //sample.setText((CharSequence) gts.get(0));

                // sets up graphical user interface
                //ImageView image;

                //image = (ImageView) findViewById(R.id.sextonImageView);
                //image.setImageResource(R.drawable.sexton);

            }
        });
    }

    public void resetGUI()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // resets booleans to false for next run
                goreckiToSexton = false;
                sextonToGorecki = false;
                eastToSexton = false;
                goreckiToAlcuin = false;
                alcuinToGorecki = false;

                // sets display variables to proper textView ID -- later use global variables to do this once
                gtsDisplay = findViewById(R.id.textView14);
                stgDisplay = findViewById(R.id.textView16);
                etsDisplay = findViewById(R.id.textView18);
                gtaDisplay = findViewById(R.id.textView20);
                atgDisplay = findViewById(R.id.textView22);

                // sets TextView text blank
                gtsDisplay.setText("");
                stgDisplay.setText("");
                etsDisplay.setText("");
                gtaDisplay.setText("");
                atgDisplay.setText("");
            }
        });
    }

    public void refreshContent()
    {
        resetGUI();
        backend();
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
