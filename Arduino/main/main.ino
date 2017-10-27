 #include "I2C.h"
#include "RpiAP.h"
#include "MotorHandler.h"

MotorHandler motorHandler;
I2C i2c;
RpiAP rap; //raspberry pi - arduino - protocol (names are not my strong point)

void setup() {
    Serial.begin(9600);
    pinMode(13, OUTPUT);
    motorHandler.init();
    rap.motors = &motorHandler;
    motorHandler.get(MotorHandler::m_x1)->s._targetPos = 500*16;
    motorHandler.get(MotorHandler::m_x2)->s._targetPos = 500*16;

    //rap.handleRequest("Mxrff");
    //rap.handleRequest("MXrff");
}

void loop() {
    if (i2c.ready()) { //check for data from rpi
        char* data = i2c.get();
        int result = rap.handleRequest(data);
        
        i2c.send(result); //respond to rpi
    }
//
//      motorHandler.get(MotorHandler::m_x1)->s.run();
//      motorHandler.get(MotorHandler::m_x2)->s.run();
//      motorHandler.get(MotorHandler::m_y)->s.run();
//      motorHandler.get(MotorHandler::m_e)->s.run();
//     motorHandler.get(MotorHandler::m_z)->s.run();
      motorHandler.tick();
    //delay(100);
}

