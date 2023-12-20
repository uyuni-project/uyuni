#!/usr/bin/python3  #  pylint: disable=missing-module-docstring,invalid-name
import paramiko
import sys
from optparse import OptionParser  #  pylint: disable=deprecated-module


def debug(msg):
    if options.verbose == "yes":
        print("DEBUG [{}]: {}".format(sys.argv[0], msg))  #  pylint: disable=consider-using-f-string


def errors(errs):  #  pylint: disable=redefined-outer-name
    if len(errs) == 0:
        return
    for e in errs:
        print("ERROR [{}]: {}".format(sys.argv[0], e))  #  pylint: disable=consider-using-f-string
    sys.exit(-1)


def outputs(out):
    for o in out:
        print("Output [{}]: {}".format(sys.argv[0], o))  #  pylint: disable=consider-using-f-string


parser = OptionParser(usage="usage: %prog [options] HOSTNAME")
parser.add_option("-u", "--username", dest="username", help="Username")
parser.add_option("-w", "--password", dest="password", help="Password")
parser.add_option("-c", "--command", dest="command", help="Command")
parser.add_option(
    "-i",
    "--insecure",
    dest="insecure",
    action="store_true",
    default=False,
    help="Insecure",
)
parser.add_option("-p", "--port", dest="port", help="Port", default=22)
parser.add_option(
    "-v",
    "--verbose",
    action="store_true",
    dest="verbose",
    default=False,
    help="verbose",
)

(options, args) = parser.parse_args()
if len(args) != 1:
    errors(["Usage: {} HOSTNAME".format(sys.argv[0])])  #  pylint: disable=consider-using-f-string

hostname = args[0]

errs = []
if not hostname:
    errs.append("No hostname set")
if not options.username:
    errs.append("No username set")
if not options.password:
    errs.append("No password set")
if not options.command:
    errs.append("No command set")

if len(errs) > 0:
    errors(errs)


ssh = paramiko.SSHClient()
if options.insecure:
    debug("setting auto add policy for unknown ssh keys")
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
debug("connecting to {}".format(hostname))  #  pylint: disable=consider-using-f-string
ssh.connect(hostname, options.port, options.username, options.password)
debug("running command {}".format(options.command))  #  pylint: disable=consider-using-f-string
stdin, stdout, stderr = ssh.exec_command(options.command)
errs = stderr.readlines()
if len(errs) != 0:
    print(
        "ERROR: There was an error connecting to {} and running {}".format(  #  pylint: disable=consider-using-f-string
            hostname, options.command
        )
    )
    errors(errs)
outputs(stdout.readlines())
