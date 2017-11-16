/*
 * MotorHandler.h - Handles motors
 * 
 * Author: nyxaria, Created: 22/10/17
*/

#ifndef SensorHandler_h
#define SensorHandler_h

#include "Arduino.h"


class SensorHandler
{
  public:
    byte distance();
    byte soilHumidity();        
  private:
  
};

#endif

