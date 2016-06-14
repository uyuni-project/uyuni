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

def client_setup():
        init_client = ''' zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3;  zypper -n --gpg-auto-import-keys ref; 
                        zypper -n in subscription-tools;
                        zypper -n in spacewalk-client-setup;
                        zypper -n in spacewalk-check; '''
        run_cmd(client, init_client, "init client", 600)



######################
# MAIN 
#####################
setup()
SET_SUMAPWD =  "chpasswd <<< \"root:linux\""
SERVER_INIT= "/var/lib/slenkins/tests-suse-manager/tests-server/bin/suma_init.sh"

init_jail_cmd = "cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; export TESTHOST={}; export BROWSER=phantomjs; cd $WORKSPACE/cucumber;".format(server.ipaddr_ext)

run_whole = "rake"
features = ( 'features/init_user_create.feature', 'features/setup_proxy.feature', 'features/running.feature', 'features/login.feature', 'features/mainpage.feature' ,
              'features/channels_add.feature', 'features/push_package.feature', 'features/create_repository.feature', 'features/systemspage.feature',
 	      'features/create_activationkey.feature', 'features/users.feature', 'features/users-createnewuser.feature', 'features/users-userdetails.feature',
	      'features/credentials.feature', 'features/csv-separator.feature', 'features/create_config_channel.feature' , 'features/register_client.feature')

basic_feature = ( 'features/init_user_create.feature', 'features/setup_proxy.feature', 'features/running.feature', 'features/login.feature', 'features/mainpage.feature' ,
              'features/channels_add.feature', 'features/push_package.feature', 'features/create_repository.feature', 'features/systemspage.feature',
              'features/create_activationkey.feature')

try:

    change_hostname = "echo \"{}     suma-server.example.com\" >> /etc/hosts; echo \"suma-server.example.com\" > /etc/hostname;  hostname -f".format(server.ipaddr)
    run_cmd(server, "hostname suma-server.example.com",  "change hostname ", 8000)
    run_cmd(server, "sed -i '$ d' /etc/hosts;", "change hosts file", 100)
    run_cmd(server, change_hostname, "change hostsfile",  200)
    run_cmd(server, "cat /etc/hosts; cat /etc/hostname", "verification hosts", 300)
    client_setup()
    journal.beginGroup("init suma-machines")
    runOrRaise(server, SERVER_INIT,  "INIT_SERVER", 8000)
    run_cmd(server, SET_SUMAPWD, "change root pwd to linux")
    run_cmd(client, SET_SUMAPWD, "change root pwd to linux")
    run_cmd(minion, SET_SUMAPWD, "change root pwd to linux")
    
    journal.beginGroup("running cucumber-suite on jail")
    subprocess.call(init_jail_cmd, shell=True)
    for feature in basic_feature:
		journal.beginTest("running feature : {}".format(feature)) 
	
		retcode = subprocess.call("export CLIENT={0}; export TESTHOST={1}; export BROWSER=phantomjs; cucumber $WORKSPACE/cucumber/{2}".format(client.ipaddr_ext, server.ipaddr_ext, feature), shell=True)
		if retcode == 0:
			journal.success("feature: {} was  successefull".format(feature))
		else :
			journal.failure("feature: {} fail!".format(feature))
except susetest.SlenkinsError as e:
    journal.writeReport()
    sys.exit(e.code)

except:
    print "Unexpected error"
    journal.info(traceback.format_exc(None))
    raise

susetest.finish(journal)
