#include <AccelStepper.h>
#include <MultiStepper.h>

#include "Arduino.h"
#include "Motor.h"
#include "RpiAP.h"
#include <AccelStepper.h>

byte pinStep, pinDir, pinEn;
byte dist, speed, acceleration;
bool busy = false;
//Queue data structure
struct node {
  char* command;
  struct node *link;
}*front, *rear;

class commandQueue {
  public:
    void insert(char*);
    int del();
    commandQueue() {
      front = NULL;
      rear = NULL;
    }
};

void commandQueue::insert(char* str) {
    node *tmp;
    tmp = new (struct node);
    tmp->command = str;
    tmp->link = NULL;
    if (front == NULL) {
        front = tmp;
    } else {
        rear->link = tmp;
    }
    rear = tmp;
}

int commandQueue::del() {
    node *tmp;
    if (front == NULL) {
      return -1;
    } else {       
        tmp = front;
        front = front->link;
        free(tmp);
        return 1;
    }
} 

//

commandQueue queue;

AccelStepper stepper(AccelStepper::DRIVER);

Motor::Motor(byte pStep, byte pDir, byte pEn, int sPmm) {
   pinStep = pStep;
   pinDir = pDir;
   pinEn = pEn;
   Motor::stepsPerMM = sPmm;
   pinMode(pEn,OUTPUT); // Enable
   digitalWrite(pEn,LOW); // Set Enable low
   stepper._pin[0] = pStep; //changed source code of AccelStepper to enable runtime instantiation
   stepper._pin[1] = pDir;  //
   stepper.enableOutputs(); //
}


int Motor::enqueue(char* req) {
  switch(req[2]) {
    case RpiAP::rotate:
      queue.insert(req);
      //return motor.rotate((byte)req[3], (byte)req[4]); //rotate (x mm) (in 1 = clockwise, 0 = counter-clockwise)
      break;
    case RpiAP::stop:
      return stop();
      break;
    case RpiAP::speed:
      return speed((byte)req[3]); // 0 - 100 % of pre-determined maxSpeed
      break;
    case RpiAP::acceleration:
      return acceleration((byte)req[3]);
      break;
    default:
      return -1;
  }
}

void Motor::tick() {
  if(!busy && front != NULL) {
    char* req = front->command;
    queue.del();
    rotate((byte)req[3], (byte)req[4]);
    busy = true;
  }
}

int Motor::rotate(byte mm, byte dir) {
  if(dir == Motor::CLOCKWISE) {
    dist += mm * Motor::NEMA17_STEPS_PER_MM;
  } else if(dir == Motor::ANTICLOCKWISE) {
    dist -= mm * Motor::NEMA17_STEPS_PER_MM;
  }

  stepper.moveTo(dist);


  
  busy = true;
}

int Motor::speed(byte percentage) {
  //stepper.setMaxSpeed((int) (MAX_SPEED * (percentage/100.0)));
  return 1;
}

int Motor::acceleration(byte percentage) {
  //stepper.setAcceleration((int) (MAX_SPEED * (percentage/100.0)));
  return 1;
}

int Motor::stop() {
    busy = false;
    while(queue.del()); //clear queue
}

int Motor::reset() {
    busy = false;
    while(queue.del()); //clear queue
}

