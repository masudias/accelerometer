# accelerometer
This project gets the readings from the Android accelerometer and save the readings into the local SQLite database first. Then you might consider exporting the readings in the CSV format into the device's external memory which can be shared later with others.

This is basically developed for a class project as we could not find any free application in the Google Play Store that provides a feasible export option to share the readings from the accelerometer of the device with others via Email or WhatsApp.

We have implemented the wake-lock feature as well, as we found for data logging, it is important to keep the screen awake so that it helps us to monitor it in the run-time.

User needs to grant the permission to write/read the external storage, as this is required to export the readings as a CSV file into the external directory. 

# How to use
We need two devices for taking accelerometer readings at the same time for our project. We need to store the readings in the local SQLite database which can be exported to a CSV file later. The exported CSV files will be stored in the `readings` directory of the external storage of the Android device. 

For our project to work smoothly, we were in need of synchronizing timestamp between these two devices used. It was surprising that, even if both of the device used in this project uses the network provided timestamp, there was significant time difference between them (nearly 4 seconds) which is not suitable for our case. Hence we needed to synchronize the timestamp between these two device using the shaking technique. 

The shaking technique works like the following. 
 - Run the app in both devices and take both of the devices in one hand and hold them together.
 - Shake the device together strongly, so that both of them can get the peak of the first large shake and sync their time accordingly. 
 - Once both of the devices are synced, it will go to the next screen automatically where you will be able to get the accelerometer readings. 
 - If one of them moves to the next screen and the other does not, then please initiate the shake and sync process again for the device which moved to the next screen. 
