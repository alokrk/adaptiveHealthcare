README

Service Oriented Computing
Alok Kucheria [akucher]
Project 1 - Adaptive Healthcare

Using sensor and location data, decide to show emergency details.

Installed Android Studio with required SDK platforms and tools.
Tested on Nexus 4,Nexus 4 simulator and Nexus 5X simulator.

Source files in AdaptiveHealthcare\app\src\main\java\com\soc\adaptivehealthcare

Flow - Detect pressure, light and location. 
-If barometer absent, assume value below threshold. [29.5 value for barometer changed to match sensor output units [mbar]]
-If light sensor absent, assume value below threshold. 
-If no net connectivity, assume duration less than 5 minutes and proceed with other factors. [Ensure connectivity.]
Press button to simulate fall. [used instead of a pseudo random number]
If all conditions satisfy, display emergency details. Else toast saying, "Not an emergency"

Barometer - Working
Light - Working
Fall - Working. Simulated using a button where another activity is called
Location - Working Partially. Depending on individual runs, getLastLocation might not fetch data properly resulting in improper results sometimes.
			Need to add fix for the same so location is taken correctly.
			Found lastknown location. Hardcoded REX Hospital location as discussed in class.
				
Emergency Details - New activity based on values from previous sensors. 


In case of Firebase error, please refer:
http://stackoverflow.com/questions/37421203/java-lang-noclassdeffounderror-com-google-firebase-firebaseoptions
Sometimes issue acknowledged by Google PM.

In case of DEX issue, addition required in gradle file.

