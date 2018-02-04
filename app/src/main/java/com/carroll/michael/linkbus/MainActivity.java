package com.carroll.michael.linkbus;

import android.animation.Animator;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;

// snackBar URL
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    public final String remoteMessageURL = "https://raw.githubusercontent.com/michaelcarroll/linkbus/master/config.txt";

    public boolean goreckiToSexton, sextonToGorecki, eastToSexton, goreckiToAlcuin, alcuinToGorecki = false;
    public ArrayList<String> gts, stg, ets, gta, atg;
    public TextView gtsTextView, stgTextView, etsTextView, gtaTextView, atgTextView;
    public TextView nextBusGtsTextView, nextBusStgTextView, nextBusEtsTextView, nextBusGtaTextView, nextBusAtgTextView;
    CardView gtsCard, gtaCard, etsCard, stgCard, atgCard;

    public CoordinatorLayout coordinatorLayout;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    private Calendar targetDate;
    private Calendar currentTime = new GregorianCalendar();
    public String scheduleURL;


    private Spinner spinner;

    private String[] daysBackend;
    private String[] daysFrontend;

    // _____________________________________________________________________________________________

    // snackbar message protocol
    public Snackbar snackbarMessage;

    public boolean snackbarToggle;
    public String snackbarOutput;

    public boolean snackbarActionToggle;
    public String snackbarURL;
    public String snackbarActionText;

    // _____________________________________________________________________________________________

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        scheduleURL = "https://apps.csbsju.edu/BusSchedule/";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        dayController();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(4.0f);

        Spinner spinner = findViewById(R.id.dayChooser);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                R.layout.spinner_layout, daysFrontend);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
                    daySelector(position);
                }


            @Override
            public void onNothingSelected(AdapterView<?> parent){
                // do nothing
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Toast.makeText(MainActivity.this, "Coming soon...",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_refresh) {
            refreshContent();
        }

        return super.onOptionsItemSelected(item);
    }

    public void dayController() {
        Calendar dayAdvance = new GregorianCalendar();
        SimpleDateFormat format1 = new SimpleDateFormat("M/d/yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("MMMM d, yyyy");
        daysBackend = new String[7];
        daysBackend[0] = "Today";
        daysFrontend = new String[7];
        daysFrontend[0] = "Today";
        for (int i = 1; i <= 6; i++) {
            dayAdvance.add(Calendar.DATE, 1);
            daysBackend[i] = format1.format(dayAdvance.getTime());
            daysFrontend[i] = format2.format(dayAdvance.getTime());
        }
    }

    public void daySelector(int position){
        switch (position) {
            case 0:
                scheduleURL = "https://apps.csbsju.edu/busschedule";
                targetDate = Calendar.getInstance(); // sets to today's date
                if (gtsCard != null)
                    refreshContent();
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                SimpleDateFormat format1 = new SimpleDateFormat("M/d/yyyy");

                scheduleURL = "https://apps.csbsju.edu/busschedule/?date=" + daysBackend[position];
                targetDate = Calendar.getInstance(); // sets to today's date
                targetDate.add(Calendar.DATE, position); // advances target date
                refreshContent();
                break;
        }
    }

    public void backend() {
        // Network operations done on separate thread to prevent UI unresponsiveness
        new Thread(new Runnable() {
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
                            } else {
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
                            } else {
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
                            } else {
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
                            } else {
                                time = time + " AM";
                            }
                            gta.add(time);
                        }

                        if (str.contains("Alcuin to Gorecki")) {
                            System.out.println(str);
                            alcuinToGorecki = true;
                        }

                        // Gets bus times for Alcuin to Gorecki and adds them to atg ArrayList
                        if ((str.contains("PM") || (str.contains("AM"))) && (alcuinToGorecki == true)) {
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            } else {
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

                    url = new URL(remoteMessageURL);

                    //Reads all text returned by server
                    in = new BufferedReader(new InputStreamReader(url.openStream()));
                    str = null;

                    //snackBar:

                    // reads HTML of URL line by line
                    while ((str = in.readLine()) != null) {

                        if (str.contains("snackbar=")) {
                            str = str.split("=")[1]; // sets str to text after colon
                            if (str.contains("enabled")) {
                                snackbarToggle = true;
                                snackbarOutput = in.readLine().split("=")[1]; // reads text after "snackbarOutput:" in next line of file
                            } else {
                                snackbarToggle = false;
                                break;
                            }
                        }

                        if (str.contains("snackbarAction=")) {
                            str = str.split("=")[1];
                            if (str.contains("enabled")) {
                                snackbarActionToggle = true;
                                snackbarURL = in.readLine().split("=")[1];
                                snackbarActionText = in.readLine().split("=")[1];
                            } else
                                snackbarActionToggle = false;
                        }

                        if (str.contains("end")) // reached end of configuration steps
                            break;
                    }

                    generateUI();

                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    Snackbar.make(coordinatorLayout, "Network error: Cannot connect to CSB/SJU servers", Snackbar.LENGTH_LONG).show();
                }

            }
        }).start();


    }

    public void generateUI() {
        // Runs UI updating on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Gorecki to Sexton
                gtsTextView = findViewById(R.id.gtsTextView);
                for (String element : gts) {
                    String temp = (String) gtsTextView.getText();
                    temp = (temp + "\n" + element);
                    gtsTextView.setText(temp);
                }

                // Sexton to Gorecki
                stgTextView = findViewById(R.id.stgTextView);
                for (String element : stg) {
                    String temp = (String) stgTextView.getText();
                    temp = (temp + "\n" + element);
                    stgTextView.setText(temp);
                }

                // CSB East to Gorecki/Sexton
                etsTextView = findViewById(R.id.etsTextView);

                for (String element : ets) {
                    String temp = (String) etsTextView.getText();
                    temp = (temp + "\n" + element);
                    etsTextView.setText(temp);
                }

                //Gorecki to Alcuin
                gtaTextView = findViewById(R.id.gtaTextView);

                for (String element : gta) {
                    String temp = (String) gtaTextView.getText();
                    temp = (temp + "\n" + element);
                    gtaTextView.setText(temp);
                }

                //Gorecki to Alcuin
                atgTextView = findViewById(R.id.atgTextView);

                for (String element : atg) {
                    String temp = (String) atgTextView.getText();
                    temp = (temp + "\n" + element);
                    atgTextView.setText(temp);
                }

                // onClickListeners for cards...in future make card it's own class to prevent duplicates of all these methods

                gtsCard = findViewById(R.id.gtsCard);
                gtsCard.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCloseCard(v, gtsTextView, gtsCard);
                    }
                });

                stgCard = findViewById(R.id.stgCard);
                stgCard.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCloseCard(v, stgTextView, stgCard);
                    }
                });

                etsCard = findViewById(R.id.etsCard);
                etsCard.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCloseCard(v, etsTextView, etsCard);
                    }
                });

                gtaCard = findViewById(R.id.gtaCard);
                gtaCard.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCloseCard(v, gtaTextView, gtaCard);
                    }
                });

                atgCard = findViewById(R.id.atgCard);
                atgCard.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openCloseCard(v, atgTextView, atgCard);
                    }
                });

                nextBusGtsTextView = findViewById(R.id.nextBusGtsTextView);
                if (goreckiToSexton) {
                    nextBusGtsTextView.setText(calculateBusTime(gts));
                    revealAnimation(gtsCard);
                }

                nextBusStgTextView = findViewById(R.id.nextBusStgTextView);
                if (sextonToGorecki) {
                    nextBusStgTextView.setText(calculateBusTime(stg));
                    revealAnimation(stgCard);
                }

                nextBusEtsTextView = findViewById(R.id.nextBusEtsTextView);
                if (eastToSexton) {
                    nextBusEtsTextView.setText(calculateBusTime(ets));
                    revealAnimation(etsCard);
                }

                nextBusGtaTextView = findViewById(R.id.nextBusGtaTextView);
                if (goreckiToAlcuin) {
                    nextBusGtaTextView.setText(calculateBusTime(gta));
                    revealAnimation(gtaCard);
                }

                nextBusAtgTextView = findViewById(R.id.nextBusAtgTextView);
                if (alcuinToGorecki) {
                    nextBusAtgTextView.setText(calculateBusTime(atg));
                    revealAnimation(atgCard);
                }

                if ((!goreckiToSexton) && (!sextonToGorecki) && (!eastToSexton) && (!goreckiToAlcuin) && (!alcuinToGorecki)) {
                    // if there are no buses, snackbar message is displayed
                    Snackbar.make(coordinatorLayout, "No buses scheduled for today :(", Snackbar.LENGTH_LONG).show();
                }

                if (snackbarToggle)
                    if (!snackbarActionToggle)
                        Snackbar.make(coordinatorLayout, snackbarOutput, Snackbar.LENGTH_LONG).show();
                    else {
                        Snackbar.make(coordinatorLayout, snackbarOutput, Snackbar.LENGTH_LONG)
                                .setAction(snackbarActionText, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent openWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse(snackbarURL));
                                        startActivity(openWebsite);
                                    }
                                })
                                .show();
                    }
            }
        });
    }

    public void resetGUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gtsCard.setVisibility(View.GONE);
                stgCard.setVisibility(View.GONE);
                etsCard.setVisibility(View.GONE);
                gtaCard.setVisibility(View.GONE);
                atgCard.setVisibility(View.GONE);

                // resets booleans to false for next run
                goreckiToSexton = false;
                sextonToGorecki = false;
                eastToSexton = false;
                goreckiToAlcuin = false;
                alcuinToGorecki = false;

