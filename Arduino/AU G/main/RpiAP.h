/*
   RaspberryPi Arduino Protocol:
    string 'req' of arbitrary size, based on implementation of tree.
    motor, read and water are root nodes of the tree structure represented by this protocol.

    Author: nyxaria, Created: 10/09/17
*/

#ifndef RpiAP_h
#define RpiAP_h

#include "Arduino.h"
#include "Motor.h"

class RpiAP
{
  public:
    RpiAP();
    int handleRequest(char* req);
    enum Constants {
      /* Motor Node */

      //Root
      motor = 'M',

      //1st
      x1 = 'x',
      x2 = 'X',
      y1 = 'y',
      y2 = 'Y',
      z = 'z',

      //2nd
      rotate = 'r',
      stop = '!',
      speed = 'v',
      acceleration = 'a',

      /*           */

      /* Read Node */

      //Root
      read = 'R',

      //1st
      soilHumidity = 'h',
      distance = 'd',

      //2nd

      // ** implemented in Motor/1st

      /*            */

      /* Write Node */

      //Root
      write = 'W',

      //1st
      water = 'w',

      /*            */
      DEFAULT = 0,
      DATA_RECEIVED = 125,
      FAILURE = 126,
      SUCCESS = 127
    };
  private:
    int motorRequest(char* req);
    //int motorRequest(char* req, Motor motor);
    int readRequest(char* req);
    int writeRequest(char* req);
    
};

#endif
