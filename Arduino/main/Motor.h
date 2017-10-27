/*
 * Motor.h - class which represents a StepperMotor
 * address = 0x01
 * 
 * Author: nyxaria, Created: 10/09/17
*/

#ifndef Motor_h
#define Motor_h

#include "Arduino.h"
#include "AccelStepper.h"

class Motor
{
  public:
    Motor();   
    int rotate(byte mm, byte dir);
    int stop();
    int speed(byte percentage);
    int reset();

    int stepsPerMM;
    int endStopPin; 
    int enqueue(char* req);
    void init(byte key, byte sPin, byte dPin, byte pES, int sPmm);
    void tick();

    AccelStepper s;
    
    enum Constants {
        CLOCKWISE = 1,
        ANTICLOCKWISE = 0,
        stepsPerMM_TIMING_BELT = 5,
        stepsPerMM_LEADSCREW = 200,
        stepsPerMM_GEAR = 200,
        maxSpeed = 2000
    };

    
  private:
     // AccelStepper s;

};

#endif

