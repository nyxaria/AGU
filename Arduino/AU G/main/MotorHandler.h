/*
 * MotorHandler.h - Handles motors
 * 
 * Author: nyxaria, Created: 11/09/17
*/

#ifndef MotorHandler_h
#define MotorHandler_h

#include "Arduino.h"
#include "Motor.h"




class MotorHandler
{
  public:
    MotorHandler();
    Motor get(byte key);
    enum Constants {
      m_x1 = 0,
      m_x2 = 1,
      m_y1 = 2,
      m_y2 = 3,
      m_z = 4
    };  
    void tick();
    
  private:
  
};

#endif
