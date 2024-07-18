import os.path
import re
import sys
import yaml

if len(sys.argv) != 2:
    print("Expecting the xml file as argument")
    exit(-1)
if not os.path.isfile(sys.argv[1]):
    print("Expecting the xml file as argument")
    exit(-1)

file_path = sys.argv[1]

with open(file_path, 'r') as file:
    result = file.read()
pattern = re.compile(r'<li>[a-z|_]*')
match  = pattern.findall(result)
tests = []
for i in match:
    i = i.replace("<li>","")
    tests.append(i)

print(yaml.dump(tests))
