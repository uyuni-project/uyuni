#!/usr/bin/env python
import sys, os
from optparse import OptionParser

#Location of satellite docs directory
SAT_DOCS_DIR = "../"
NUTCH_CONF_TEMPLATE = "./NUTCH_CONF_TEMPLATE"
OUTPUT_DIR = "./conf"

QUICK_START = "quick"
REFERENCE_GUIDE = "reference"
INSTALL_GUIDE = "install"
CLIENT_CONFIG_GUIDE = "client-config"
#PROXY_QUICK_START = "proxy-quick"

LANGUAGES = [ "en-US" ]

GUIDES = {
    QUICK_START: LANGUAGES,
    REFERENCE_GUIDE: LANGUAGES,
    INSTALL_GUIDE: LANGUAGES,
    CLIENT_CONFIG_GUIDE: LANGUAGES
#    PROXY_QUICK_START: LANGUAGES
}

RELEASE_NOTES = {
#    RELEASE_NOTES_SATELLITE: ["en-US"]
}

def verifyAll():
    verifyPaths(GUIDES)
    verifyPaths(RELEASE_NOTES)

def verifyPaths(dictionary):
    for entry in dictionary.keys():
        sat_docs_dir = getSatDocsDir()
        top = os.path.join(sat_docs_dir, entry)
        if not os.path.isdir(top):
            print "Missing directory: ", top
            continue
        for lang in dictionary[entry]:
            lang_dir = os.path.join(top, lang)
            if not os.path.isdir(lang_dir):
                print "Missing directory: ", lang_dir

def getLangAbbrv(lang):
    return lang

def adjustNutchConf(lang, entry=None, finalize=False):
    nutch_dir = getNutchConfDir(lang)
    #Append to crawl-urlfilter.txt
    urlfilter = os.path.join(nutch_dir, "crawl-urlfilter.txt")
    if not os.path.isfile(urlfilter):
        print "Error, %s is not available" % (urlfilter)
        sys.exit(1)
    f = open(urlfilter, "a+")
    if not finalize:
        data = "+.*/%s/\n" % (entry)
    else:
        data = "#skip everything else\n-.*\n"
    f.write(data)
    f.close()

def getSatDocsDir():
    pwd = os.getcwd()
    sat_docs_dir = os.path.join(pwd, SAT_DOCS_DIR)
    if not os.path.isdir(sat_docs_dir):
        print "Error, not able to verify Satellite Documents exist."
        print "Looing at: %s" % (sat_docs_dir)
        print "cd to where this script is and try to re-run, or adjust SAT_DOCS_DIR=%s" % (SAT_DOCS_DIR)
        sys.exit(1)
    return sat_docs_dir

def getLangDir(lang):
    lang = getLangAbbrv(lang)
    lang_dir = os.path.join(OUTPUT_DIR, lang)
    if not os.path.exists(lang_dir):
        os.mkdir(lang_dir)
    return lang_dir

def getLangUrlsFile(lang):
    lang_dir = getLangDir(lang)
    urls_dir = os.path.join(lang_dir, "urls")
    if not os.path.exists(urls_dir):
        os.mkdir(urls_dir)
    urls_file = os.path.join(urls_dir, "urls.txt")
    return urls_file

def getNutchConfDir(lang):
    lang_dir = getLangDir(lang)
    lang_dir_nutch = os.path.join(lang_dir, "nutch_conf")
    return lang_dir_nutch

def adjustUrlFile(lang, entry):
    urls_file = getLangUrlsFile(lang)
    f = open(urls_file, "a+")
    sat_docs_dir = getSatDocsDir()
    data = "file://%s/%s/\n" % (sat_docs_dir, entry)
    f.write(data)
    f.close()

def finalizeNutchConf():
    """
    this will configure nutch to ignore all other documents that weren't
    expliticly added to crawl-urlfilter.txt
    """
    for lang in LANGUAGES:
        adjustNutchConf(lang, finalize=True)

def process(docDict):
    #Add entries for each document, get the document path with language
    #add it to urls file and edit nutch conf
    for entry in docDict.keys():
        for lang in docDict[entry]:
            print "Process %s in %s" % (entry, lang)
            lang_dir = os.path.join(entry, lang)
            #Verify the target path we'll write to configuration files
            sat_docs_dir = getSatDocsDir()
            verify_dir = os.path.join(sat_docs_dir, lang_dir)
            if not os.path.isdir(verify_dir):
                print "%s is not a valid directory" % (verify_dir)
                #return False
            adjustUrlFile(lang, lang_dir)
            adjustNutchConf(lang, lang_dir)
    return True

def init():
    """
    init() -    responsibilites include 
    1) creating the output directory where all our configuration files will reside.
    2) creating per language directories
    3) remove existing nutch conf per lang if it exists 
    4) copying the nutch template config files to each language
    5) erase existing urls.txt files per language
    """
    if not os.path.exists(OUTPUT_DIR):
        os.mkdir(OUTPUT_DIR)
    for lang in LANGUAGES:
        lang_dir_nutch = getNutchConfDir(lang) 
        if os.path.exists(lang_dir_nutch):
            if lang_dir_nutch != "/":
                cmd = "rm -fr %s" % (lang_dir_nutch)
                os.system(cmd)
                print "Removed Directory: %s" % (lang_dir_nutch)
        cmd = "cp -R %s %s" % (NUTCH_CONF_TEMPLATE,  lang_dir_nutch)
        os.system(cmd)
        urls_file = getLangUrlsFile(lang)
        if os.path.exists(urls_file):
            os.remove(urls_file)
            print "Removed File: %s" % (urls_file)

if __name__ == "__main__":
    usage = "usage: %prog [options]"
    parser = OptionParser(usage)
    parser.add_option("--docs-dir", dest="docs_dir",
            help="Specifies the Satellite docs directory")

    (options, args) = parser.parse_args()

    if options.docs_dir:
        SAT_DOCS_DIR = options.docs_dir

    verifyAll()
    init()
    if not process(GUIDES):
        print "Error, stopping"
        sys.exit(1)
    if not process(RELEASE_NOTES):
        print "Error, stopping"
        sys.exit(1)
    finalizeNutchConf()
    print "Successful, you are now ready to run nutch for each language."
