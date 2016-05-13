#! /usr/bin/python

import subprocess
import sys
import traceback
import twopence
import susetest
import suselog


journal = None
suite = "/var/lib/slenkins/tests-tomcat"
client = None
server = None
minion = None

def setup():
    global client, server, journal, minion

    config = susetest.Config("tests-tomcat")
    journal = config.journal

    client = config.target("client")
    server = config.target("server")
    minion = config.target("minion")

## HELPERs FUNCTION
def run_command(node, command, msg):
        journal.beginTest("Running commands for {}".format(msg))
        if not node.run(command, timeout = 8000):
                journal.failure("{} FAIL!".format(msg))
                return False
        journal.success("{} OK! ".format(msg))
        return True


######################
# MAIN 
#####################

setup()

# basic commands for running cucumber.
SET_SUMA_ENVS = "TESTHOST=#{SERVER_IP}; BROWSER=phantomjs; export TESTHOST; export phantomjs"
SET_SUMAPWD =  "chpasswd <<< \"root:linux\""
SERVER_INIT= "/var/lib/slenkins/tests-suse-manager/tests-server/bin/suma_init.sh"

# We run the cucumber suite from jail. Sett the environment and run it with rake.
jail_cmd = "cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; export TESTHOST={}; export BROWSER=phantomjs; cd $WORKSPACE/cucumber;  rake".format(server.ipaddr)


try:
    journal.beginGroup("init suma-machines")
    run_command(server, SERVER_INIT,  "INIT_SERVER")
    run_command(server, SET_SUMAPWD, "change root pwd to linux")
    run_command(client, SET_SUMAPWD, "change root pwd to linux")
    run_command(minion, SET_SUMAPWD, "change root pwd to linux")

    journal.beginGroup("running cucumber-suite on jail")
    subprocess.call(jail_cmd, shell=True)
    journal.success("finish group")
except:
    print "Unexpected error"
    journal.info(traceback.format_exc(None))
    raise

susetest.finish(journal)
