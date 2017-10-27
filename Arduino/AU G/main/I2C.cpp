#include "Arduino.h"
#include "Wire.h"
#include "I2C.h"
#include "RpiAP.h"

#define I2C_ADDRESS 0x01
#define PACKET_BEGIN 0xff
#define PACKET_END 0xfe
#define PACKET_SIZE 32

int data = 254;
int out = 0;
int toRead = 0;
bool done = false;
char dataBuffer[PACKET_SIZE + 1];


void sendData() {
  Wire.write(out);
}

void receiveData(int byteCount){
  while(Wire.available()) {
    data = Wire.read();
    if(data == PACKET_BEGIN) {
      toRead = PACKET_SIZE - 1;
      for(int i = 0; i < PACKET_SIZE; i++) {
        dataBuffer[i] = '\0';
      }
      out = 0;
    } else if(data == PACKET_END) {
      out = RpiAP::DATA_RECEIVED; //notify pi we got the data
      sendData();                 //
      Serial.print("RECIEVED DATA: ");
      Serial.println(dataBuffer);
      done = true;
      toRead = -1;
    } else if(toRead != -1) {
      dataBuffer[(PACKET_SIZE - 1) - toRead--] = (char) data;
    }
  }
}


I2C::I2C() {
  Wire.begin(I2C_ADDRESS);
  Wire.onReceive(receiveData);
  Wire.onRequest(sendData);
}


char* I2C::get() {
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