/*                // sets display variables to proper textView ID -- later use global variables to do this once
                gtsDisplay = findViewById(R.id.textView14);
                stgDisplay = findViewById(R.id.textView16);
                etsTextView = findViewById(R.id.etsTextView);
                gtaDisplay = findViewById(R.id.textView20);
                atgDisplay = findViewById(R.id.textView22);*/

                // sets TextView text blank
                gtsTextView.setText("");
                nextBusGtsTextView.setText("");

                stgTextView.setText("");
                nextBusStgTextView.setText("");

                etsTextView.setText("");
                nextBusGtaTextView.setText("");

                gtaTextView.setText("");
                nextBusAtgTextView.setText("");

                atgTextView.setText("");
                nextBusGtaTextView.setText("");

                snackbarToggle = false;
            }
        });
    }

    public void openCloseCard(View v, TextView textView, CardView cardView)
    {
        if (textView.getVisibility() == View.GONE) {
            //TransitionManager.beginDelayedTransition(cardView);
            textView.setVisibility(View.VISIBLE);
        }
        else
            //TransitionManager.beginDelayedTransition(cardView);
            textView.setVisibility(View.GONE);
    }

    public void refreshContent()
    {
        resetGUI();
        backend();
        System.out.println(scheduleURL);

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void revealAnimation(CardView card){
        // previously invisible view
        View myView = card;

        // get the center for the clipping circle
        int cx = myView.getWidth();
        int cy = myView.getHeight();

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }

    public String calculateBusTime(ArrayList<String> route) {
        String nextTime = route.get(0);
        currentTime = Calendar.getInstance();
        System.err.println("next bus arrives at " + route + " at:" + nextTime);

        // for dealing with bus times with a range
        if (nextTime.contains("-")){
            String firstTime = nextTime.split("-")[0];
            int startTimeHour = Integer.parseInt(firstTime.split(":")[0].replaceAll("[^0-9]", ""));
            int startTimeMin = Integer.parseInt(firstTime.split(":")[1].replaceAll("[^0-9]", ""));

            System.out.println("start range hour: " + startTimeHour);
            System.out.println("start range min: " + startTimeMin);


            String secondTime = nextTime.split("-")[1];
            int finishTimeHour = Integer.parseInt(secondTime.split(":")[0].replaceAll("[^0-9]", ""));
            int finishTimeMin = Integer.parseInt(secondTime.split(":")[1].replaceAll("[^0-9]", ""));

            System.out.println("end range hour: " + finishTimeHour);
            System.out.println("end range min: " + finishTimeMin);

            Calendar arrival = new GregorianCalendar();
            arrival = targetDate;
            arrival.set(Calendar.HOUR, startTimeHour);
            arrival.set(Calendar.MINUTE, startTimeMin);
            arrival.set(Calendar.SECOND, 0);

            long differenceInMillis = arrival.getTimeInMillis() - currentTime.getTimeInMillis();

            if (firstTime.contains("AM"))
                arrival.set(Calendar.AM_PM, Calendar.AM);
            else
                arrival.set(Calendar.AM_PM, Calendar.PM);

            Calendar departure = new GregorianCalendar();
            departure = targetDate;
            departure.set(Calendar.HOUR, finishTimeHour);
            departure.set(Calendar.MINUTE, finishTimeMin);
            departure.set(Calendar.SECOND, 0);

            System.out.println("LOOK EHEREEE: " + arrival.getTime());

            System.out.println("current time: " + currentTime.getTime());
            System.out.println("next bus time range start: " + arrival.getTime());

            if (secondTime.contains("AM"))
                departure.set(Calendar.AM_PM, Calendar.AM);
            else
                departure.set(Calendar.AM_PM, Calendar.PM);

            System.out.println(differenceInMillis <= 0);

            if (differenceInMillis < 0){ // bus has arrived and is in pickup range
                differenceInMillis = departure.getTimeInMillis() - currentTime.getTimeInMillis();
                int minutesUntilDeparture = (int)(differenceInMillis / 1000 / 60) + 1;
                return ("Busses running. Last bus departs in " + minutesUntilDeparture + " min.");
            }
            // ______
            else { // bus has not arrived yet
                arrival = targetDate;
                arrival.set(Calendar.HOUR, startTimeHour);
                arrival.set(Calendar.MINUTE, startTimeMin);
                arrival.set(Calendar.SECOND, 0); // re-sets data to arrival calendar since it was corrupt after making departure calendar... java bug?

                differenceInMillis = arrival.getTimeInMillis() - currentTime.getTimeInMillis();

                int minutesUntilArrival = (int)(differenceInMillis / 1000 / 60) + 1; // minutes
                int hoursUntilArrival = (int)differenceInMillis / 1000 / 60 / 60; // hours
                int daysUntilArrival = (int)differenceInMillis / 1000 / 60 / 60 / 24; // days


                if (daysUntilArrival > 1) {
                    if ((hoursUntilArrival - (24 * daysUntilArrival)) == 0)
                        return ("Next bus arrives in " + daysUntilArrival + " days " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                    else
                        return ("Next bus arrives in " + daysUntilArrival + " days " + (hoursUntilArrival - (24 * daysUntilArrival)) + " hr " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                }

                else if (daysUntilArrival == 1) {
                    if ((hoursUntilArrival - (24 * daysUntilArrival)) == 0)
                        return ("Next bus arrives in " + daysUntilArrival + " day " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                    else
                        return ("Next bus arrives in " + daysUntilArrival + " day " + (hoursUntilArrival - (24 * daysUntilArrival)) + " hr " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                }

                else if (hoursUntilArrival >= 1) {
                    if ((minutesUntilArrival - (60 * hoursUntilArrival)) == 0)
                        return ("Next bus arrives in " + hoursUntilArrival + " hr");
                    else
                        return ("Next bus arrives in " + hoursUntilArrival + " hr " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                }
                else
                    return ("Next bus arrives in " + minutesUntilArrival + " min.");
            }
        }
        else { // next bus time is not a range of times
            int arrivalTimeHour = Integer.parseInt(nextTime.split(":")[0].replaceAll("[^0-9]", ""));
            int arrivalTimeMin = Integer.parseInt(nextTime.split(":")[1].replaceAll("[^0-9]", ""));

            Calendar arrival = new GregorianCalendar();
            arrival = targetDate;
            arrival.set(Calendar.HOUR, arrivalTimeHour);
            arrival.set(Calendar.MINUTE, arrivalTimeMin);
            arrival.set(Calendar.SECOND, 0);

            if (nextTime.contains("AM")) {
                arrival.set(Calendar.AM_PM, Calendar.AM);
                if (arrivalTimeHour == 12)
                    arrival.set(Calendar.HOUR, 0);
            }
            else
                arrival.set(Calendar.AM_PM, Calendar.PM);

            System.out.println("current time: " + currentTime.getTime());
            System.out.println("next bus time: " + arrival.getTime());

            long differenceInMillis = arrival.getTimeInMillis() - currentTime.getTimeInMillis();
            System.err.println("difference: " + differenceInMillis);

            int minutesUntilArrival = (int)(differenceInMillis / 1000 / 60) + 1; // minutes
            int hoursUntilArrival = (int)differenceInMillis / 1000 / 60 / 60; // hours
            int daysUntilArrival = (int)differenceInMillis / 1000 / 60 / 60 / 24; // days


            System.err.println(minutesUntilArrival);
            System.err.println(hoursUntilArrival);
            System.err.println(daysUntilArrival);

            System.out.println("currentTime PM: " + ((currentTime.get(Calendar.AM_PM) == Calendar.PM)));
            System.out.println("currentTime AM: " + ((currentTime.get(Calendar.AM_PM) == Calendar.AM)));
            System.out.println("arrival PM: " + ((arrival.get(Calendar.AM_PM) == Calendar.PM)));
            System.out.println("arrival AM: " + ((arrival.get(Calendar.AM_PM) == Calendar.AM)));

            if ((currentTime.get(Calendar.AM_PM) == Calendar.PM) && (arrival.get(Calendar.AM_PM) == Calendar.AM))
                arrival.add(Calendar.DATE, 1); // advances target date

            if (daysUntilArrival > 1) {
                if ((hoursUntilArrival - (24 * daysUntilArrival)) == 0)
                    return ("Next bus arrives in " + daysUntilArrival + " days " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                else
                    return ("Next bus arrives in " + daysUntilArrival + " days " + (hoursUntilArrival - (24 * daysUntilArrival)) + " hr " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
            }

            else if (daysUntilArrival == 1) {
                if ((hoursUntilArrival - (24 * daysUntilArrival)) == 0)
                    return ("Next bus arrives in " + daysUntilArrival + " day " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
                 else
                    return ("Next bus arrives in " + daysUntilArrival + " day " + (hoursUntilArrival - (24 * daysUntilArrival)) + " hr " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
            }

            else if (hoursUntilArrival >= 1) {
                if ((minutesUntilArrival - (60 * hoursUntilArrival)) == 0)
                    return ("Next bus arrives in " + hoursUntilArrival + " hr");
                else
                    return ("Next bus arrives in " + hoursUntilArrival + " hr " + (minutesUntilArrival - (60 * hoursUntilArrival)) + " min.");
            }
            else
                return ("Next bus arrives in " + minutesUntilArrival + " min.");
            }
        }
    }


