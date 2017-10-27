#include "Arduino.h"
#include "SensorHandler.h"

byte pinModes[60];
 
SensorHandler::SensorHandler() {
    //
}

int tempVar;

byte SensorHandler::endStop(byte pin) {
    if(pinModes[pin] == SensorHandler::OUT) {
        pinMode(pin, INPUT);
        pinModes[pin] == SensorHandler::IN;
        delay(10);
    }

    tempVar = digitalRead(pin);
    if(tempVar == HIGH) {
        return 1;
    } else if(tempVar == LOW) {
        return 0;
    }
    
}

