/*
 * I2C.h - Handles communcation between Raspberry PI and Arduino
 * address = 0x01
 * 
 * Author: nyxaria, Created: 10/09/17
*/

#ifndef I2C_h
#define I2C_h

#include "Arduino.h"

class I2C
{
  public:
    I2C();
    bool ready();
    char* get();
    void send(int i);
  private:
  
};

#endif
