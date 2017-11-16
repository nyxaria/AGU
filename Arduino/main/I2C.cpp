#include "Arduino.h"
#include "Wire.h"
#include "I2C.h"
#include "RAP.h"

#define I2C_ADDRESS 0x01
#define PACKET_BEGIN 0xff
#define PACKET_END 0xfe
#define PACKET_SIZE 32

byte data = 254;
int out = 0;
int toRead = 0;
bool done = false;
byte dataBuffer[PACKET_SIZE + 1];


void sendData() {
    Wire.write(out);
}

void receiveData(int byteCount){
  while(Wire.available()) {
    data = Wire.read();
    if(data == PACKET_BEGIN) {
      toRead = PACKET_SIZE - 1;
      for(byte i = 0; i < PACKET_SIZE; i++) {
        dataBuffer[i] = 0;
      }
      //out = 0;
    } else if(data == PACKET_END) {
      out = RAP::DATA_RECEIVED; //notify pi we got the data
      sendData();                 //
      done = true;
      toRead = -1;
    } else if(toRead != -1) {
      out = data;
      dataBuffer[(PACKET_SIZE - 1) - toRead--] = data;
    }
  }
}

void I2C::init() {
  Wire.begin(I2C_ADDRESS);
  Wire.onReceive(receiveData);
  Wire.onRequest(sendData);
}

I2C::I2C() {
}


byte* I2C::get() {
  if(ready()) {
    done = false;
    return dataBuffer;
  } else {
    return NULL;
  }
}

void I2C::send(int i) {
    out = i;
    sendData();
}

bool I2C::ready() {
  return done;
}


