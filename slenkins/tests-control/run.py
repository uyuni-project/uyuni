#! /usr/bin/python

import subprocess
import sys
import traceback
import twopence
import susetest
import suselog
from susetest_api.assertions import *
from susetest_api.files import *

journal = None
suite = "/var/lib/slenkins/tests-suse-manager"
client = None
server = None
minion = None

def setup():
    global client, server, journal, minion

    config = susetest.Config("tests-suse-manager")
    journal = config.journal

    client = config.target("client")
    server = config.target("server")
    minion = config.target("minion")

def runOrRaise(node, command, msg, time_out = 60):
        ''' exec command on node, with msg (fail/succ message for journal)
            and use tiimeout = 60 as default, but as optional param. so can be modified.'''
        node.journal.beginTest("{}".format(msg))
        status = node.run(command, timeout = time_out)
        if not status and status.code != 0 :
               node.journal.failure("{} FAIL!".format(msg))
               raise susetest.SlenkinsError(1)
        node.journal.success("{} OK! ".format(msg))
        return True



######################
# MAIN 
#####################
setup()

SET_SUMAPWD =  "chpasswd <<< \"root:linux\""
SERVER_INIT= "/var/lib/slenkins/tests-suse-manager/tests-server/bin/suma_init.sh"
jail_cmd = "cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; export TESTHOST={}; export BROWSER=phantomjs; cd $WORKSPACE/cucumber;  rake".format(server.ipaddr_ext)
# jail_single_cmd = "cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; export TESTHOST={};export BROWSER=phantomjs; cd $WORKSPACE/cucumber; cucumber {}".format(server.ipaddr_ext, feat)
try:
    # bug workaround 
    change_hostname = "echo \"{}     suma-server\" > /etc/hosts; echo \"suma-server\" > /etc/hostname;  hostname -f".format(server.ipaddr)
	
    run_cmd(server, "hostname suma-server",  "change hostname ", 8000)
    run_cmd(server, "sed -i '$ d' /etc/hosts;", "change hosts file", 100)
    run_cmd(server, change_hostname, "verify change hostsfile",  200)

    journal.beginGroup("init suma-machines")
    runOrRaise(server, SERVER_INIT,  "INIT_SERVER", 8000)
    run_cmd(server, SET_SUMAPWD, "change root pwd to linux")
    run_cmd(client, SET_SUMAPWD, "change root pwd to linux")
    run_cmd(minion, SET_SUMAPWD, "change root pwd to linux")

    journal.beginGroup("running cucumber-suite on jail")
    subprocess.call(jail_cmd, shell=True)
    journal.success("finish group")

except susetest.SlenkinsError as e:
    journal.writeReport()
    sys.exit(e.code)

except:
    print "Unexpected error"
    journal.info(traceback.format_exc(None))
    raise

susetest.finish(journal)
