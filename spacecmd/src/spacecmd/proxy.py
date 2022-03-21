#
# Licensed under the GNU General Public License Version 3
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Copyright (c) 2022 SUSE LLC

import gettext
import logging
from spacecmd.utils import *

translation = gettext.translation('spacecmd', fallback=True)
try:
    _ = translation.ugettext
except AttributeError:
    _ = translation.gettext

def help_proxy_container_config(self):
    print(_('proxy_container_config: create a proxy system and return its configuration file'))
    print(_('''usage: proxy_container_config [options] PROXY_FQDN SERVER_FQDN MAX_CACHE EMAIL ROOT_CA CRT KEY

parameters:
  PROXY_FQDN  the fully qualified domain name of the proxy to create.
  SERVER_FQDN the fully qualified domain name of the server to connect to proxy to.
  MAX_CACHE   the maximum cache size in MB. 60% of the storage is a good value.
  EMAIL       the email of the proxy administrator
  CA          path to the root CA used to sign the proxy certificate in PEM format
  CRT         path to the proxy certificate in PEM format
  KEY         path to the proxy certificate private key in PEM format

options:
  -o, --output Path where to create the generated configuration. Default: 'config.zip'
  -i, --intermediate-ca  Path to an intermediate CA used to sign the proxy
            certicate in PEM format. May be provided multiple times.

examples:
  proxy_container_config -o config.zip proxy.lab server.lab 1024 proxy@acme.org root_ca.crt proxy.crt proxy.key
'''))


def do_proxy_container_config(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-r', '--root-ca', default='')
    arg_parser.add_argument('-i', '--intermediate-ca', action='append', default=[])
    arg_parser.add_argument('-c', '--certificate', default='')
    arg_parser.add_argument('-k', '--key', default='')
    arg_parser.add_argument('-o', '--output', default='config.zip')

    args, options = parse_command_arguments(args, arg_parser)

    if len(args) != 7:
        self.help_proxy_container_config()
        return

    (proxy_fqdn, server_fqdn, max_cache, email, root_ca, certificate, key) = args

    root_ca = read_file(root_ca)
    intermediate_cas = [read_file(path) for path in options.intermediate_ca]
    cert = read_file(certificate)
    key = read_file(key)

    config = self.client.proxy.container_config(self.session,
            proxy_fqdn, server_fqdn, int(max_cache), email,
            root_ca, intermediate_cas, cert, key,
    )

    with open(options.output, 'wb') as fd:
        fd.write(config.data)
    return options.output


def help_proxy_container_config_generate_cert(self):
    print(_('proxy_container_config_generate_cert: create a proxy system and return its configuration file'))
    print(_('''usage: proxy_container_config_generate_cert PROXY_FQDN SERVER_FQDN MAX_CACHE EMAIL CA_CRT CA_KEY CA_PASSWORD

parameters:
  PROXY_FQDN  the fully qualified domain name of the proxy to create.
  SERVER_FQDN the fully qualified domain name of the server to connect to proxy to.
  MAX_CACHE   the maximum cache size in MB. 60% of the storage is a good value.
  EMAIL       the email of the proxy administrator
  CA_CRT path to the certificate of the CA to use to generate a new proxy certificate
  CA_KEY path to the private key of the CA to use to generate a new proxy certificate
  CA_PASSWORD path to a file containing the password of the CA private key

options:
  -o, --output Path where to create the generated configuration. Default: 'config.zip'
  --ssl-cname alternate name of the proxy to set in the certificate. Can be provided multiple times
  --ssl-country country code to set in the certificate. If omitted, default values from mgr-ssl-tool will be used.
  --ssl-state state name to set in the certificate. If omitted, default values from mgr-ssl-tool will be used.
  --ssl-city the city name to set in the certificate. If omitted, default values from mgr-ssl-tool will be used.
  --ssl-org the organization name to set in the certificate. If omitted, default values from mgr-ssl-tool will be used.
  --ssl-org-unit the organization unit name to set in the certificate. If omitted, default values from mgr-ssl-tool will be used.
  --ssl-email the email to set in the certificate. If omitted, default values from mgr-ssl-tool will be used.

examples:
  proxy_container_config proxy.lab server.lab 1024 proxy@acme.org ssl-build/RHN-ORG-TRUSTED-SSL-CERT ssl-build/RHN-ORG-PRIVATE-SSL-KEY ca-password.txt
'''))


def do_proxy_container_config_generate_cert(self, args):
    arg_parser = get_argument_parser()
    arg_parser.add_argument('-o', '--output', default='config.zip')
    arg_parser.add_argument('--ssl-cname', action='append', default=[])
    arg_parser.add_argument('--ssl-country', default='')
    arg_parser.add_argument('--ssl-state', default='')
    arg_parser.add_argument('--ssl-city', default='')
    arg_parser.add_argument('--ssl-org', default='')
    arg_parser.add_argument('--ssl-org-unit', default='')
    arg_parser.add_argument('--ssl-email', default='')

    args, options = parse_command_arguments(args, arg_parser)

    if len(args) != 7:
        self.help_proxy_container_config_generate_cert()
        return

    (proxy_fqdn, server_fqdn, max_cache, email, ca_cert, ca_key, ca_password) = args

    ca_cert = read_file(ca_cert)
    ca_key = read_file(ca_key)
    ca_password = read_file(ca_password)

    config = self.client.proxy.container_config(self.session, proxy_fqdn, server_fqdn, int(max_cache), email,
            ca_cert, ca_key, ca_password, options.ssl_cname, options.ssl_country, options.ssl_state,
            options.ssl_city, options.ssl_org, options.ssl_org_unit, options.ssl_email)

    with open(options.output, 'wb') as fd:
        fd.write(config.data)
    return options.output


def read_file(path):
    '''
    utility function reading a file and returning its content.
    '''
    if not path:
        return None

    with open(path, 'r') as fd:
        return fd.read()
