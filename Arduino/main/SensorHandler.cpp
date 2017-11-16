#include "Arduino.h"
#include "SensorHandler.h"
#include "NewPing.h"


#define TRIGGER_PIN 40
#define ECHO_PIN 41

#define SOIL_SENSOR_PIN 40

NewPing usSensor(TRIGGER_PIN, ECHO_PIN, 200);

unsigned int tempVar;

/*
 * returns avg. distance from probe to nearest obstacle.
 */
byte SensorHandler::distance() {
  for(int i = 0; i < 10; i++) {
    tempVar = usSensor.convert_cm(usSensor.ping());
    delay(55);
  }
  return tempVar/10;
}

/*
 * Returns percentage calculated from the analog output of the probe.
 */
byte SensorHandler::soilHumidity() {
  return map(analogRead(SOIL_SENSOR_PIN), 0, 1023, 0, 100);
}

