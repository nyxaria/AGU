#include "Arduino.h"
#include "MotorHandler.h"
#include "Motor.h"

#define X1_STEP_PIN 2
#define X1_DIR_PIN  3
#define X1_ENDSTOP_PIN 42

#define X2_STEP_PIN 4
#define X2_DIR_PIN  5
#define X2_ENDSTOP_PIN 43

#define Y_STEP_PIN 6
#define Y_DIR_PIN  7
#define Y_ENDSTOP_PIN 41

#define E_STEP_PIN 8
#define E_DIR_PIN  9

#define Z_STEP_PIN 10
#define Z_DIR_PIN  11
#define Z_ENDSTOP_PIN 40





Motor x1;
Motor x2;
//Motor y;
//Motor z;
//Motor e;
MotorHandler::MotorHandler() {

}

long last = millis();
void MotorHandler::init() {
    //x1 = new Motor();
    x1.init(0, X1_STEP_PIN, X1_DIR_PIN, X1_ENDSTOP_PIN, Motor::stepsPerMM_TIMING_BELT);
    
    //x2 = new Motor();
    x2.init(1,X2_STEP_PIN, X2_DIR_PIN, X2_ENDSTOP_PIN, Motor::stepsPerMM_TIMING_BELT);
    
    //y.init(2,Y_STEP_PIN, Y_DIR_PIN, Y_ENDSTOP_PIN, Motor::stepsPerMM_TIMING_BELT);
    //z.init(3,Z_STEP_PIN, Z_DIR_PIN, Z_ENDSTOP_PIN, Motor::stepsPerMM_LEADSCREW);
    //e.init(4,E_STEP_PIN, E_DIR_PIN, -1, Motor::stepsPerMM_GEAR);
}

Motor* MotorHandler::get(byte key) {
  switch (key) {
    case MotorHandler::m_x1:
      return &x1;
      break;
    case MotorHandler::m_x2:
      return &x2;
      break;
    case MotorHandler::m_y:
      //return &y;
      break;
    case MotorHandler::m_e:
      //return &e;
      break;
    case MotorHandler::m_z:
      //return &z;
      break;
  }
  return 0;
}

void MotorHandler::tick() { //update logic
    
  x1.tick();
  x2.tick();
  //y.tick();
  //e.tick();
  //z.tick();

 
}


