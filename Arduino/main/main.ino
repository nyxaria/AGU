#include "I2C.h"
#include "RAP.h"
#include "MotorHandler.h"
#include "SensorHandler.h"

SensorHandler sensorHandler;
MotorHandler motorHandler;
I2C i2c;
RAP rap; //raspberry pi - arduino - protocol (names are not my strong point)

void setup() {
    pinMode(38, OUTPUT); //water
    pinMode(32, OUTPUT); //vacuum
    digitalWrite(32, LOW);
    digitalWrite(38, LOW);

    i2c.init();
    motorHandler.init();
    rap.motors = &motorHandler;
    rap.sensors = &sensorHandler;
   
}
long delayMs = millis();
int toSend = 0;
void loop() {
    if (i2c.ready()) { //check for data from rpi
        i2c.send(rap.handleRequest(i2c.get())); //run process and respond to rpi
        delayMs = millis();
    }
    if(millis() - delayMs > 100) { //send constant flag for RPI except when requesting information
        i2c.send(motorHandler.finished());
    }
    motorHandler.tick();
}

