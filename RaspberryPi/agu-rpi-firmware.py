import os
import subprocess
import glob
import time
import smbus
import pickle
from bluetooth import *
import threading
import datetime

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

CONTROLLER_REFRESH_RATE = 0.1;

bus = smbus.SMBus(1);

address = 0x01;
number = 0;
MAX_STRING_LENGTH = 30;
SEED_BANK = 50;
plantInfoPath = "/home/pi/Desktop/plantinfo";
potsPath = "/home/pi/Desktop/pots";
seedsPath = "/home/pi/Desktop/seeds";
metaDataPath = "/home/pi/Desktop/metaData";
width = 750;
height = 750;
pots = [];
seeds= [];
plants = {};
TYPE_RECT = 0;
TYPE_CIRCLE = 1;
offset = {};
positions = {};

port = server_sock.getsockname()[1]

uuid = "e77fa4a0-b21c-11e7-8f1a-0800200c9a66"
global toSend;
global commsThread;
global scheduleThread;
global controlThread;
global client_sock;
global meta;


def main():
        global commsThread, controlThread, scheduleThread;
        
        loadData();
        
        controlThread = threading.Thread(target=controllerThread);
        controlThread.start();
        scheduleThread = threading.Thread(target=scheduleThread);
        scheduleThread.start();

        initHardware();

        commsThread = threading.Thread(target=setUpComms);
        commsThread.start();

        
 
        
def connected():
        initFrontEnd();

## inits ##
        
def initHardware(): #on startup
        if(curX == 0 and curZ == 0): #dont home on origin
                moveTo(50,50,-1);
        home(False,False, True);
        home(True, True, False);
        print("\nHARDWARE READY");

        
def initFrontEnd(): #on BT connected
        prepareMeta();
        writeBtComms(meta);
        time.sleep(1);
        for seed in seeds:
                if(seed.seed != b'Empty'):
                        writeBtComms("SEED:"+str(seed)+":");
                        time.sleep(0.15);

        for pot in pots:
                writeBtComms("POT:"+str(pot)+":");
                time.sleep(0.1);
        
### -------------- ###

global now;
global next_start;

SCHEDULE_DAILY_HOUR = 20;

def scheduleThread():
        print("SCHEDULES READY\n---------");
        while True:
                now = datetime.datetime.now();
                next_start = datetime.datetime(now.year, now.month, now.day, SCHEDULE_DAILY_HOUR, 0, 0);
                if(now >= next_start):
                        next_start += datetime.timedelta(1);
                        tendCrops();

                time.sleep(60);

def tendCrops():
        if(len(pots) > 0):
                for pot in pots:
                        print("["+time.strftime("%H:%M:%S")+"] WATERING " + str(pot.seed) + " AT ("+ str(pot.x) +","+str(pot.y)+")")
                        waterPot(pot);
                        pot.active += 1;
                        
### Persistance + Data management ###

def save(mode):
        global pots, seeds;
        if(mode == 0): #pots
                file = open(potsPath, "wb");
                pickle.dump(pots,file);
                file.close();
        elif(mode == 1): #seeds
                file = open(seedsPath, "wb");
                pickle.dump(seeds,file)
                file.close();

        with open(metaDataPath, "r") as file:
                data = [line.rstrip("\n") for line in file];
        data[8] = currentAttachment;
        with open(metaDataPath, "w") as file:
                for item in data:
                        file.write("%s\n" % item);

def loadData():
        global pots, seeds, offset;
        prepareMeta();
        pots = [];
        seeds = [];
        filePots = open(potsPath, "rb");
        if(os.stat(potsPath).st_size != 0):
                pots = pickle.load(filePots);
        filePots.close();
        fileSeeds = open(seedsPath, "rb")
        if(os.stat(potsPath).st_size != 0):
                seeds = pickle.load(fileSeeds)
        fileSeeds.close();

        
        data = [line.strip("\n") for line in open(metaDataPath)];

        offset[SOIL_HUMIDITY_SENSOR] = int(data[0]);
        offset[ULTRA_SOUND_SENSOR] = int(data[1]);
        offset[VACUUM_BASE] = int(data[2]);
        offset[SCIENTIFIC_STICK] = int(data[3]);

        positions[ULTRA_SOUND_SENSOR] = data[4];
        positions[SOIL_HUMIDITY_SENSOR] = data[5];
        positions[VACUUM_BASE] = data[6];
        positions[SEED_BANK] = data[7]
        currentAttachment = int(data[8]);
        

