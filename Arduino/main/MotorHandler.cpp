#include "Arduino.h"
#include "MotorHandler.h"
#include "Motor.h"
#include "RAP.h"

#define X1_STEP_PIN 2
#define X1_DIR_PIN  3
#define X1_ENDSTOP_PIN 42

#define X2_STEP_PIN 8
#define X2_DIR_PIN  9
#define X2_ENDSTOP_PIN 43

#define Y_STEP_PIN 10
#define Y_DIR_PIN  11
#define Y_ENDSTOP_PIN 52

//#define E_STEP_PIN 6
//#define E_DIR_PIN  7

#define Z_STEP_PIN 6
#define Z_DIR_PIN  7
#define Z_ENDSTOP_PIN 40

float stepsPerMM_LEADSCREW = 141.73*8.0/9.0;




Motor x1;
Motor x2;
Motor y;
Motor z;
//Motor e;
MotorHandler::MotorHandler() {

}

void MotorHandler::init() {
    x1.init(X1_STEP_PIN, X1_DIR_PIN, X1_ENDSTOP_PIN, Motor::stepsPerMM_TIMING_BELT);
    x2.init(X2_STEP_PIN, X2_DIR_PIN, X2_ENDSTOP_PIN, Motor::stepsPerMM_TIMING_BELT);
   
    y.init(Y_STEP_PIN, Y_DIR_PIN, Y_ENDSTOP_PIN, stepsPerMM_LEADSCREW);
    z.init(Z_STEP_PIN, Z_DIR_PIN, Z_ENDSTOP_PIN, Motor::stepsPerMM_TIMING_BELT);
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
      return &y;
      break;
    case MotorHandler::m_z:
      return &z;
      break;
//    case MotorHandler::m_e:
      //return &e;
//      break;
  }
  return 0;
}

void MotorHandler::tick() { //update logic
    
  x1.tick();
  x2.tick();
  y.tick();
  //e?.tick();
  z.tick();

 
}

void MotorHandler::ready(bool b) {
    x1.ready = x2.ready = y.ready = z.ready = b;
}


byte MotorHandler::finished() {
    if(!x1.busy && !x2.busy && !y.busy && !z.busy) {
        return RAP::MOTOR_FINISHED;
    } else {
        return RAP::MOTOR_RUNNING;
    }
}


