#include "I2C.h"
#include "RpiAP.h"
#include "MotorHandler.h"

MotorHandler motorHandler;
I2C i2c;
RpiAP rap; //raspberry pi - arduino - protocol

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  if(i2c.ready()) { //receieved data
    char* data = i2c.get();
    int result = rap.handleRequest(data);
    //delay(1000);
    i2c.send(result);
    //Serial.println(result);

    //Serial.println(result);
    //if(!rap.handleRequest(i2c.get())) { //check for success, if not successfull report to RPI
    //   Serial.println("error");
    //
    //   i2c.send(0); //error
    //}
  }

  motorHandler.tick();

  delay(100);
}
