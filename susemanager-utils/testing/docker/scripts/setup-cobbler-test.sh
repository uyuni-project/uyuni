#!/bin/bash
# Start a real Cobbler inside the Java unit-test container
# configured so that the Java test code can log in without a running
# Uyuni server.
#
set -e

COBBLER_API="http://127.0.0.1/cobbler_api"
SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"
LOGS_DIR="/manager/java/test-results/"

port_open() {
    python3 - "$1" <<'PY'
import socket, sys
s = socket.socket()
s.settimeout(2)
try:
    s.connect(("127.0.0.1", int(sys.argv[1])))
    sys.exit(0)
except OSError:
    sys.exit(1)
PY
}

# FIXME: I did this wrong in https://build.opensuse.org/requests/1342326
if [ -f /etc/cobbler/settings.d/zz-uyuni.conf ]; then
  rm /etc/cobbler/settings.d/zz-uyuni.conf
fi
sed -i 's|module = authentication.configfile|module = authentication.spacewalk|' /etc/cobbler/modules.conf

cat > /etc/cobbler/settings.d/zz-uyuni.settings <<EOF
client_use_localhost: true
pxe_just_once: true
redhat_management_server: uyuni.example.org
server: uyuni.example.org
# Custom auth endpoint as the real uyuni server is not running
uyuni_authentication_endpoint: http://127.0.0.1:8000
redhat_management_permissive: true
# Speed up the test to not require cobbler sync workflow
manage_tftpd: false
EOF

# The Java tests write their profile templates into the test kickstart dir
# (host-mounted at /tmp/kickstart), while cobbler validates the profile's
# template path inside its own template directory - link the two trees.
for sub in wizard raw; do
    if [ -d "/var/lib/cobbler/templates/$sub" ] && [ ! -L "/var/lib/cobbler/templates/$sub" ]; then
        mv "/var/lib/cobbler/templates/$sub" "/var/lib/cobbler/templates/$sub.dist"
    fi
    ln -sfn "/tmp/kickstart/$sub" "/var/lib/cobbler/templates/$sub"
done

# Cobbler 3.3.3 defect: utils.copyfile's error path calls os.path.samefile
# with a not-yet-existing destination, which masks the original error (hit
# when distro files are copied across devices, e.g. from the host-mounted
# fixture directory). Guard it - the tests never read the tftpboot copies.
python3 - <<'PY'
path = "/usr/lib/python3.13/site-packages/cobbler/utils.py"
src = open(path).read()
if "cbltest-patch" not in src:
    old = ('            raise OSError("Cannot read: %s" % src)\n'
           '        if os.path.samefile(src, dst):')
    new = ('            raise OSError("Cannot read: %s" % src)\n'
           '        try:\n'
           '            same = os.path.samefile(src, dst)\n'
           '        except OSError:\n'
           '            same = False\n'
           '        if same:  # cbltest-patch: dst may not exist yet\n')
    if old in src:
        open(path, "w").write(src.replace(old, new, 1))
        print("cobbler utils.copyfile patched")
PY

# Background full-sync tasks (triggered by the commands under test) clean
# the rendered trees while the next test's distro save copies its files -
# tolerate the resulting FileNotFoundError on the synchronous per-distro
# copy (the tests never read the tftpboot/www copies).
python3 - <<'PY'
path = "/usr/lib/python3.13/site-packages/cobbler/cobbler_collections/collection.py"
src = open(path).read()
if "cbltest-patch" not in src:
    old = ("                elif isinstance(ref, distro.Distro):\n"
           "                    self.lite_sync.add_single_distro(ref.name, rebuild_menu=rebuild_menu)")
    new = ("                elif isinstance(ref, distro.Distro):\n"
           "                    try:  # cbltest-patch: background syncs race with concurrent saves/removals\n"
           "                        self.lite_sync.add_single_distro(ref.name, rebuild_menu=rebuild_menu)\n"
           "                    except (FileNotFoundError, OSError):\n"
           "                        pass")
    if old in src:
        open(path, "w").write(src.replace(old, new, 1))
        print("cobbler collection.add patched")
PY

# restart cobblerd to pick up the patches (started below when not running)
if port_open 25151; then
    kill "$(pgrep cobblerd)" 2>/dev/null || true
    sleep 2
fi

# The SSL vhost aborts apache startup (deployment-time cert missing).
if [ -f /etc/apache2/vhosts.d/vhost-ssl.conf ]; then
    mv /etc/apache2/vhosts.d/vhost-ssl.conf /etc/apache2/vhosts.d/vhost-ssl.conf.disabled
fi

# Expose /cobbler_api beyond localhost: the tests connect from the VM
#    host through the published port (same approach as the acceptance
#    suite's cobbler_test.rb).
cat > /etc/apache2/conf.d/zz-cobblerapi-test.conf <<'EOF'
# Written by setup-cobbler-test.sh (Uyuni Java unit tests).
<Location "/cobbler_api">
    Require ip 127.0.0.1 ::1 10.0.0.0/8 172.16.0.0/12 192.168.0.0/16
</Location>
EOF

echo "Starting authentication shim"
python3 "${SCRIPTS_DIR}/cobbler-auth-shim.py" > "${LOGS_DIR}/cobbler-test-shim.log" 2>&1 &

echo "Starting cobbler daemon"
if ! port_open 25151; then
    cobblerd -B --log-level INFO
fi

# Apache serves /cobbler_api (ProxyPass to cobblerd :25151; cobblerd
#    itself is path-sensitive and would 404 the /cobbler_api path).
if port_open 80; then
    # pick up the changed configuration
    /usr/sbin/start_apache2 -k restart >> "${LOGS_DIR}/cobbler-test-apache.log" 2>&1
else
    /usr/sbin/start_apache2 -k start > "${LOGS_DIR}/cobbler-test-apache.log" 2>&1
fi

