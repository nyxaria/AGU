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
    boolean pause;
    void init();
    int enqueue(byte key, char* req);
    Motor* get(byte key);
    void ready(bool b);
    enum Constants {
      m_x1 = 0,
      m_x2 = 1,
      m_y = 2,
//      m_e = 3,
      m_z = 4
      
    };  
    void tick();
    byte finished();
    
  private:
  
};

#endif

