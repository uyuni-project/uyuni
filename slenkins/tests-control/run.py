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

def client_setup():
        init_client = ''' zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3;  zypper -n --gpg-auto-import-keys ref; 
                        zypper -n in subscription-tools;
                        zypper -n in spacewalk-client-setup;
                        zypper -n in spacewalk-check; 
                        zypper -n in spacewalk-oscap; 
			zypper -n in rhncfg-actions'''
        run_cmd(client, init_client, "init client", 600)
	run_cmd(client, " zypper -n in andromeda-dummy milkyway-dummy virgo-dummy", "install dummy package needed by tests", 900)
	

def setup_server():		
	change_hostname = "echo \"{}     suma-server.example.com\" >> /etc/hosts; echo \"suma-server.example.com\" > /etc/hostname;  hostname -f".format(server.ipaddr)
	run_cmd(server, "hostname suma-server.example.com",  "change hostname ", 8000)
	run_cmd(server, "sed -i '$ d' /etc/hosts;", "change hosts file", 100)
	run_cmd(server, change_hostname, "change hostsfile",  200)
	run_cmd(server, "mv  /var/lib/slenkins/tests-suse-manager/tests-server/install/ /", "move install", 900)

######################
# MAIN 
#####################
setup()
SET_SUMAPWD =  "chpasswd <<< \"root:linux\""
SERVER_INIT= "/var/lib/slenkins/tests-suse-manager/tests-server/bin/suma_init.sh"

run_cucumber_on_jail = "cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; export CLIENT={}; export TESTHOST={}; export BROWSER=phantomjs; cd $WORKSPACE/cucumber; rake".format(client.ipaddr_ext, server.ipaddr_ext)

def run_all_feature():
	journal.beginTest("running cucumber whole suite")
	subprocess.call("cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; cd $WORKSPACE/cucumber;", shell=True)
        subprocess.call("export CLIENT={0}; export TESTHOST={1}; export BROWSER=phantomjs; cd $WORKSPACE/cucumber ; rake ".format(client.ipaddr_ext, server.ipaddr_ext), shell=True)
	journal.success("finished to run cucumber")
	

def run_single_features():

	subprocess.call("cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; cd $WORKSPACE/cucumber;", shell=True)

	
	features = ( 'features/init_user_create.feature', 'features/setup_proxy.feature', 'features/running.feature', 'features/login.feature', 'features/mainpage.feature' ,
              'features/channels_add.feature', 'features/push_package.feature', 'features/create_repository.feature', 'features/systemspage.feature',
 	      'features/create_activationkey.feature', 'features/users.feature', 'features/users-createnewuser.feature', 'features/users-userdetails.feature',
	      'features/credentials.feature', 'features/csv-separator.feature', 'features/create_config_channel.feature' , 'features/register_client.feature',
	      'features/test_config_channel.feature', 'features/action_chain.feature', 'features/xmlrpc_actionchain.feature', 'features/system_configuration.feature',
	      'features/custom_system_info.feature', 'features/create_group.feature', 'features/add_sys_of_group_to_SSM.feature', 'features/configuration.feature',
	      'features/add_system_to_conf.feature', 'features/check_errata-npn.feature' )

	basic_feature = ( 'features/init_user_create.feature', 'features/setup_proxy.feature', 'features/running.feature', 'features/login.feature', 'features/mainpage.feature' ,
              'features/channels_add.feature', 'features/push_package.feature', 'features/create_repository.feature', 'features/systemspage.feature',
              'features/create_activationkey.feature')
	
	for feature in features:
		journal.beginTest("running feature : {}".format(feature)) 
		retcode = subprocess.call("export CLIENT={0}; export TESTHOST={1}; export BROWSER=phantomjs; cucumber $WORKSPACE/cucumber/{2}".format(client.ipaddr_ext, server.ipaddr_ext, feature), shell=True)
		if retcode == 0:
			journal.success("feature: {} was  successefull".format(feature))
		else :
			journal.failure("feature: {} fail!".format(feature))

def post_install_server():
	# modify clobberd
	journal.beginTest("Set up clobberd right configuraiton")
	replace_clobber = {'redhat_management_permissive: 0' : 'redhat_management_permissive: 1' }
	replace_string(server, replace_clobber, "/etc/cobbler/settings")
	journal.success("done clobberd conf !")
	run_cmd(server, "systemctl restart cobblerd.service && systemctl status cobblerd.service", "restarting cobllerd after configuration changes") 
	# files needed for tests
	runOrRaise(server, "mv  /var/lib/slenkins/tests-suse-manager/tests-server/pub/* /srv/www/htdocs/pub/", "move to pub", 900)
	runOrRaise(server, "mv  /var/lib/slenkins/tests-suse-manager/tests-server/vCenter.json /tmp/", "move to pub", 900)
###################################### MAIN ################################################################################
try:
    # change hostname, and move the install(fedora kernel, etc) dir to /
    setup_server()
    # change password to linux to all systems
    for node in (server, client, minion): 
    	run_cmd(node, SET_SUMAPWD, "change root pwd to linux")
    # install some spacewalk packages on client
    client_setup()
    # run migration.sh script
    journal.beginGroup("init suma-machines")
    runOrRaise(server, SERVER_INIT,  "INIT_SERVER", 8000)
    post_install_server()

    
    journal.beginGroup("running cucumber-suite on jail")
    run_all_feature() 

except susetest.SlenkinsError as e:
    journal.writeReport()
    sys.exit(e.code)

except:
    print "Unexpected error"
    journal.info(traceback.format_exc(None))
    raise

susetest.finish(journal)