def prepareMeta():
        global width,height;
        global plantInfoPath;
        global plants;
        global meta;
        meta = "META:";
        with open(plantInfoPath, 'r') as f:
            for line in f:
                    data = line.split(",");
                    if(data[0] != "Crop"):
                            plants[str(data[0])] = {};
                            plants[str(data[0])]["depth"] = int(data[1]);
                            plants[str(data[0])]["spacing"] = int(data[2]);
                            plants[str(data[0])]["water"] = data[3];
                            plants[str(data[0])]["vacuum_attachment"] = int(data[4]);
                            meta += data[0] + "~";
            f.close()

        meta = meta[:-1];
        meta += "," + str(width) + "~" + str(height);

### -------------- ###
        

### Hardware control ###

## Protocol constants

#motor node
motor = 0;

x1 = 0;
x2 = 1;
y = 2;
e = 3;
z = 4;

rotate = 0;
stop = 1;
speed = 2;
reset = 3;

ready = 5;

#read mpde
read = 1

soilHumidity = 0;
distance = 1;

#write node
write = 2;

wtr = 0;
_vacuum = 1;

on = 1;
off = 0;

#flags
DEFAULT = 0;
MOTOR_FINISHED = 50
MOTOR_RUNNING = 51;
DATA_RECEIVED = 125;
FAILURE = 126;
SUCCESS = 127;


#vars

NONE = -1;
ULTRA_SOUND_SENSOR = 0;
SOIL_HUMIDITY_SENSOR = 1;


VACUUM_BASE = 9;

GAUGE14_VACUUM = 10;
GAUGE16_VACUUM = 11;
GAUGE18_VACUUM = 12;
GAUGE20_VACUUM = 13;
GAUGE22_VACUUM = 14;
GAUGE25_VACUUM = 15;
GAUGE27_VACUUM = 16;
GAUGE30_VACUUM = 17;
SCIENTIFIC_STICK = 18;

VACUUM_ATTACHMENT_SPACING = 20;

currentAttachment = -1;
##
timeOn = 0;
waterActive = False;
delta_height = 600;
delta_hole = 2;
offset_attachment = 50;
seedbankSpacing = 22;

curX = 0;
curY = 0;
curZ = 0;

ATTACHMENTS_X = 0;
#Low level commands
def controllerThread():
        global timeOn, waterActive;
        while True:
                if(waterActive):
                        if(int(round(time.time()*1000)) - timeOn > 0):
                            timeOn = 0;
                            writeWater(off,0);
                            waterActive = False;

def flush():
        writeI2CData([motor, ready]);

def homeMotor(motorCode): #int 
        global curY, curZ, curY;
        writeI2CData([motor, motorCode, reset]);
        if(motorCode == x1 or motorCode == x2):
                curX = 0;
        elif(motorCode == z):
                curZ = 0;
        elif(motorCode == y):
                curY = 0;

def moveMotorTo(motorCode, distMM): #int, int
        global curY, curZ, curY;
        low = distMM%255;
        high = distMM//255;

        if(low == 255 or low == 254): #255 and 254 = start and end of bit flags
                low = 0; #error = 2mm in worst case , worth redoing ? strict packet format. 
                high += 1;
        
        writeI2CData([motor, motorCode, rotate, high, low]);
        if(motorCode == x1 or motorCode == x2):
                curX = distMM;
        elif(motorCode == z):
                curZ = distMM;
        elif(motorCode == y):
                curY = distMM;

def stopMotor(motorCode): #int
        writeI2CData([motor, motorCode, stop]);

def speedMotor(motorCode, percentage): #int, int
        writeI2CData([motor, motorCode, percentage]); 

def writeWater(state, t): #int, int
        global timeOn, waterActive;
        if(t != -1):
                if(state == on):
                        waterActive = True;
                        timeOn = int(round(time.time()*1000 + t))
        writeI2CData([write, wtr, state]);

def writeVacuum(state): #int
        writeI2CData([write, _vacuum, state]);

def readHumidity():
        writeI2CData([read, soilHumidity]);

def readDistance():
        writeI2CData([read, distance]);

def waitMotorFinished():
        while True:#
                if(readI2CByte() == MOTOR_FINISHED):
                        break;

def waitActionFinished():
        while True:
                if(readI2CByte() != DATA_RECEIVED):
                        break;
                
#Medium-level Commands/Sequences

def vacuum(state):
        writeVacuum(state);
        waitActionFinished();

def water(state, t):
        writeWater(state, t);
        waitActionFinished();

def getSoilHumidity():
        readHumidity();
        waitActionFinished();
        return readI2CByte();
def getDistance():
        readDistance();
        waitActionFinished();
        pot.height = readI2CByte();

