/*
   RaspberryPi Arduino Protocol:
    string 'req' of arbitrary size, based on implementation of tree.
    motor, read and water are root nodes of the tree structure represented by this protocol.

    Author: nyxaria, Created: 10/09/17
*/

#ifndef RAP_h
#define RAP_h

#include "Arduino.h"
#include "Motor.h"
#include "MotorHandler.h"
#include "SensorHandler.h"

class RAP
{
    public:
    int handleRequest(byte* req);
        enum Constants {
            /* Motor Node */
        
            //Root
            motor = 0,

          //1st
            x1 = 0,
            x2 = 1,
            y = 2,
            e = 3,
            z = 4,
            ready = 5,
            
            //2nd
            rotate = 0,
            stop = 1,
            speed = 2,
            reset = 3,
    
          /*           */
    
          /* Read Node */
    
            //Root
            read = 1,
    
            //1st
            soilHumidity = 0,
            distance = 1,
    
            //2nd
    
            // ** implemented in Motor/1st
    
            /*            */
    
            /* Write Node */
    
            //Root
            write = 2,
    
            //1st
            water = 0,
            vacuum = 1,
    
          /*            */
            DEFAULTT = 0,
            MOTOR_FINISHED = 50,
            MOTOR_RUNNING = 51,
            DATA_RECEIVED = 125,
            FAILURE = 126,
            SUCCESS = 127
          };

          MotorHandler* motors;
          SensorHandler* sensors;
    private:
        int motorRequest(byte* req);
        int readRequest(byte* req);
        int writeRequest(byte* req);
    
};

#endif

