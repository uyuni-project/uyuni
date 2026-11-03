# pylint: skip-file
# Uyuni unit-test auth shim (test-only, accept-all).
#
# Emulates the Uyuni side of Cobbler's "authentication.spacewalk" module:
# the module validates credentials by calling back into the Uyuni XML-RPC
# API (uyuni_authentication_endpoint) using exactly these three dotted
# methods. Running a full Uyuni server inside the test container is not an
# option, so this shim accepts every login/token, letting the production
# auth flow of the Java code (IntegrationService/CobblerLoginCommand) work
# unchanged against the real Cobbler under test.
#
# Bound to 127.0.0.1 inside the test container only; never deployed anywhere.
from xmlrpc.server import SimpleXMLRPCServer, SimpleXMLRPCRequestHandler


class Handler(SimpleXMLRPCRequestHandler):
    # The Uyuni API path the spacewalk auth module posts to.
    rpc_paths = ("/rpc/api",)


def checkAuthToken(user, token):
    # Uyuni's AuthHandler.checkAuthToken returns 1 for a valid token.
    return 1


def login(user, password):
    return "shim-session-token-0001"


def listRoles(session, user):
    # spacewalk.py accepts logins only for config_admin/org_admin users.
    return ["org_admin"]


def main():
    server = SimpleXMLRPCServer(
        ("127.0.0.1", 8000), requestHandler=Handler, logRequests=False, allow_none=True
    )
    server.register_function(checkAuthToken, "auth.checkAuthToken")
    server.register_function(login, "auth.login")
    server.register_function(listRoles, "user.listRoles")
    server.serve_forever()


if __name__ == "__main__":
    main()
