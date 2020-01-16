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
    #import pdb; pdb.set_trace()
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
                node += oline
            elif currentid and "trans-unit" in oline:
                node += oline
                newfile.append((currentid, node))
                currentid = None
                node = ""
            elif currentid:
                node += oline
            else:
                newfile.append(oline)
    with open(translation) as t:
        print("translation: {0}".format(translation))
        for tline in t:
            if not currentid and "trans-unit" in tline:
                currentid = getid(tline)
            elif currentid and "<source>" in tline and "</source>" in tline:
                source += tline
            elif currentid and "<source>" in tline:
                source += tline
                insource = True
            elif insource and "</source>" in tline:
                source += tline
                insource = False
            elif currentid and "trans-unit" in tline:
                found = False
                for n, item in enumerate(newfile):
                    if isinstance(item, tuple) and item[0] == currentid:
                        nd = sourcepattern.sub(source, item[1])
                        newfile[n] = (item[0], nd)
                        found = True
                        print("Node found: {0}".format(currentid))
                        break;
                source = ""
                currentid = None
            elif currentid:
                node += tline
                if insource:
                    source += tline
            elif "target-language" in tline:
                for n, item in enumerate(newfile):
                    if isinstance(item, str) and "source-language" in item:
                        newfile[n] = tline

    newname = orig
    if SAVE:
        newname += ".new"
    with open(newname, "w") as new:
        for line in newfile:
            if isinstance(line, tuple):
                new.write(line[1])
            else:
                new.write(line)


usage = sys.argv[0] + " <branding file> <merge to file>"
try:
  branding = sys.argv[1]
  mergeto = sys.argv[2]

  if not os.path.exists(branding):
      print(usage)
      sys.exit(1)
  if not os.path.exists(mergeto):
      print(usage)
      sys.exit(1)

  align(mergeto, branding)
except:
    print(usage)
    raise

