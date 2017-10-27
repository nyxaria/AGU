#include "Arduino.h"
#include "Motor.h"
#include "MotorHandler.h"
#include "RpiAP.h"
#include "SensorHandler.h"
#include <AccelStepper.h>
#include <stdlib.h>     /* abs */

SensorHandler sensors;

byte speed, acceleration;
bool busy = false;
bool homing = false;
int stepsPerMM = 5;


Motor::Motor() {
    
}

AccelStepper s(1);
void Motor::init(byte key, byte sPin, byte dPin, byte pES, int sPmm) {
    stepsPerMM = sPmm;
    endStopPin = pES;
    //init(stepper);
    s._interface = 1;
    s._pin[0] = sPin;
    s._pin[1] = dPin;
   // s.enableOutputs();

    s.setMaxSpeed(2000);
    s.setSpeed(2000);
    s.setAcceleration(1000);
}


int Motor::enqueue(char* req) {
    switch(req[2]) {
        case RpiAP::rotate:
            return rotate((byte)req[3], (byte)req[4]);
            break;
        case RpiAP::speed:
            return speed((byte)req[3]); // 0 - 100 % of pre-determined maxSpeed
            break;
        case RpiAP::stop:
            return stop();
            break;
        case RpiAP::reset:
            return reset();    
        default:
            return RpiAP::FAILURE;
  }
}

void Motor::tick() {
   s.run();
   if (s.distanceToGo() == 0)
 s.moveTo(-s.currentPosition());
    if(homing) {
        if(sensors.endStop(endStopPin) == 1) {
            stop();
            s._currentPos = 0;
            s._targetPos = 0;
            homing = false;
        } else {
            s._targetPos = s._currentPos - 10;
            delay(1);
        }
        return;
    }

    if(s._targetPos == s._currentPos) {
        busy = false;
    }
}

int Motor::rotate(byte high, byte low) {
    int mm = high + low;
    s._targetPos = abs(mm*stepsPerMM*100);
    busy = true;
    return 1;

}

int Motor::speed(byte percentage) {
    s.setMaxSpeed((percentage * Motor::maxSpeed) / 100);
    s.setSpeed((percentage%101 * Motor::maxSpeed) / 100);
    return 1;
}


int Motor::stop() {
    s.stop();
    s.runToPosition();
    s._targetPos = s._currentPos;
    busy = false;
}

int Motor::reset() { // implement system to check when motors at origin
    busy = false;
    s.stop(); 
    s.runToPosition();
    homing = true;
}







