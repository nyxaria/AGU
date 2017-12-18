plantInfoPath = "/home/pi/Desktop/plantinfo"
meta = "";
width=750
height=750;
with open(plantInfoPath, 'r') as f:
    for line in f:
            data = line.split(",");
            if(data[0] != "Crop"):
                    meta += data[0] + "|";

meta = meta[:-1];
meta += "," + str(width) + "|" + str(height);
print(meta);
