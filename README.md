## Linkbus

Linkbus is an Android application that provides an always-accurate view of the "Link" bus schedule for the [College of Saint Benedict & Saint John's University](https://csbsju.edu/) campuses. 

App offerings for the school's bus schedule were weak and hadn't been updated for approximately 3 years, so I decided build my own. The app pulls data from [csbsju.edu/bus](https://csbsju.edu/bus) to provide the most accurate and up-to-date schedule, so any changes to the schedule are immediately reflected in the app.

## Installation

Linkbus is currently available for Android in the [Google Play Store](https://play.google.com/store/apps/details?id=com.carroll.michael.linkbus). In the future I plan to create a solution for iOS devices.

[<img src="https://user-images.githubusercontent.com/20750745/98448918-d1d99380-20ec-11eb-9262-c95ad1b957ab.png">](https://play.google.com/store/apps/details?id=com.carroll.michael.linkbus&hl=en_US)

[![Foo](http://www.google.com.au/images/nav_logo7.png)](http://google.com.au/)

## Features

* **Lightweight:** App launches within 1-2 seconds and displays schedule immediately (much faster than loading a website)
* **Clean interface:** Expandable cards for each bus route (CardView), follows Material UI guidelines 
* **Date advancement:** Ability to view schedule for a future date, up to a week in advance
* **[JodaTime library](https://github.com/dlew/joda-time-android):** While Java has [built-in time handeling](https://docs.oracle.com/javase/7/docs/api/java/util/Calendar.html), prior to Java 8 it has critical bugs and is difficult to use. JodaTime provides a solution for applications that need date/time management with many [benefits](http://www.joda.org/joda-time/#Why_Joda-Time). 
* **Remote messaging system:** Ability for developer to remotely set an Android SnackBar message to display on initial launch (for example, a special class schedule for the day — see screenshots)

## Screenshots

![screener_1517256064241](https://user-images.githubusercontent.com/20750745/36409018-8a509968-15ce-11e8-87e5-848a55300000.png)
![screener_1517256256121](https://user-images.githubusercontent.com/20750745/36409017-8a44ecc6-15ce-11e8-8125-4a57a78c117e.png)
![screener_1517256207613](https://user-images.githubusercontent.com/20750745/36409019-8a5c3cd2-15ce-11e8-9c22-9e5fdc318145.png)

## Compatibility

Linkbus requires Android 5.0+ (minSdkVersion 21). In the future I plan to create a solution for iOS devices.

## Feedback

I appreciate all feedback as it helps the development of this project immensley. If you find any bugs or have a feature request, please [open an issue](https://github.com/MichaelCarroll/linkbus/issues/new) or send feedback using the link on [Google Play](https://play.google.com/store/apps/details?id=com.carroll.michael.linkbus). The app is now out of beta and is mostly stable for regular use, however bugs may crop up.

## License

[MIT License](https://raw.githubusercontent.com/michaelcarroll/linkbus/master/LICENSE)
