#include "Arduino.h"
#include "MotorHandler.h"
#include "Motor.h"

#define X_STEP_PIN         54
#define X_DIR_PIN          55
#define X_ENABLE_PIN       38
#define X_CS_PIN           53
#define X_STEPS_PER_MM     -1

#define Y_STEP_PIN         60
#define Y_DIR_PIN          61
#define Y_ENABLE_PIN       56
#define Y_CS_PIN           49
#define Y_STEPS_PER_MM     -1
#define Y_STEPS_PER_MM     -1

#define Z_STEP_PIN         46
#define Z_DIR_PIN          48
#define Z_ENABLE_PIN       62
#define Z_CS_PIN           40
#define Z_STEPS_PER_MM     -1

#define E0_STEP_PIN        26
#define E0_DIR_PIN         28
#define E0_ENABLE_PIN      24
#define E0_CS_PIN          42

#define E1_STEP_PIN        36
#define E1_DIR_PIN         34
#define E1_ENABLE_PIN      30
#define E1_CS_PIN          44

//implement hashmap<char, Motor> for more abstraction
Motor x1(X_STEP_PIN, X_DIR_PIN, X_ENABLE_PIN, X_STEPS_PER_MM);
Motor x2(E0_STEP_PIN, E0_DIR_PIN, E0_ENABLE_PIN, X_STEPS_PER_MM);
Motor y1(Y_STEP_PIN, Y_DIR_PIN, Y_ENABLE_PIN, Y_STEPS_PER_MM);
Motor y2(E1_STEP_PIN, E1_DIR_PIN, E1_ENABLE_PIN, Y_STEPS_PER_MM);
Motor z(Z_STEP_PIN, Z_DIR_PIN, Z_ENABLE_PIN, Z_STEPS_PER_MM);

MotorHandler::MotorHandler() {

}

Motor MotorHandler::get(byte key) {
  switch (key) {
    case MotorHandler::m_x1:
      return x1;
      break;
    case MotorHandler::m_x2:
      return x2;
      break;
    case MotorHandler::m_y1:
      return y1;
      break;
    case MotorHandler::m_y2:
      return y2;
      break;
    case MotorHandler::m_z:
      return z;
      break;
  }
}

void MotorHandler::tick() { //update logic
  x1.tick();
  x2.tick();
  y1.tick();
  y2.tick();
  z.tick();
}

