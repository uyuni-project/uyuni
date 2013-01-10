#! /bin/bash
#    Copyright (C) 2013  Novell, Inc.
#
#    This library is free software; you can redistribute it and/or
#    modify it under the terms of the GNU Lesser General Public
#    License as published by the Free Software Foundation;
#    version 2.1 of the License
#
#    This library is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with this library; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#

if [ 0$UID -gt 0 ]; then
       echo Run as root.
       exit 1
fi

print_help () {
        cat <<HELP
usage: configure-tftpsync.sh proxy1.domain.top proxy2.domain.top ...

HELP
exit 1
}

if [ $# -eq 0 ]; then
    print_help
fi

for proxy in $@; do
    if ! echo "$proxy" | grep -E "^[a-zA-Z].*\..+$" >/dev/null ; then
        echo "Invalid hostname for proxy: $proxy"
        print_help
    fi
done

cp /etc/cobbler/settings /etc/cobbler/settings.bak
# remove proxies section from conf
cat /etc/cobbler/settings.bak | awk '{if(/^proxies:/) x=1; else if (x == 1 && /^[[:space:]]*-/) x=1; else print }' > /etc/cobbler/settings

# create new proxies section
echo "proxies:" >> /etc/cobbler/settings
for proxy in $@; do
    echo " - \"$proxy\"" >> /etc/cobbler/settings
done

# remove cache file to push all files again
rm -f /var/lib/cobbler/pxe_cache.json
/etc/init.d/cobblerd restart
/etc/init.d/apache2 reload
echo "Please call 'cobbler sync' to push the enviroment to the new configured proxies"

