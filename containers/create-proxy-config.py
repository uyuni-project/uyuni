#!/usr/bin/python3

import argparse
import glob
import os
import shutil
import socket
import xmlrpc.client as xmlrpclib

def parse_arguments():
  description = 'Creates a configuration directory for a Proxy container'
  parser = argparse.ArgumentParser(description=description)
  parser.add_argument('fqdn', metavar='FQDN', type=str, help='FQDN of the Proxy')
  parser.add_argument('username', metavar='USERNAME', type=str, help='Name of user which will create the Proxy')
  parser.add_argument('password', metavar='PASSWORD', type=str, help='Password of which will create the Proxy')
  parser.add_argument('cnames', metavar='cnames', type=str, nargs='*', help='Additional CNAMES of the Proxy')
  parser.add_argument("-c", "--country", dest="country", help="Country for auto-generation of SSL certificate", default="US")
  parser.add_argument("-s", "--state", dest="state", help="State for auto-generation of SSL certificate", default="STATE")
  parser.add_argument("-i", "--city", dest="city", help="City for auto-generation of SSL certificate", default="CITY")
  parser.add_argument("-o", "--organization", dest="organization", help="Organization for auto-generation of SSL certificate", default="ORGANIZATION")
  parser.add_argument("-u", "--organization-unit", dest="organization_unit", help="Organization unit for auto-generation of SSL certificate", default="ORGANIZATION UNIT")
  parser.add_argument("-e", "--email", dest="email", help="E-mail for auto-generation of SSL certificate", default="postmaster@localhost")
  results = parser.parse_args()
  return results

args = parse_arguments()
fqdn = args.fqdn
country = args.country
state = args.state
city = args.city
organization = args.organization
organization_unit = args.organization_unit
email = args.email
cnames = args.cnames
subdir = ".".join(fqdn.split('.')[:-2])

# Create the config path directory in the current directory
config_path = "./proxy-config"
if os.path.exists(config_path):
    shutil.rmtree(config_path)
os.mkdir(config_path)

# Execute mgr-ssl-tool and copy the generated files to proxy-config
ssl_build_path = "/root/ssl-build"
os.system(
    """
    mgr-ssl-tool \
        --gen-server \
        --dir="/root/ssl-build" \
        --set-country="{}" \
        --set-state="{}" \
        --set-city="{}" \
        --set-org="{}" \
        --set-org-unit="{}" \
        --set-email="{}" \
        --set-hostname="{}" \
        {}
    """.format(country, state, city, organization, organization_unit, email, fqdn,
        " ".join(['--set-cname="{}"'.format(cn) for cn in cnames]))
)

# Copy the CA file and the generated RPM
ca_file = ssl_build_path + "/RHN-ORG-TRUSTED-SSL-CERT"
shutil.copy(ca_file, config_path)
srvcert_pkg = glob.glob('/root/ssl-build/{}/*.noarch.rpm'.format(subdir))[-1]
shutil.copy(srvcert_pkg, config_path)

# Generate minion keys and pre-accept
os.system("salt-key --gen-keys={} --gen-keys-dir=/tmp".format(fqdn))
shutil.copy("/tmp/{}.pub".format(fqdn), "/etc/salt/pki/master/minions/{}".format(fqdn))

# Copy minion keys to proxy-config
os.mkdir(config_path + "/salt")
os.mkdir(config_path + "/salt/pki")
os.mkdir(config_path + "/salt/pki/minion")
minion_config_path = config_path + "/salt/pki/minion/"
shutil.copy("/tmp/{}.pub".format(fqdn), minion_config_path + "minion.pub")
shutil.copy("/tmp/{}.pem".format(fqdn), minion_config_path + "minion.pem")

# Copy the master pub key
shutil.copy("/etc/salt/pki/master/master.pub", minion_config_path + "/minion_master.pub")

client = xmlrpclib.Server("http://localhost/rpc/api", verbose=0)
key = client.auth.login(args.username, args.password)

# Create the system profile
id = client.system.createSystemProfile(key, args.fqdn, {"hostname": args.fqdn})
sid = client.system.downloadSystemId(key, id)
client.proxy.activateProxy(sid, "5.2")

# Save the systemid file
os.mkdir(config_path + "/sysconfig")
os.mkdir(config_path + "/sysconfig/rhn")
with open(config_path + "/sysconfig/rhn/systemid", "w") as file:
    file.write(sid)

# Print some output and environment variables
print("\n=======================================================\n\n")
print("\nThe proxy config files were created, environment variables:")
print("UYUNI_MASTER='{}'".format(socket.getfqdn()))
print("UYUNI_MINION_ID='{}'".format(fqdn))
print("UYUNI_CA_CERTS='/config/RHN-ORG-TRUSTED-SSL-CERT'")
print("UYUNI_SRV_CERT='/config/{}'".format(os.path.basename(srvcert_pkg)))
print("UYUNI_EMAIL='{}'".format(email))

