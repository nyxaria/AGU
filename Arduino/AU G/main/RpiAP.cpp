#include "Arduino.h"
#include "RpiAP.h"
#include "MotorHandler.h"
#include "Motor.h"

MotorHandler motors;





RpiAP::RpiAP() {
  //
}

int RpiAP::handleRequest(char* req) {
  //queue.insert(req);
  switch(req[0]) {
    case RpiAP::motor:
      return motorRequest(req);
      break;
    case RpiAP::read:
      return readRequest(req);
      break;
    case RpiAP::write:
      return writeRequest(req);
      break;
    default:
      return RpiAP::FAILURE; //unknown request
  }
}

int RpiAP::motorRequest(char* req) { // Motor/1st
  switch(req[1]) {
    case RpiAP::x1:
      return motors.get(MotorHandler::m_x1).enqueue(req);
      break;
    case RpiAP::x2:
      return motors.get(MotorHandler::m_x2).enqueue(req);
      break;
    case RpiAP::y1:
      return motors.get(MotorHandler::m_y1).enqueue(req);
      break;
    case RpiAP::y2:
      return motors.get(MotorHandler::m_y2).enqueue(req);
      break;
    case RpiAP::z:
      return motors.get(MotorHandler::m_z).enqueue(req);
      break;
    default:
      return RpiAP::FAILURE; //unknown code
  }
}

int RpiAP::readRequest(char* req) {
  /*switch(req[1]) {
    case RpiAP::soilHumidity:
      return sensors.soilHumidity();
      break;
    case RpiAP::distance:
      switch(req[2]) {
        case RpiAP::x1:
          return sensors.distX1();
          break;
        case RpiAP::x2:
          return sensors.distX2();
          break;
        case RpiAP::y1:
          return sensors.distY1();
          break;
        case RpiAP::y2:
          return sensors.distY2();
          break;
        case RpiAP::z:
          return sensors.distZ();
          break;
        default:
          return RpiAP::FAILURE; //unknown code
      }
      break;
    default:
      return RpiAP::FAILURE; //unknown code
  }*/ return RpiAP::FAILURE;
}

int RpiAP::writeRequest(char* req) {
  /*switch(req[1]) {
    case RpiAP::water:
      return controller.water(req[2]); // 1 = on, 0 = off
      break;
    default:
      return -1; //unknown code
  }*/ return RpiAP::FAILURE;
}




