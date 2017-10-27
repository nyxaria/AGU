# AGU

In this project I set out to create a robot that autonimously takes care of plants, in my case predominantly mint for my morning mint tea. Yum.<br>
The main goal -alongside making my mint tea without setting my foot out the apartment- was to learn as much as possible in as many areas I could, including software, hardware, electronics, CAD and even editing.

Project Overview
----------------
The project is made up of 3 layers: The Android (High-level instructions), RaspberryPi 3 (High-level interpreter of instructions), and Arduino Mega (Low-level hardware controller)

<p align="center"><img src="https://i.imgur.com/9xJH2lu.png" data-canonical-src="https://i.imgur.com/9xJH2lu.png" width="520" height="400" /></p>

The Android application is a simple UI composed of a rectangular 'garden' view where the user can add pots, and a 'seedbank' view where the user can select which seeds are in the physical trays of the machine. This application then communictes via Bluetooth with the RaspberryPi 3 [RPI], sending the raw data that the user put in. _[see: ~/Android/_]{__Java__}
<br><br>
The RPI acts as the brian of the operation, converting the high level abstracted instructions from the Android application into sequences of low level instructions, similar to GCODE, that the Arduino carries out one after another. It does this via scheduled timers as well as based on sensor data, such as the humidity soil sensor, depending on the plant.
It also makes a height map every day to monitor growth, personalise watering for each plant via plant-specific data scraped from a website **{see: /Utility Programs/*}**   _[see: ~/RaspberryPi/_]{__Python__}
<br><br>
The Arduino firmware is the most low level code in this whole project, with the purpose of controlling the stepper motors on the gantry and probe, read end-stops, and turn the 2 pumps on/off. It communicates via the I2C bus with the Raspberry Pi 3, which acts as the brains of this build. _[see: ~/Arduino/_]{___C++__}_

CAD
---
During this project I learnt to use Inventor, Fusion360, EAGLE as well as Fritzing. In the end, I ended up using Inventor instead of Fusion360 for the 3D model as it felt more natural to me (and more powerful) but used Fritzing and Eagle for the electronics diagram and schematic, respectively. *{see: ~/CAD/}*

Software
---

In this regard, I had alot of freedom in how I wanted to implement this. I ended up using an Arduino Mega because of the speed at which it alowed me to prototype, and the (relative) simplicity of the C++. In a future project, however, I will probably use an 8051/PIC microcontroller and use Assembly, not because I like self-inflicted pain but because it seems the only natural step in moving away from the 'hobby electronics' world. 
<br>In another direction I could use a ESP32 with it's Wifi+BT capabilities, and potentially eliminate the RPI entirely. 
However writing all the 'brain' code in Python is _way_ easier than in C++, which is the main reason why I chose the RPI (MicroPython just dosen't have the same support on Arduino's.)
<br><br>
For the interface of the project I decided to make an Android application in Java (none of that Ionic nonsense) to brush up on my skills, but it could easily have been a Web interface written in JS+Html+Css for the UI and Flask+Py on the back end.

Hardware
---

Makerbeams, ect budget
