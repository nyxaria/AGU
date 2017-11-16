#include "Arduino.h"
#include "RAP.h"
#include "MotorHandler.h"
#include "Motor.h"
#include "SensorHandler.h"

int RAP::handleRequest(byte* req) {
  //queue.insert(req);
  switch(req[0]) {
    case RAP::motor:
      return motorRequest(req);
      break;
    case RAP::read:
      return readRequest(req);
      break;
    case RAP::write:
      return writeRequest(req);
      break;
    default:
      return RAP::FAILURE; //unknown request
  }
}

int RAP::motorRequest(byte* req) { // Motor/1st
    motors->ready(req[1] == RAP::ready);

    if(req[1] == RAP::ready) {
        return RAP::SUCCESS;
    }
    
  switch(req[1]) {
    case RAP::x1:
      return motors->get(MotorHandler::m_x1)->enqueue(req);
      break;
    case RAP::x2:
      return motors->get(MotorHandler::m_x2)->enqueue(req);
      break;
    case RAP::y:
      return motors->get(MotorHandler::m_y)->enqueue(req);
      break;
//    case RAP::e:
//      return motors->get(MotorHandler::m_e)->enqueue(req);
//      break;
    case RAP::z:
      return motors->get(MotorHandler::m_z)->enqueue(req);
      break;
    default:
      return RAP::FAILURE; //unknown code
  }
}

int RAP::readRequest(byte* req) {
  switch(req[1]) {
    case RAP::soilHumidity:
      return sensors->soilHumidity();
      break;
    case RAP::distance:
      return sensors->distance();
      break;
    default:
      return RAP::FAILURE; //unknown code
  }
}

int RAP::writeRequest(byte* req) {
  switch(req[1]) {
    case RAP::water:
      if(req[2] == 1) {
        digitalWrite(38, HIGH);
      } else if(req[2] == 0) {
        digitalWrite(38, LOW);
      } 
      return RAP::SUCCESS;
      break;
      case RAP::vacuum:
      if(req[2] == 1) {
        digitalWrite(32, HIGH);
      } else if(req[2] == 0) {
        digitalWrite(32, LOW);
      } 
      return RAP::SUCCESS;
      break;
    default:
      return RAP::FAILURE; //unknown code
  }
}





