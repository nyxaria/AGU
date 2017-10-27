/*
 * Motor.h - class which represents a StepperMotor
 * address = 0x01
 * 
 * Author: nyxaria, Created: 10/09/17
*/

#ifndef Motor_h
#define Motor_h

#include "Arduino.h"

class Motor
{
  public:
    Motor(byte pStep, byte pDir, byte pEn, int sPmm);
    int rotate(byte mm, byte dir);
    int stop();
    int speed(byte percentage);
    int acceleration(byte percentage);
    int reset();

    int stepsPerMM;

    int enqueue(char* req);

    void tick();
    
    enum Constants {
      CLOCKWISE = 1,
      ANTICLOCKWISE = 0,
      STEPS_PER_REV = 200;
      NEMA17_STEPS_PER_MM = 0
    };

    
  private:
  
};

#endif
