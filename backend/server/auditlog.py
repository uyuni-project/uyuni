#!/usr/bin/python -u
# -*- coding: utf-8 -*-
#
# Copyright (c) 2010 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import inspect
import os
import pwd
from socket import error
try:
    from xmlrpc.client import ServerProxy, Error
except:
    from xmlrpclib import ServerProxy, Error

from spacewalk.common.rhnConfig import initCFG, CFG
from spacewalk.common.rhnLog import log_error, log_debug
from spacewalk.server import rhnServer, rhnSQL


class AuditLogException(Exception):
    def __init__(self, msg):
        self.msg = msg
        log_error(msg)

    def __str__(self):
        return self.msg

def auditlog_xmlrpc(method, method_name, args, request):
    """Log the calling of an XMLRPC API method

    Note: we only log methods that include "system_id" in their
    parameter list as these methods are deemed "important".

    :method: a function object from the API that was called
    :method_name: the full name of the function (includes the class)
    :args: a tuple of arguments that the function has been called with
    :request: the request object of the API call

    """
    enabled, server_url = _read_config()

    # if logging is disabled, we don't do anything at all
    if not enabled:
        return

    # If the method around
    if not method:
        raise AuditLogException("An attempt to call unknown method at server %s." % server_url)


    try:
        server = ServerProxy(server_url)
    except IOError as e:
        raise AuditLogException("Could not establish a connection to the "
                                "AuditLog server. IOError: %s. "
                                "Is this server url correct? %s"
                                % (e, server_url))

    # we only want to log the actions that require authentication 
    # (so far this means functions which require a system_id)
    if "system_id" in inspect.getargspec(method).args:
        uid = _get_uid()

        server_id, args = _get_server_id(method, args)

        message = "%s%s" % (method_name, args)

        headers = request.headers_in

        hostname = headers["SERVER_NAME"]

        extmap = {"EVT.SRC": "BACKEND_API",
                  "REQ.REMOTE_ADDR": headers.get("REMOTE_ADDR", ""),
                  "REQ.SERVER_PORT": headers.get("SERVER_PORT", ""),
                  "REQ.DOCUMENT_ROOT": headers.get("DOCUMENT_ROOT", ""),
                  "REQ.SCRIPT_FILENAME": headers.get("SCRIPT_FILENAME", ""),
                  "REQ.SCRIPT_URI": headers.get("SCRIPT_URI", "")}

        if "HTTP_X_RHN_PROXY_AUTH" in headers:
            extmap.update({
                    "REQ.PROXY": headers["HTTP_X_RHN_PROXY_AUTH"],
                    "REQ.PROXY_VERSION": headers.get("HTTP_X_RHN_PROXY_VERSION", ""),
                    "REQ.ORIGINAL_ADDR": headers.get("HTTP_X_RHN_IP_PATH", "")})
        try:
            server.audit.log(uid, message, hostname, extmap)
        except (Error, error) as e:
            raise AuditLogException("Got an error while talking to the "
                                    "AuditLogging server at %s. Error was: %s"
                                    % (server_url, e))
        log_debug(2, "Logged method call %s to the AuditLog server" % message)

def _get_uid():
    """The uid that we send is the current process's uid and name"""
    euid = os.geteuid()
    username = pwd.getpwuid(euid).pw_name
    uid = "%s(%s)" % (euid, username)
    return uid

def _read_config():
    # we want to change the logging file to 'audit' and set it back
    # after we finished reading the config file
    # TODO Changing the component twice on every request is not nice
    comp = CFG.getComponent()
    initCFG("audit")

    enabled = CFG.get("enabled")
    server_url = CFG.get("server", "")

    # XXX haven't tested what happens if it's not set back to the original value
    initCFG(comp)

    return (enabled, server_url)

def _get_server_id(method, args):
    """Get the server_id string from the function and remove it from 'args'

    We only want the string (e.g. "10001000") not the whole XML.

    :method: a function object from the API that was called
    :args: a tuple of arguments that the function has been called with

    Returns:
    :system_id: a string of the system_id e.g. "10001000"
    :args: a tuple of the arguments that were received, with system_id removed

    """
    params = inspect.getargspec(method).args

    # 'self' is the first parameter of the function, but we don't have
    # it in our 'args' list
    params.remove("self")

    index = params.index("system_id")
    args = list(args)
    sysid_xml = args[index]
    system_id = rhnServer.get(sysid_xml).getid()

    # we replace the system_id XML with the corresponding string in the
    # argument list as well
    args[index] = system_id
    return (system_id, tuple(args))
