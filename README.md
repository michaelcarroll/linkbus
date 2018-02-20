##Linkbus

Linkbus is an Android application that provides an always-accurate view of the "Link" bus schedule for the College of Saint Benedict / Saint John's University campuses. 

Current app offerings for the bus schedule were weak and hadn't been updated for 3 years, so I decided built my own. The app pulls data from csbsju.edu/bus to provide the most accurate and up-to-date schedule, so any changes to the schedule are immediately reflected in the app.

##Installation

Linkbus is currently available for Android in the [Google Play Store](https://play.google.com/store/apps/details?id=com.carroll.michael.linkbus). In the future I plan to create a solution for iOS devices.

##Features

* **Fast launch:** app launches within 1-2 seconds and displays schedule immediately (much faster than loading a website)
* **Clean interface:** Expandable cards for each bus route (CardView), follows Material UI guidelines 
* **Date advancement:** ability to view schedule for a future date, up to a week in advance
* **[JodaTime library](https://github.com/dlew/joda-time-android):** While Java has [built-in time handeling](https://docs.oracle.com/javase/7/docs/api/java/util/Calendar.html), prior to Java 8 it has critical bugs and is difficult to use. JodaTime provides a solution for applications that need date/time management with many [benefits](http://www.joda.org/joda-time/#Why_Joda-Time). 
* **Remote messaging system:** ability for developer to display an Android SnackBar message on launch with useful information (ex. link to an altered class schedule)

##Screenshots

##Compatibility

Linkbus requires Android 5.0+ (minSdkVersion 21). In the future I plan to create a soltuion for iOS devices.

##Feedback

I appreciate all feedback and it helps the development immensley. If you find any bugs or have a feature request, please [open an issue](https://github.com/MichaelCarroll/linkbus/issues/new) or send feedback using the link on Google Play.

##Changelog

##License

[MIT License](https://raw.githubusercontent.com/michaelcarroll/linkbus/master/LICENSE)
