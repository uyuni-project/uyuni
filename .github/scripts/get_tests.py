import re
import sys
import yaml

if len(sys.argv) != 2:
    print("Expecting the XML string as argument")
    exit(-1)

xml_string = sys.argv[1]
pattern = re.compile(r'<li>([\w\-]+)</li>')
matches = pattern.findall(xml_string)
print(yaml.dump(matches))
