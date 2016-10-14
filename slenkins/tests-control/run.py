#! /usr/bin/python

import subprocess
import sys
import traceback
import twopence
import susetest
import suselog
from susetest_api.assertions import *
from susetest_api.files import *
from susetest_api.machinery import *
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
        init_client = ''' zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0:/SLE12-SUSE-Manager-Tools/images/repo/SLE-12-Manager-Tools-POOL-x86_64-Media1/ suma3-devel-tools; 
                        zypper -n --gpg-auto-import-keys ref; 
                        zypper -n in subscription-tools;
                        zypper -n in spacewalk-client-setup;
                        zypper -n in spacewalk-check; 
                        zypper -n in spacewalk-oscap; 
			zypper -n in rhncfg-actions'''
        run_cmd(client, init_client, "init client", 600)
        # dummy packages for tests
	run_cmd(client, " zypper -n in andromeda-dummy milkyway-dummy virgo-dummy", "install dummy package needed by tests", 900)
        run_cmd(client, "echo \"{}     suma-server.example.com\" >> /etc/hosts;" .format(server.ipaddr), "setup host", 300)
	# openscap packages needed for tests
	#FIXME: why this packages are not in the tools repo? they are in the update repo .
	update_repo = "zypper ar http://download.suse.de/ibs/SUSE/Updates/SUSE-Manager-Server/3.0/x86_64/update/SUSE:Updates:SUSE-Manager-Server:3.0:x86_64.repo ;"
	run_cmd(client, update_repo + "zypper -n --gpg-auto-import-keys ref ;", "install openscap repo", 400)
	openscap = [ "openscap-content", "openscap-extra-probes", "openscap-utils"]
        [  run_cmd(client, "zypper -n in " + scap , "install" + scap) for scap in openscap ]

def setup_server():		
	change_hostname = "echo \"{}     suma-server.example.com\" >> /etc/hosts; echo \"suma-server.example.com\" > /etc/hostname;  hostname -f".format(server.ipaddr)
	run_cmd(server, "hostname suma-server.example.com",  "change hostname ", 8000)
	run_cmd(server, "sed -i '$ d' /etc/hosts;", "change hosts file", 100)
	run_cmd(server, change_hostname, "change hostsfile",  200)
	run_cmd(server, "mv  /var/lib/slenkins/tests-suse-manager/tests-server/install/ /", "move install", 900)
	

def setup_minion():
	# adding the repo devel for install salt-minion package.
	saltRepo = "zypper ar http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/3.0/images/repo/SUSE-Manager-Server-3.0-POOL-x86_64-Media1/ suma3_devel ; "
	saltInst = "zypper -n --gpg-auto-import-keys ref;  zypper -n in salt-minion;"
	run_cmd(minion, saltRepo + saltInst, "installing SALT on Minion SLES", 400)
	# change hostname to sle-minion
	change_hostname = "echo \"{}     sle-minion.example.com\" >> /etc/hosts; echo \"sle-minion.example.com\" > /etc/hostname;  hostname -f".format(minion.ipaddr)
        run_cmd(minion, "hostname sle-minion.example.com",  "change hostname ", 8000)
        run_cmd(minion, "sed -i '$ d' /etc/hosts;", "change hosts file", 100)
        run_cmd(minion, change_hostname, "change hostsfile",  200)
        # install dummy packages for salt test needed.
	run_cmd(minion, " zypper -n in andromeda-dummy milkyway-dummy virgo-dummy", "install dummy package needed by tests", 900)
	# this is for that server and minion can know they togheter.
        run_cmd(minion, "echo \"{}     suma-server.example.com\" >> /etc/hosts;" .format(server.ipaddr), "setup host", 300)
######################
# MAIN 
#####################
setup()

def run_all_feature():
	''' this function is on the control-node, and run all cucumber features defined on run_sets/testsuite.yml'''
	journal.beginTest("running cucumber whole suite")
	subprocess.call("cp -R /var/lib/slenkins/tests-suse-manager/tests-control/cucumber/ $WORKSPACE; cd $WORKSPACE/cucumber;", shell=True)
        subprocess.call("export MINION={2}; export CLIENT={0}; export TESTHOST={1}; export BROWSER=phantomjs; cd $WORKSPACE/cucumber ; rake ".format(client.ipaddr_ext, server.ipaddr_ext, minion.ipaddr_ext), shell=True)
	journal.success("finished to run cucumber")
	
def post_install_server():
	'''' clobberd configuration changes are necessary'''
	# modify clobberd
	journal.beginTest("Set up clobberd right configuraiton")
	replace_clobber = {'redhat_management_permissive: 0' : 'redhat_management_permissive: 1' }
	replace_string(server, replace_clobber, "/etc/cobbler/settings")
	journal.success("done clobberd conf !")
	run_cmd(server, "systemctl restart cobblerd.service && systemctl status cobblerd.service", "restarting cobllerd after configuration changes") 
	# modify rhn_reg config file, for timeout issue
       
	journal.beginTest("Set up rhn_configuration with more netw entries")
        replace_string(server, {'networkRetries=1' : 'networkRetries=10'}, "/etc/sysconfig/rhn/up2date")
	journal.success("done with rhn!")

	# files needed for tests 
	runOrRaise(server, "mv  /var/lib/slenkins/tests-suse-manager/tests-server/pub/* /srv/www/htdocs/pub/", "move to pub", 900)
	runOrRaise(server, "mv  /var/lib/slenkins/tests-suse-manager/tests-server/vCenter.json /tmp/", "move to pub", 900)

def check_cucumber():
    journal.beginTest("check tests cucumber for failures")
    output_cucumber = "$WORKSPACE/cucumber/output.html"
    check = "grep \"scenarios (0 failed\" {}".format(output_cucumber)
    if subprocess.call(check, shell=True) : 
		journal.failure("FAIL : some tests of cucumber failed ! ")
		return False
    journal.success("all tests of cucumber are ok")
    return True

###################################### MAIN ################################################################################
try:
    # change hostname, and move the install(fedora kernel, etc) dir to /
    setup_server()
    # change password to linux to all systems

    SET_SUMAPWD =  "chpasswd <<< \"root:linux\""
    [  run_cmd(node, SET_SUMAPWD, "change root pwd to linux") for node in (server, client, minion) ]
    # setup packages on client machine
    client_setup()

    # run migration.sh script
    journal.beginGroup("init suma-machines")
    SERVER_INIT= "/var/lib/slenkins/tests-suse-manager/tests-server/bin/suma_init.sh"
    runOrRaise(server, SERVER_INIT,  "INIT_SERVER", 8000)
    # modify clobber 
    post_install_server()

    # setup the minion (SLES minion)
    setup_minion()

    # run cucumber suite 
    journal.beginGroup("running cucumber-suite on jail")
    run_all_feature() 
    # check that all test are sucessufull (control node side)
    # otherwise fail in Jenkins .
    check_cucumber()

except susetest.SlenkinsError as e:
    journal.writeReport()
    sys.exit(e.code)

except:
    print "Unexpected error"
    journal.info(traceback.format_exc(None))
    raise

susetest.finish(journal)
