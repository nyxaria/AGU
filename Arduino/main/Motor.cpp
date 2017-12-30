#include "Arduino.h"
#include "Motor.h"
#include "MotorHandler.h"
#include "RAP.h"
#include "SensorHandler.h"
#include <AccelStepper.h>
#include <stdlib.h>     /* abs */
#include <math.h>

void Motor::init(byte sPin, byte dPin, byte pES, int sPmm) {
    stepsPerMM = sPmm;
    endStopPin = pES;
    s._interface = 1;
    s._pin[0] = sPin;
    s._pin[1] = dPin;
    stepPin = sPin;
    dirPin = dPin;
    s.enableOutputs();
    s._targetPos = s._currentPos = 0;
    if(stepPin == 10)
        pinMode(pES, INPUT_PULLUP);
    else
        pinMode(pES, INPUT);

    if(sPin == 8) {
        s.setPinsInverted(true);
    }
    
    if(sPin == 10) { // y dir does not have 16 micro stepping enabled
        s.setMaxSpeed(2000/4);
        s.setSpeed(2000/4);
        s.setAcceleration(1000/4);
    } else {
        s.setMaxSpeed(2000);
        s.setSpeed(2000);
        s.setAcceleration(1000);
    }
   
}


int Motor::enqueue(byte* req) {
    switch(req[2]) {
        case RAP::rotate:
            return rotate((byte)req[3], (byte)req[4]);
            break;
        case RAP::speed:
            return speed((byte)req[3]); // 0 - 100 % of pre-determined maxSpeed
            break;
        case RAP::stop:
            return stop();
            break;
        case RAP::reset:
            return reset();    
        default:
            return RAP::FAILURE;
  }
}

void Motor::tick() {
   if(ready) {
       if(homing) {
            s.run();
            if((stepPin == 10 && digitalRead(endStopPin) == LOW) || (stepPin != 10 && digitalRead(endStopPin) == HIGH)) {
                if(timer == 0) 
                    timer = millis();
                
                if((millis() - timer > 10 && stepPin != 10) || (millis() - timer > 5 && stepPin == 10)) {
                    s.stop();
                    s.runToPosition();
                    s._targetPos = s._currentPos = 0;

                    timer = 0;
                    homing = false;
                    busy = false;
                    complete = true;
    
                    if(stepPin == 10) { // y dir does not have 16 micro stepping enabled
                        s.setMaxSpeed(2000/4);
                        s.setSpeed(2000/4);
                        s.setAcceleration(1000/4);
                    } else {
                        s.setMaxSpeed(2000);
                        s.setSpeed(2000);
                        s.setAcceleration(1000);
                    }
                }
            } else {
                timer = millis();
            }
       } else {
            if(s._targetPos != s._currentPos) {       
                s.run();
            }
            busy = !(s._targetPos == s._currentPos);
       }
   }
}


int Motor::rotate(byte high, byte low) {
    complete = false;
    busy = true;
    homing = false;
    long mm = high*((long)255) + low;
    //double toDo = 0.0;
    //leftOverSteps += modf(leftOverSteps, &toDo);

    s.moveTo(-abs((long)((mm/* + toDo*/)*stepsPerMM)));

    return RAP::SUCCESS;

}

int Motor::speed(byte percentage) {
    s.setMaxSpeed((percentage * Motor::maxSpeed) / 100);
    s.setSpeed((percentage * Motor::maxSpeed) / 100);
    s.setAcceleration((percentage * Motor::maxSpeed) / 200);
    return RAP::SUCCESS;
}


int Motor::stop() {
    s.stop();
    s._targetPos = s._currentPos;
    busy = false;
    return RAP::SUCCESS;
}

int Motor::reset() {
    complete = false;
    busy = true;
    homing = true;
    s.stop(); 

    if(stepPin != 10) {
        s.setMaxSpeed(1000);
        s.setSpeed(500);
        s.setAcceleration(500);
    } else {
        s.setMaxSpeed(250);
        s.setSpeed(250);
        s.setAcceleration(500);
    }
    s.move(1000000);

    return RAP::SUCCESS;
}

