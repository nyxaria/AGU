## Quick and dirty program that scrapes herb information from
## http://herbgardening.com
## and appends it to a csv file (~/portfolio/AUG/Utility Programs/plantinfo.txt)
## Made by nyxaria

import requests
from bs4 import BeautifulSoup
import re


herbs = [];

page = requests.get("http://herbgardening.com/growingmint.htm")
soup = BeautifulSoup(page.content, 'html.parser')
html = list(soup.children)[2];
body = list(html.children)[3];
herbList = list(list(list(list(list(body.children)[6])[17])[3])[1]);
herbList += list(list(list(list(list(body.children)[6])[17])[3])[3]);
herbList = str(herbList);
herbList = herbList.replace("\n","").replace(" ","");

for raw in herbList.split("HowToGrow"):
    if(not raw.startswith("[")):
        if("/" in [raw[:raw.find("<")].lower()]):
            herbs += [raw[:raw.find("<")].lower().split("/")[1]]
            continue;
        herbs += [raw[:raw.find("<")].lower()];
print(herbs);
#print([m.end() for m in re.finditer("HowToGrow", herbList)]);
for herb in herbs:
    page = requests.get("http://herbgardening.com/growing"+herb+".htm")
    if(page.status_code == 200):#success
        soup = BeautifulSoup(page.content, 'html.parser')

        
        html = list(soup.children)[2];
        #body = list(html.children)
        #content = list(list(body.children)[6])[15];

        
        plantSpacingRaw = str(list(html)).replace(" ", "").replace("\n","").split("PlantSpacing")[1].split("cm");
        numbers = [];
        for entry in plantSpacingRaw:
            #print(entry);

            raw = entry[entry.rfind("(") + 1:]
            if(raw[0].isdigit() and "days" not in raw):
                if("and" in raw):
                    for i in raw.split("and"):
                        numbers += [float(i)];
                elif("-" in raw):
                    for i in raw.split("-"):
                        numbers += [float(i)];
                else:
                    numbers += [float(raw)];
        plantSpacing = int(sum(numbers)/float(len(numbers)));
        print(herb + "," + str(plantSpacing) + "," );
        with open("/Users/georgehartt/Documents/plantinfo.txt", "a") as myfile:
            myfile.write(herb.capitalize() + ",5,"+str(plantSpacing)+",nd\n");
##        print(strRaw);
        #print([m.start() for m in re.finditer("cm", strRaw)]);
