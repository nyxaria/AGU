# Autonomous Growing Unit

In this project I set out to create a robot that autonimously takes care of plants.<br>
The main goal was to learn as much as possible in as many areas I could, including software, hardware, electronics, CAD and even editing.

Project Overview
----------------
The project is made up of 3 layers: The Android (High-level instructions), RaspberryPi 3 (High-level interpreter of instructions), and Arduino Mega (Low-level hardware controller).

<p align="center"><img src="https://i.imgur.com/9xJH2lu.png" data-canonical-src="https://i.imgur.com/9xJH2lu.png" width="520" height="400" /></p>

The Android application is a simple UI composed of a rectangular 'garden' view where the user can add pots, and a 'seedbank' view where the user can select which seeds are in the physical trays of the machine. This application then communictes via Bluetooth with the RaspberryPi 3 [RPI], sending the raw data that the user put in. (Java)
<p align="center"><img src="https://i.imgur.com/MVGp59z.png" data-canonical-src="https://i.imgur.com/MVGp59z.png" width="800" height="225" /></p>

<br><br>
The RPI acts as the brian of the operation, converting the high level abstracted instructions from the Android application into sequences of low level instructions, similar to GCODE, that the Arduino carries out one after another. It does this via scheduled timers as well as based on sensor data, such as the humidity soil sensor, depending on the plant.
It also makes a height map every day to monitor growth, personalise watering for each plant via plant-specific data scraped from a website (see Utility Programs.) (Python)
<br><br>
The Arduino firmware is the most low level code in this whole project, with the purpose of controlling the stepper motors on the gantry and probe, read end-stops, and turn the 2 pumps on/off. It communicates via the I2C bus with the Raspberry Pi 3, which acts as the brains of this build. (C++)

CAD
---
During this project I learnt to use Inventor, Fusion360, EAGLE as well as Fritzing. In the end, I ended up using Inventor instead of Fusion360 for the 3D model as it felt more natural to me (and more powerful) but used Fritzing and Eagle for the electronics diagram and schematic, respectively.

Model can be viewed online at https://skfb.ly/6vAEX.
<p align="center"><img src="/CAD/digrams:renders/cad_gif.gif" width="500px"></p>

<p align="center"><img src="/CAD/digrams:renders/Electronics%20Schematic.png" width="650px"></p>

Software
---

In this regard, I had alot of freedom in how I wanted to implement this. I ended up using an Arduino Mega because of the speed at which it alowed me to prototype, and the relative simplicity of the C++. In a future project, however, I will probably use an 8051/PIC/vanilla Atmel microcontroller and use Assembly or embedded C, not because I like self-inflicted pain but because it seems the only natural step in moving away from the 'hobby electronics' world. 
<br>In another direction I could use a ESP32 with it's Wifi+BT capabilities, and potentially eliminate the RPI entirely.
<br><br>
For the interface of the project I decided to make an Android application in Java (none of that Ionic nonsense) to brush up on my skills, but it could easily have been a Web interface written in JS+Html+Css for the UI and Flask+Py on the back end.

Hardware
---

I managed to get Makerbeams for free from my friend, so I used them for the construction. To hold everything together, I used tie plates from home depot. Everything else is held together by hot glue. For motion, I used bearings for z and x axis, and leadscrew for the y axis.

In this project I used Nema17 stepper motors which I managed to get for a really good price second hand. For the fluid transfer, I used a water pump from a coffee-machine and a vacuum pump from I ordered from China. The Mosfets and other components I scavenged from old electronics I got my hands on.

Electronics
---

I first created a schematic for project, and was planning on etching the PCB for the board containing the stepper motor drivers. However, none of the printers I have used to print the schematics on the toner-transfer paper in New York worked with the photo-transfer method. I presume it was because they were all Brother printers. As a result of that, I instead used a perf-board to make the driver break-out board.


<p align="center"><img src="/CAD/digrams:renders/Electronics%20Diagram.png" width="400px"></p>
