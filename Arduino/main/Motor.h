/*
 * Motor.h - class which abstracts away a Stepper Motor
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
    int rotate(byte mm, byte dir);
    int stop();
    int speed(byte percentage);
    int reset();


    boolean busy = false;
    boolean homing = false;
    boolean ready = false;
    int stepPin;
    int dirPin;
    int endStopPin;
    double leftOverSteps;
    long timer;
    int stepsPerMM;
    int enqueue(byte* req);
    void init(byte sPin, byte dPin, byte pES, int sPmm);
    void tick();
    
    
    bool complete;
    AccelStepper s;
    
    enum Constants {
        CLOCKWISE = 1,
        ANTICLOCKWISE = 0,
        stepsPerMM_TIMING_BELT = 80,
        stepsPerMM_GEAR = 200,
        maxSpeed = 2000
    };

    
  private:
     // AccelStepper s;

};

#endif