def moveTo(targetX, targetZ, targetY):
        if(targetX != -1):
                moveMotorTo(x1, targetX);
                moveMotorTo(x2, targetX);
        if(targetZ != -1):
                moveMotorTo(z, targetZ);
        if(targetY != -1):
                moveMotorTo(y, targetY);
        flush();
        waitMotorFinished();

def home(homeX, homeZ, homeY):
        if(homeX):
                homeMotor(x1);
                homeMotor(x2);
        if(homeZ):
                homeMotor(z);
        if(homeY):
                homeMotor(y);
        flush();
        waitMotorFinished();
        if(homeY):
                moveTo(-1,-1,10)


def moveToAttachment(attachment):
        global currentAttachment, positions;
        if(currentAttachment == attachment):
                return;


        if(attachment == ULTRA_SOUND_SENSOR):
                xOff = 16;
                zOff = 1 
                moveTo(59 + xOff,191 + zOff,525);
                
                moveTo(44 + xOff ,190+ zOff,565);
                
                moveTo(30-5+ xOff,208 + zOff,578);
                moveTo(30-5,205,581);
                moveTo(65,203,578);
                moveTo(-1,-1,520);
                moveTo(59+xOff, 191 + zOff, 505)
                
                currentAttachment = attachment;

                return;
        if(attachment == SOIL_HUMIDITY_SENSOR):
                moveTo(39,256,525);
                moveTo(40,255,565);
                moveTo(34,265,580);
                moveTo(31,265,582);
                moveTo(60,265,580);
                moveTo(60,280,575);
                moveTo(47,280,575);
                moveTo(60,280,575);
                moveTo(60,280,550);
                moveTo(39,256,525);
                currentAttachment = attachment;
                return;

        #this dosen't work =( ah the optimism of a software guy.
        pts = [];
        if(attachment > 9 and attachment < 20):
                pts += positions[VACUUM_BASE].split(",");
                print(pts);
                moveTo(int(str(pts[0])), int(str(pts[1])) + (attachment - 10)*VACUUM_ATTACHMENT_SPACING, -1);
        moveTo(-1, -1, int(str(pts[2])));

        
        elif(attachment > 9 and attachment < 20):
                vacuum(on);
                time.sleep(1);

        moveTo(-1, -1, int(str(pts[2])) - offset_attachment);

        
def depositAttachment(attachment):
        global currentAttachment, positions;
        if(currentAttachment == NONE and attachment < 9):
                return;

        if(attachment == ULTRA_SOUND_SENSOR):
                xOff = 16;
                zOff = 1 
                moveTo(59 + xOff,200 + zOff,525);
                moveTo(80,200+zOff,578);
                moveTo(30-5+ xOff,202 + zOff,576);
                moveTo(-1,-1,520);                
                moveTo(59 + xOff,191 + zOff,525);
                currentAttachment = NONE;

                return;
        if(attachment == SOIL_HUMIDITY_SENSOR):
                xOff = 16;
                zOff = 41; 
                moveTo(59 + xOff,200 + zOff,525);
                moveTo(80,200+zOff,578);
                moveTo(30-5+ xOff,202 + zOff,576);
                moveTo(-1,-1,520);                
                moveTo(59 + xOff,191 + zOff,525);
                currentAttachment = NONE;
                return;


        pts = [];
        if(attachment > 9 and attachment < 20):
                pts += positions[VACUUM_BASE].split(",");
                moveTo(int(str(pts[0])), int(str(pts[1])) + (attachment - 10)*VACUUM_ATTACHMENT_SPACING, -1);
        
        moveTo(-1, -1, int(str(pts[2])));

        if(attachment > 9 and attachment < 20):
                vacuum(off);
                time.sleep(1);

        moveTo(-1, -1, int(str(pts[2])) - offset_attachment);

    
