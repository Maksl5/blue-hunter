# Blue Hunter (Android)
Hello and welcome to this repository.
It's very important to read this here, to get an imagination about this project.


----------


## Introduction / Description
As you might know, _Blue Hunter_ will be the follow-up of the [Scan Most Bluetooth Devices](http://forum.xda-developers.com/showthread.php?t=863626) game. The reasons for the complete restart of the same intent are those:

 - _Scan Most Bluetooth Devices_ was started up in 2010. At this time I had nothing to do with Java. I was a complete newbie. Therefore the code for the complete base was, to put it simply, shit. But I built on this base further and further. After some time I realized, that this can't be rescued anymore.

 - The complete intern handling of things wasn't that, what I imagined.

 - Some new features I wanted to get implemented were really not fitting into the game structure.

 - And of course I wanted to do some new.

For those of you, who didn't know about _Scan Most Bluetooth Devices_, here is a short description of that, what the game should look like in the future.

Blue Hunter is a level-up game, in which you have to search for Bluetooth devices in range. Each unique device, that you'll find will give you some EXP. While gaining more an more EXP, you will, of couse, get into the next level. For each level-up you will unlock specific small game features, that will make it mor comfortable to play. That's not all. You will have the chance to decide, if you also want to play against the others, who play this game. For that there will be the Leaderboard. Later you will also be able to add friends, make a player vs. player game, establish your own clan, etc. Achievements will also be integrated.


----------


## Development
For sure the game is currently under development. But this doesn't prevents me from opening the source here to GitHub. Keep in mind, that classes that are written for security purposes of course are not published. Security does play an important role in this society, so please excuse me, that I will not share any of my hash/security algorithms or fields, that contain secure content. The peculiarity of this project is, that you can get the newest nightly available, right from my testing purposes pushed to the [bin/](https://github.com/Maksl5/blue-hunter/tree/master/bin) folder. So you can always keep an eye on this and check out, what's new now. Updates of the .apk are very frequently. If I just worked on the project on a day, there will be the corresponding .apk available at this day.


----------


## Permissions
This game uses some Android Permissions. Here I want to explain, for what these Permissions are used:

 - **_android.permission.BLUETOOTH_** - Is used to receive the state of Bluetooth and to start the discovery of Bluetooth devices.
 - **_android.permission.BLUETOOTH\_ADMIN_** - Is used to turn Bluetooth on, when necessary.
 - **_android.permission.INTERNET_** - Is used to connect to the internet, to get the game synchronized with the game servers.
 - **_android.permission.ACCESS_NETWORK_STATE_** - Is used to determinate internet connectivity change.
 - **_android.permission.VIBRATE_** - Is used to notify the user about a new found device by vibration.


----------


## Compatibility

As this game is still in a very early development stage (pre-alpha), this game can only installed on devices with **_Honeycomb (3.\*)_** and above. It should work well under **_ICS (4.0.\*)_** and **_Jelly Bean (4.1)_**. Under **_Honeycomb_** should be a _Force Close_ when opening the app. If not so, feel lucky.

Support for **_Gingerbread (2.3.\*)_** will come, when the game reaches an playable development state. Please be patient. Thank you.


----------


## Translations
Like in _Scan Most Bluetooth Devices_ I depend on the community for getting translations.
At this point I want to ask you, if you think you can translate the game into your language. If this is the case I really would like to send you to the [strings.xml](https://github.com/Maksl5/blue-hunter/blob/master/res/values/strings.xml) file. There you'll find all strings, that can be translated. Later I will integrate a small platform for translators, which will make them lifes easier. Much easier ! But also this takes time.

### Included Translations:

> - English
> - Dutch (Marc.)
>

### Oncoming Translations:

> - German
>

Thank you ! (Of course I will think about a worthy compensation for your work !)


----------


## Privacy
Here I want to explain to you, what private information located on your device and got from other devices through Bluetooth is used by this game. This list contains and explains **ALL** gathered information from the game, you might worry about.

 - **_Serial Number (S/N)_** - The game uses the serial number of your device, to determinate the installation of the game and to identify the unique game installation. The purpose of the use is, if you reset your Android to factory and after that install the game, it can automatically drag over the application data from the server, without user interaction.<br>
_This information is sent to the game server and stored in a database._

 - **_Version Code_** - This is the internal version code of the game. It is used to check, if a new nightly version is available.<br>
_This information is sent to the game server and stored in a database._

 - **_Remote MAC Address_** - This is the MAC Address of devices which you will found. It is used to determinate the manufacturer of the device and to give you specific expierence values for each manufacturer. (Some devices are more rare than others.)<br>
_This information is **NOT** sent to the game server, but stored in the offline game database._

 - **_Remote Device RSSI_** - This is the **R**eceived **S**ignal **S**trength **I**ndication. This is used to display as information to you.<br>
_This information is **NOT** sent to the game server, but stored in the offline game database._

 - **_Remote Device Name_** - This is the name of the remote Bluetooth device. This is used to display as information to you.<br>
_This information is **NOT** sent to the game server, but stored in the offline game database._

 - **_Local Time_** - This is the time, that is saved, when you find a new device.<br>
_This information is **NOT** sent to the game server, but stored in the offline game database._


All information gathered from the game and send to the game server into the database is completely nonpublic. The database is completely locked. The only person, who has access to this database is me ! No companies and no other persons have access and are allowed to gather information from this database in any way. Your stored information will not be used for any other purposes, that don't have to do with the game functionality and with statistic purposes !


----------


## Support
You have questions ? Just ask me. You can send me an email all the time. I will try to answer all your questions.
You can also make feature suggestions.
If you find any bugs, errors, Force Closes, whatever, please feel free, to flood the [Issues site](https://github.com/Maksl5/blue-hunter/issues) on GitHub. You can also aks questions there, if you don't want to wait.

### FAQ

> Will this game be published on the Google Play Store ?

_Yes, it will. The entry is already in the system._


----------


Thank you for the time you spent for this !

Greetings !