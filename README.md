GooglePlayServices-OpenFL
=========================
Google Play Services extension for OpenFL 
Originally based on https://github.com/mkorman9/admob-openfl

Note: Upon setting up this library, Google Play Services will try to log when your application is started.

Initial Setup
--------------
1. Clone this repo using ```haxelib git GPS https://github.com/CharcoalStyles/GooglePlayServices-OpenFL``` command   
2. Add this line to your project.xml file ```<haxelib name="GPS" /> ```   
3. Copy the file ids.xml to the root directory of the project.   
4. Replace **Google_App_ID** in ids.xml with the Game Services App ID from the Google Play Developer Console.   
5. Add following code to your project.xml:   
```<template path="ids.xml" rename="res/values/ids.xml" if="android" />```   

To Show ADs
--------------
1. Import AD class ```import admob.AD; ```   
2. Setup ads at the beginning of your code   
```AD.init(ADMOB_ID, AD.LEFT, AD.BOTTOM, AD.BANNER_PORTRAIT, ?DEVICE_ID);```   
```AD.initInterstitial(ADMOB_ID, ?DEVICE_ID);```   
where the arguments are following:   
  - Ad unit id.   
  - Position in x axis. Could be ```AD.LEFT``` or ```AD.RIGHT```   
  - Position in y axis. Could be ```AD.TOP``` or ```AD.BOTTOM```   
  - Banner size. Could be ```AD.BANNER_PORTRAIT``` or ```AD.BANNER_LANDSCAPE``` (see Google's documentation)   
  - Device ID. Set a device to show only test Ads. Optional.   
3. Show Banner Ad ```AD.show();```   
4. Show Interstitial Ad ```AD.showInterstitial();```   
5. You can hide the Banner Ad anytime by calling ```AD.hide();```



Leaderboards
--------------

1. Setup your leaderboards in the Google Play Developer Console
2. Import Leaderboards class ```import playServices.Leaderboards;```
3. Submit a score: ```Leaderboards.submitScore(leaderboardId, score);```
4. View a leaderboard: ```Leaderboards.showLeaderboard(leaderboardId);```
5. View all leaderboards: ```Leaderboards.showAllLeaderboards();```
