import smbus
import time
import subprocess
bus = smbus.SMBus(1);

address = 0x01;
number = 0;
MAX_STRING_LENGTH = 30;

def main():
    print("\n"*50);
    print("STARTING...");
    readNumber();
    i = 0;
    data = [];
    datas = [];
    while True:
        while True:
            i = int(input("bit: "));
            if(i == -1):
                datas += [data];
                data = [];
                continue;
            if(i == -2):
                break;
            data += [i];
        for l in datas:
            writeString(l);
        datas = [];
        
def writeNumber(value):
    bus.write_byte(address, value);
    return -1

def readNumber():
    number = bus.read_byte(address);
    return number

def writeString(data):
    writeNumber(255);

    for char in data:
        writeNumber(char);

    writeNumber(254);
    print("REQUEST: Sent '{}' to address {}".format(data, hex(address)));
    if(readNumber() == 125): #sent flag that received data, sometimes response gets sent already so it just falls thru
        print("\t Device at {} recieved '{}'. Waiting for response...".format(hex(address), data));
        
    while(True):
        if(readNumber != 125): #wait for response
            break;
    time.sleep(10);
    returnCode = readNumber();
    print("\t Response = {}".format(returnCode));
    return returnCode;

if(__name__ == "__main__"):
    main();
