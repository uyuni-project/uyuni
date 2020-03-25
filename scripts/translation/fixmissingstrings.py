#!/usr/bin/python

import sys
import os
import re

SAVE = False

skip = ["emptyspace.jsp"]
idpattern = re.compile('id="([^"]+)"')
sourcepattern = re.compile(r"<source>.*</source>", re.S)

def getid(line):
    try:
        return idpattern.search(line).group(1)
    except AttributeError as e:
        print("Error in line '{0}': {1}".format(line, e))
        sys.exit(1)


def align(orig, translation):
    #import pdb; pdb.set_trace()yy
    newfile = []
    node = ""
    source = ""
    insource = False
    currentid = None
    with open(orig) as o:
        print("Orig: {0}".format(orig))
        for oline in o:
            if not currentid and "trans-unit" in oline:
                currentid = getid(oline)
                print("id: {0}".format(currentid))
                node += oline
            elif currentid and "<source>" in oline and "</source>" in oline:
                source += oline
                node += oline
            elif currentid and "<source>" in oline:
                source += oline
                node += oline
                insource = True
            elif insource and "</source>" in oline:
                source += oline
                node += oline
                insource = False
            elif currentid and "trans-unit" in oline:
                node += oline
                newfile.append((currentid, node, source))
                currentid = None
                node = ""
                source = ""
            elif currentid:
                node += oline
                if insource:
                    source += oline
            else:
                newfile.append(oline)
    with open(translation) as t:
        print("translation: {0}".format(translation))
        for tline in t:
            if not currentid and "trans-unit" in tline:
                #import pdb; pdb.set_trace()
                currentid = getid(tline)
                print("id: {0}".format(currentid))
                node += tline
            elif currentid and "trans-unit" in tline:
                node += tline
                found = False
                for n, item in enumerate(newfile):
                    if isinstance(item, tuple) and item[0] == currentid:
                        nd = sourcepattern.sub(item[2].strip(), node)
                        newfile[n] = (item[0], nd, item[2])
                        found = True
                        break;
                if not found:
                    print("Node not found: {0}".format(node))
                node = ""
                currentid = None
            elif currentid:
                node += tline
            elif "target-language" in tline:
                for n, item in enumerate(newfile):
                    if isinstance(item, str) and "source-language" in item:
                        newfile[n] = tline

    if SAVE:
        translation += ".new"
    with open(translation, "w") as new:
        for line in newfile:
            if isinstance(line, tuple):
                new.write(line[1])
            else:
                new.write(line)


files = os.listdir('.')
#print files

for translation in files:

    if translation.startswith('StringResource_') and translation.endswith('.xml') and translation != 'StringResource_en_US.xml':
        #print 'processing ' + str(file)
        align('StringResource_en_US.xml', translation)