def waterPot(pot):
        global pots, plants;
        targetX = 0;
        targetY = 0;
        water = plants[str(pot.seed)[2:-1]]["water"];
        radius = 0;

        if(pot.Type == TYPE_RECT):
                targetX = pot.x + (pot.xDim/2);
                targetY = pot.y + (pot.yDim/2);
                radius = min(pot.xDim/4, pot.yDim/4);
        elif(pot.Type == TYPE_CIRCLE):
                targetX = pot.x;
                targetY = pot.y;
                radius = min(pot.xDim/2, pot.yDim/2);


        if(currentAttachment != ULTRA_SOUND_SENSOR):
                depositAttachment();
                moveToAttachment(ULTRA_SOUND_SENSOR);

        home(False, False, True);
        moveTo(targetX + radius, targetY + radius, -1);

        if(pot.height == 0):
                pot.height = getDistance() - offset[ULTRA_SOUND_SENSOR];
                pot.archive += [pot.height];
        else:
                pot.archive += [getDistance() - offset[ULTRA_SOUND_SENSOR]];

        depositAttachment(currentAttachment);
        moveToAttachment(HUMIDITY_SOIL_SENSOR);

        home(False, False, True);
            
        pot.water = 0;
        moveTo(targetX + radius, targetY + radius, -1);

        if(water == "nd"):
                while(pot.water < pot.threshold):
                        moveTo(-1, -1, 300);
                        water(on, -1);

                        moveTo(targetX - radius, targetY + radius);
                        moveTo(targetX - radius, targetY - radius);
                        moveTo(targetX + radius, targetY - radius);
                        moveTo(targetX + radius, targetY + radius);

                        water(off, -1);
                        time.sleep(10);

                        moveTo(-1, -1, pot.height - offset[SOIL_HUMIDITY_SENSOR]);
                        pot.water = getSoilHumiditiy();
                
        else:
                water(on,int(round(float(pot.water)/float(float(240)/1000/60/60))));
                
                while(int(round(time.time()*1000)) - timeOn > 0):
                        if(waterActive):
                                break;
                        moveTo(targetX - radius, targetY + radius);
                        if(waterActive):
                                break;
                        moveTo(targetX - radius, targetY - radius);
                        if(waterActive):
                                break;
                        moveTo(targetX + radius, targetY - radius);
                        if(waterActive):
                                break;
                
                time.sleep(10);
                moveTo(-1, -1, pot.height - offset[SOIL_HUMIDITY_SENSOR]);
                pot.water = getSoilHumidity();

        home(False, False, True);
        pot.dayCount += 1;
            

def seedPot(pot):
        global pots, plants;
        targetX = 0;
        targetY = 0;
        depth = plants[pot.seed]["depth"];

        if(pot.Type == TYPE_RECT):
                targetX = pot.x + (pot.xDim/2);
                targetY = pot.y + (pot.yDim/2);
        elif(pot.Type == TYPE_CIRCLE):
                targetX = pot.x;
                targetY = pot.y;

        if(pot.height == 0):
                if(currentAttachment != ULTRA_SOUND_SENSOR):
                        depositAttachment(currentAttachment);
                moveToAttachment(ULTRA_SOUND_SENSOR);

                home(False, False, True);
                moveTo(targetX, targetY, -1);
                
                pot.height = getDistance() - offset[ULTRA_SOUND_SENSOR];
                pot.archive += [pot.height];

        
        depositAttachment(currentAttachment);

        home(False, False, True);
        moveToAttachment(plants[pot.seed]["vacuum_attachment"]);
        getSeed(pot.seed);

        home(False, False, True);
        moveTo(targetX, targetY, -1);
        moveTo(-1,-1, pot.height - offset[VACUUM_BASE] + depth-10);
        vacuum(off);
        moveTo(-1,-1, pot.height - offset[VACUUM_BASE] + depth+5);
        moveTo(-1,-1, pot.height - offset[VACUUM_BASE] + depth+10);
        vacuum(on);
        ###

        home(False, False, True);
        depositAttachment(plants[pot.seed]["vacuum_attachment"]);

        vacuum(off);

def getSeed(seed):
        for seedbank in seeds:
                if(seedbank.seed == seed):
                        pts = positions[VACUUM_BASE].split(",");
                        moveTo(int(str(pts[0])), int(str(pts[1])) + seedbank.index*seedbankSpacing, -1);
                        moveTo(-1, -1, int(str(pts[2])));
                        vacuum(on);
                        
### I2C Comms ###

def writeI2CByte(value):
    bus.write_byte(address, int(value));


def readI2CByte():
    try:
        return(bus.read_byte(address));
    except IOError:
        print("error");
    return number

def writeI2CData(data):
    if(data == []):
            return;

    try:
            writeI2CByte(255); #flag start

            for byte in data:
                    writeI2CByte(byte);

            writeI2CByte(254); #flag end
            print("I2C-send: '{}' to address {}".format(data, hex(address)));
   
            returnCode = readI2CByte();
            print("\t Response = {}".format(returnCode));
    except IOError:
            print("ioerror");
            subprocess.call(["i2cdetect","-y","1"]);
            writeI2CData(data);
            print("trying again");
    #return returnCode;

###

        
### Bluetooth Comms ###

