#include "Arduino.h"
#include "SensorHandler.h"


#define TRIGGER_PIN 40
#define ECHO_PIN 41

#define SOIL_SENSOR_PIN 40
#define SOIL_SENSOR_POWER 41

unsigned int tempVar;

/*
 * returns avg. distance from probe to nearest obstacle.
 */
byte SensorHandler::distance() {

  pinMode(TRIGGER_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  tempVar = 0;
  
  for(int i = 0; i < 10; i++) {
    digitalWrite(TRIGGER_PIN, LOW);
    delayMicroseconds(5);
    digitalWrite(TRIGGER_PIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIGGER_PIN, LOW);
 
    pinMode(ECHO_PIN, INPUT);
    tempVar += (pulseIn(ECHO_PIN, HIGH)/2) / 29.1;
  
    delay(55);
  }
  return tempVar/10;
}

/*
 * Returns percentage calculated from the analog output of the probe.
 */
byte SensorHandler::soilHumidity() {
  pinMode(SOIL_SENSOR_POWER, OUTPUT);//Set D7 as an OUTPUT
  pinMode(SOIL_SENSOR_PIN, INPUT);
  digitalWrite(SOIL_SENSOR_POWER, LOW);
  digitalWrite(SOIL_SENSOR_POWER, HIGH);//turn D7 "On"
  delay(10);//wait 10 milliseconds 
  tempVar = analogRead(SOIL_SENSOR_PIN);//Read the SIG value form sensor 
  digitalWrite(SOIL_SENSOR_POWER, LOW);//turn D7 "Off"

  return map(tempVar, 0, 1023, 0, 100);
}

