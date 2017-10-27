## Quick and dirty program that reformats the list from 
## http://www.greeneducationfoundation.org/greenthumbchallengesub/start-up-kit/grow-plant-your-garden/496-vegetable-planting-chart.html (see ~/portfolio/AUG/Utility Programs/plantInfo_raw.txt)
## into a csv file
## 
## Made by nyxaria

i = 0; #counter
out = open("/Users/georgehartt/Documents/plantinfo.txt","w",encoding='utf-8');

def hasNumbers(inputString):
    return any(char.isdigit() for char in inputString)

with open ("/Users/georgehartt/Documents/plantInfo_raw.txt", "r",encoding='utf-8') as raw:
    for line in raw:
        if(line != "\n"):
            print(str(i) + " : " + line.replace("\n",""))
            if(i >= 0):
                if(not hasNumbers(line)):
                    out.write(line.replace("\n",""));
                else:
                    line = line.replace('"',"").replace(" ","").replace("\n","");
                    numbers = [];
                    for number in line.split("-"):
                        if("/" in number):
                            fraction = number.split("/");
                            numbers.append(10 * 2.54 * float(fraction[0]) / float(fraction[1])); #convert to mm
                        else:
                            numbers.append(10 * 2.54 * float(number));

                    out.write(str(int(sum(numbers)/float(len(numbers))))); #2dp, write average of numbers
                if(i == 2):
                    out.write("\n");
                    i = -4;
                else:
                    out.write(",");
            i+=1;
            

out.close();
