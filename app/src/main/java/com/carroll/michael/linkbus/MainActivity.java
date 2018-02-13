package com.carroll.michael.linkbus;

import android.animation.Animator;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

// snackBar URL
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private final String remoteMessageURL = "https://raw.githubusercontent.com/michaelcarroll/linkbus/master/config.txt";

    private boolean goreckiToSexton = false;
    private boolean sextonToGorecki = false;
    private boolean eastToSexton = false;
    private boolean goreckiToAlcuin = false;
    private boolean alcuinToGorecki = false;
    private ArrayList<String> gts;
    private ArrayList<String> stg;
    private ArrayList<String> ets;
    private ArrayList<String> gta;
    private ArrayList<String> atg;
    private TextView gtsTextView;
    private TextView stgTextView;
    private TextView etsTextView;
    private TextView gtaTextView;
    private TextView atgTextView;
    private TextView nextBusGtsTextView;
    private TextView nextBusStgTextView;
    private TextView nextBusEtsTextView;
    private TextView nextBusGtaTextView;
    private TextView nextBusAtgTextView;
    private CardView gtsCard;
    private CardView gtaCard;
    private CardView etsCard;
    private CardView stgCard;
    private CardView atgCard;

    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private DateTime targetDate;
    private String scheduleURL;


    private Spinner spinner;

    private String[] daysBackend;
    private String[] daysFrontend;

    // _____________________________________________________________________________________________

    // snackbar message protocol

    private boolean snackbarToggle;
    private String snackbarOutput;

    private boolean snackbarActionToggle;
    private String snackbarURL;
    private String snackbarActionText;

    // _____________________________________________________________________________________________

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        JodaTimeAndroid.init(this);

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
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                daySelector(position);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

    private void dayController() {
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

    private void daySelector(int position) {
        switch (position) {
            case 0:
                scheduleURL = "https://apps.csbsju.edu/busschedule";
                targetDate = DateTime.now(); // sets to today's date
                if (gtsCard != null)
                    refreshContent();
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                scheduleURL = "https://apps.csbsju.edu/busschedule/?date=" + daysBackend[position];
                targetDate = DateTime.now(); // sets to today's date
                targetDate = targetDate.plusDays(position);
                refreshContent();
                break;
        }
    }

    private void backend() {
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
                        if ((str.contains("PM") || (str.contains("AM"))) && (goreckiToSexton) && (!sextonToGorecki)) {
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
                        if ((str.contains("PM") || (str.contains("AM"))) && (sextonToGorecki) && (!eastToSexton)) {
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
                        if ((str.contains("PM") || (str.contains("AM"))) && (eastToSexton) && (!goreckiToAlcuin) && (!alcuinToGorecki)) {
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
                        if ((str.contains("PM") || (str.contains("AM"))) && (goreckiToAlcuin) && (!alcuinToGorecki)) {
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
                        if ((str.contains("PM") || (str.contains("AM"))) && (alcuinToGorecki)) {
                            String time = str;
                            // uses regex to delete non-numeric characters, except for ":"
                            time = time.replaceAll("[^\\d:*-]", "");
                            // adds PM or AM to bus time
                            if (str.contains("PM")) {
                                time = time + " PM";
                            } else {
                                time = time + " AM";
                            }
                            atg.add(time);
                        }

                        if (str.contains("Crossroads"))
                            break;
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

    private void generateUI() {
        // Runs UI updating on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

                gtsTextView = findViewById(R.id.gtsTextView);
                stgTextView = findViewById(R.id.stgTextView);
                etsTextView = findViewById(R.id.etsTextView);
                gtaTextView = findViewById(R.id.gtaTextView);
                atgTextView = findViewById(R.id.atgTextView);

                nextBusGtsTextView = findViewById(R.id.nextBusGtsTextView);
                nextBusStgTextView = findViewById(R.id.nextBusStgTextView);
                nextBusEtsTextView = findViewById(R.id.nextBusEtsTextView);
                nextBusGtaTextView = findViewById(R.id.nextBusGtaTextView);
                nextBusAtgTextView = findViewById(R.id.nextBusAtgTextView);


                // Gorecki to Sexton
                if (goreckiToSexton) {
                    for (String element : gts) {
                        String temp = (String) gtsTextView.getText();
                        temp = (temp + "\n" + element);
                        gtsTextView.setText(temp);
                    }
                    nextBusGtsTextView.setText(calculateBusTime(gts));
                    revealAnimation(gtsCard);
                }

                // Sexton to Gorecki
                if (sextonToGorecki) {
                    for (String element : stg) {
                        String temp = (String) stgTextView.getText();
                        temp = (temp + "\n" + element);
                        stgTextView.setText(temp);
                    }
                    nextBusStgTextView.setText(calculateBusTime(stg));
                    revealAnimation(stgCard);
                }

                // CSB East to Gorecki/Sexton
                if (eastToSexton) {
                    for (String element : ets) {
                        String temp = (String) etsTextView.getText();
                        temp = (temp + "\n" + element);
                        etsTextView.setText(temp);
                    }
                    nextBusEtsTextView.setText(calculateBusTime(ets));
                    revealAnimation(etsCard);
                }

                //Gorecki to Alcuin
                if (goreckiToAlcuin) {
                    for (String element : gta) {
                        String temp = (String) gtaTextView.getText();
                        temp = (temp + "\n" + element);
                        gtaTextView.setText(temp);
                    }
                    nextBusGtaTextView.setText(calculateBusTime(gta));
                    revealAnimation(gtaCard);
                }

                //Alcuin to Gorecki
                if (alcuinToGorecki) {
                    for (String element : atg) {
                        String temp = (String) atgTextView.getText();
                        temp = (temp + "\n" + element);
                        atgTextView.setText(temp);
                    }
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

    private void resetGUI() {
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

    private void openCloseCard(View v, TextView textView, CardView cardView) {
        if (textView.getVisibility() == View.GONE) {
            //TransitionManager.beginDelayedTransition(cardView);
            textView.setVisibility(View.VISIBLE);
        } else
            //TransitionManager.beginDelayedTransition(cardView);
            textView.setVisibility(View.GONE);
    }

    private void refreshContent() {
        resetGUI();
        backend();

        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void revealAnimation(CardView card) {
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

    private String calculateBusTime(ArrayList<String> route) {
        String nextTime = route.get(0);

        if (nextTime.contains("-")) { // if next bus time is a range of times
            String arrivalTime = nextTime.split("-")[0]; // "*7:30 "
            String departureTime = nextTime.split("-")[1]; // " 7:40 AM"

            // arrival time
            int arrivalTimeHour = Integer.parseInt((arrivalTime.split(":")[0].replaceAll("\\D+", ""))); // hour (7)
            // convert to 24-hour time
            if ((nextTime.contains("PM")) && (arrivalTimeHour != 12)) // if nextTime is after 12:59PM, add 12 to convert to 24hr time
                arrivalTimeHour += 12;
            if ((nextTime.contains("AM")) && (arrivalTimeHour == 12)) // if nextTime is between 12AM - 12:59AM, remove 12 to convert to 24hr time
                arrivalTimeHour -= 12;

            int arrivalTimeMinute = Integer.parseInt((arrivalTime.split(":")[1].replaceAll("\\D+", ""))); // minute (30)

            // departure time
            int departureTimeHour = Integer.parseInt((departureTime.split(":")[0].replaceAll("\\D+", ""))); // hour (7)
            // convert to 24-hour time
            if ((nextTime.contains("PM")) && (departureTimeHour != 12)) // if nextTime is after 12:59PM, add 12 to convert to 24hr time
                departureTimeHour += 12;
            if ((nextTime.contains("AM")) && (departureTimeHour == 12)) // if nextTime is between 12AM - 12:59AM, remove 12 to convert to 24hr time
                departureTimeHour -= 12;

            int departureTimeMinute = Integer.parseInt((departureTime.split(":")[1].replaceAll("\\D+", ""))); // minute (40)

            DateTime now = DateTime.now();

            DateTime nextBusArrival = new DateTime()
                    .withDayOfMonth(targetDate.getDayOfMonth())
                    .withHourOfDay(arrivalTimeHour)
                    .withMinuteOfHour(arrivalTimeMinute + 1) // adds one minute to next bus time to fix rounding issue during subtraction
                    .withSecondOfMinute(0);

            if ((now.hourOfDay().get() >= 12) && (nextTime.contains("AM"))) // if current time is pm and next bus arrives in am, nextBus advances by day to prevent negative time
            {
                nextBusArrival = nextBusArrival.plusDays(1);
            }

            DateTime nextBusDeparture = new DateTime()
                    .withDayOfMonth(targetDate.getDayOfMonth())
                    .withHourOfDay(departureTimeHour)
                    .withMinuteOfHour(departureTimeMinute + 1) // adds one minute to next bus time to fix rounding issue during subtraction
                    .withSecondOfMinute(0);

            if ((now.hourOfDay().get() >= 12) && (nextTime.contains("AM"))) // if current time is pm and next bus arrives in am, nextBus advances by day to prevent negative time
            {
                nextBusDeparture = nextBusDeparture.plusDays(1);
            }

            Duration duration = new Duration(now, nextBusArrival);
            Boolean hasArrived = false;

            System.out.println("duration - min :" + duration.getStandardMinutes());

            if (duration.getStandardMinutes() <= 0) {
                hasArrived = true;
                duration = new Duration(now, nextBusDeparture);
            }

            PeriodFormatter formatter = new PeriodFormatterBuilder() // formats time correctly for display
                    .appendWeeks().appendSuffix(" weeks ")
                    .appendDays().appendSuffix(" days ")
                    .appendHours().appendSuffix(" hr ")
                    .appendMinutes().appendSuffix(" min. ")
                    .printZeroNever()
                    .toFormatter();

            String formattedDuration = formatter.print(duration.toPeriodFrom(now));

            if (formattedDuration.length() > 0 && !hasArrived)
                return ("Next bus arrives in " + formattedDuration);
            else if (formattedDuration.length() > 0 && hasArrived)
                return ("Busses running. Last bus departs in " + formattedDuration);
        } else { // next bus time is singular

            int nextTimeHour = Integer.parseInt((nextTime.split(":")[0])); // hour
            // convert to 24-hour time
            if ((nextTime.contains("PM")) && (nextTimeHour != 12))
                nextTimeHour += 12;
            if ((nextTime.contains("AM")) && (nextTimeHour == 12)) // if nextTime is between 12AM - 12:59AM, remove 12 to convert to 24hr time
                nextTimeHour -= 12;

            int nextTimeMinute = Integer.parseInt((nextTime.split(":")[1].replaceAll("\\D+", ""))); // minute

            DateTime now = DateTime.now();

            DateTime nextBus = new DateTime()
                    .withDayOfMonth(targetDate.getDayOfMonth())
                    .withHourOfDay(nextTimeHour)
                    .withMinuteOfHour(nextTimeMinute + 1) // adds one minute to next bus time to fix rounding issue during subtraction
                    .withSecondOfMinute(0);

            if ((now.hourOfDay().get() >= 12) && (nextTime.contains("AM"))) // if current time is pm and next bus arrives in am, nextBus advances by day to prevent negative time
            {
                nextBus = nextBus.plusDays(1);
            }

            Duration duration = new Duration(now, nextBus);

            PeriodFormatter formatter = new PeriodFormatterBuilder() // formats time correctly for display
                    .appendWeeks().appendSuffix(" weeks ")
                    .appendDays().appendSuffix(" days ")
                    .appendHours().appendSuffix(" hr ")
                    .appendMinutes().appendSuffix(" min. ")
                    .printZeroNever()
                    .toFormatter();

            String formattedDuration = formatter.print(duration.toPeriodFrom(now));

            if (formattedDuration.length() > 0)
                return ("Next bus arrives in " + formattedDuration);
            else
                return ("Bus arriving now");
        }
        return null;
    }
}