def setUpComms():
        global connected;
        global client_sock;
        advertise_service( server_sock, "AGU",
				   service_id = uuid,
				   service_classes = [ uuid, SERIAL_PORT_CLASS ],
				   profiles = [ SERIAL_PORT_PROFILE ],
#                   protocols = [ OBEX_UUID ]
					)
        while True:
                print("Waiting for connection on RFCOMM channel %d" % port)

                client_sock, client_info = server_sock.accept()
                print("Accepted connection from "+ ''.join(map(str,client_info))+"\n-------")
                connected();
                try:
                        while True:
                                data = client_sock.recv(1024)
                                if len(data) != 0:
                                        processBtData(data);

                except IOError:
                        pass

def writeBtComms(data):
        global client_sock;
        client_sock.send(data);
        print("BT-send: "+data+"\n");

processing = b"POTS"
def processBtData(data):
        global pots, seeds, processing;
        d = data.split(b":");
        print("BT-read: "+str(data)+"\n");
        
        if(d[0] == b"POTS"):
                pots = [];
                for line in d[1].split(b"~"):
                        raw = line.split(b",");
                        x = 0
                        y = 0
                        Type = 0
                        xDim = 0
                        yDim = 0
                        seed = b""
                        water = b""
                        frequency = 1
                        threshold = 50
                        active = b"";
                        archive = [];
                        height = 0;
                        
                        for l in raw:
                                pair = l.split(b"=");
                                if(pair[0]==b"x"):
                                        x = int(pair[1]);
                                elif(pair[0]==b"y"):
                                        y = int(pair[1]);
                                elif(pair[0]==b"type"):
                                        Type = int(pair[1]);
                                elif(pair[0]==b"xDim"):
                                        xDim = int(pair[1]);
                                elif(pair[0]==b"yDim"):
                                        yDim = int(pair[1]);
                                elif(pair[0]==b"seed"):
                                        seed = pair[1];
                                elif(pair[0]==b"water"):
                                        water = int(pair[1]);
                                elif(pair[0]==b"frequency"):
                                        frequency=pair[1];
                                elif(pair[0]==b"threshold"):
                                        threshold=pair[1];
                                elif(pair[0]==b"active"):
                                        active=int(pair[1]);
                                elif(pair[0]==b"archive"):
                                        if(len(pair[1]) > 0):
                                                for entry in pair[1].split(","):
                                                        archive += [int(entry)];
                                                if(len(archive) > 0):
                                                        height = archive[0];
                                                
                        
                        pots += [Pot(x, y, Type, xDim, yDim, seed, water, frequency, threshold, active, archive, height, 0)];
                save(0);
        elif(d[0]== b"SEEDS"):
                seeds = [];
                for line in d[1].split(b"~"):
                        raw = line.split(b",");
                        index = 0
                        seed = b""
                        color = b"";
                        for l in raw:
                                pair = l.split(b"=");
                                if(pair[0]==b"index"):
                                        index = int(pair[1]);
                                elif(pair[0]==b"seed"):
                                        seed = pair[1];
                                elif(pair[0]==b"color"):
                                        color = pair[1];
                        seeds += [Seed(index, seed, color)];
        
                save(1);

### -------------- ###

                
### Class Definitions ###

class Pot:
        def __init__(self, x, y, Type, xDim, yDim, seed, water, frequency, threshold, active, archive, height, dayCount):
                self.x = x;
                self.y = y;
                self.Type = Type;
                self.xDim = xDim;
                self.yDim = yDim;
                self.seed = seed;
                self.water = water;
                self.frequency = frequency;
                self.threshold = threshold;
                self.active = active;
                self.archive = archive;
                self.height = height;
                self.dayCount = dayCount;
        def __repr__(self):
                return str(b"x="+str(self.x).encode("ascii")+b",y="+str(self.y).encode("ascii")+b",type="+str(self.Type).encode("ascii")+b",xDim="+str(self.xDim).encode("ascii")+b",yDim="+str(self.yDim).encode("ascii")+ b",seed="+self.seed+b",water="+str(self.water).encode("ascii")+b",frequency="+str(self.frequency).encode("ascii")+b",threshold="+str(self.threshold).encode("ascii")+ b",active="+str(self.active).encode("ascii")+b",archive="+str(self.archive).replace("[","").replace("]","").replace(",","+").encode("ascii"));
                
class Seed:
        def __init__(self, index, seed, color):
                self.index = index;
                self.seed = seed;
                self.color = color;

        def __repr__(self):
                return str(b"index="+str(self.index).encode("ascii")+b",seed="+str(self.seed).encode("ascii")+b",color="+str(self.color).encode("ascii"));

if(__name__ == "__main__"):
    main();
