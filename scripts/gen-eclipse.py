#!/usr/bin/python
import os.path, os
from sys import argv, exit
base_template="""<?xml version="1.0" encoding="UTF-8"?>
<classpath>
  <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
  <classpathentry kind="output" path="build/classes"/>
%s
%s
</classpath>"""

classpath_srcentry = """  <classpathentry kind="src" path="%s"/>"""
classpath_libentry = """  <classpathentry kind="lib" path="%s"/>"""
classpath_libsource_entry = """  <classpathentry kind="lib" path="%s" sourcepath="%s"/>"""

def main():
    if len(argv) < 3 or len(argv) > 4:
        print ("Usage: python %s <src dirs separated by :> <jar dirs separated by :> [<src jars separated by :>]" % argv[0])
        print ("Example: python %s \"src/java:src/config\" \"/libdir1:libdir2\" \"libsrcdir1:libsrcdir2\"" % argv[0])
        exit(1)

    # source paths
    src_entries = []
    for dr in argv[1].replace(' ', '').split(":"):
        if dr.strip():
            src_entries.append(classpath_srcentry % dr)

    # lib sources paths
    libsrc_entries = {}
    if len(argv) == 4:
        for dr in argv[3].replace(' ', '').split(":"):
            if dr.strip():
                for f in os.listdir(dr):
                    if f != "rhn.jar" and f.endswith(".jar") and not f in libsrc_entries:
                        libsrc_entries[f] = os.path.join(dr,f)

    # lib paths - lib sources will be added when available
    entries = {}
    for dr in argv[2].replace(' ', '').split(":"):
        if dr.strip():
            if os.path.isdir(dr):
                for f in os.listdir(dr):
                    if f != "rhn.jar" and f.endswith(".jar") and not f in entries:
                        if f in  libsrc_entries:
                            entries[f] = classpath_libsource_entry % (os.path.join(dr,f) , libsrc_entries[f])
                        elif f[:-4] + "-" +"src.jar" in libsrc_entries:
                            entries[f] = classpath_libsource_entry % (os.path.join(dr,f) ,
                                                                libsrc_entries[f[:-4] + "-" +"src.jar"])
                        else:
                            entries[f] = classpath_libentry % os.path.join(dr,f)
            if os.path.isfile(dr):
                f = os.path.basename(dr)
                if f != "rhn.jar" and f.endswith(".jar") and not f in entries:
                    entries[f] = classpath_libentry % dr

    # put it all together
    print (base_template % ("\n".join(src_entries), "\n".join (entries.values())))

if __name__=="__main__":
    main()
