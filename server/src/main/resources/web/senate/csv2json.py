import csv
import json

lastgroup = ''
data = []
i = 0
dReader = csv.reader(open('data.csv'),delimiter=",")
for row in dReader:
  if row[5].strip() != lastgroup:
    if i != 0:
      data.append(group)
    lastgroup = row[5].strip()
    group = {
      "abbreviation": row[5].strip(),
      "name": row[7].strip(),
      "color": row[6].strip(),
      "people": []
    }
  if int(row[4]) == 0:
    op = "green"
  else:
    op = "gray"
  person = {
    "name": row[2].strip() + " - " + row[1].strip() + "<br/>" +row[7].strip(),
    "opacity": 1,
    "background": op
  }
  group['people'].append(person)
  i = i + 1
data.append(group)
with open('hemicycle.json', 'w') as outfile:
  json.dump(data, outfile)
