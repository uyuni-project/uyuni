#!/bin/bash

if [ 0$UID -gt 0 ]; then
    echo Run as root.
    exit 1
fi

if [ ! -e /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT -a -e /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT ]; then
    ln -s /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT
fi

print_help() {
    cat <<HELP
usage: configure-proxy.sh [options]

options:
  --activate-SLP
            activate the SLP server so SUSE Manager proxy gets advertised
  --answer-file=filename
            Indicates the location of an answer file to be use for answering
            questions asked during the installation process. See man page for
            for an example and documentation.
  --force-own-ca
            Do not use parent CA and force to create your own.
  -h, --help
            show this help message and exit
  --http-password=HTTP_PASSWORD
            The password to use for an authenticated proxy.
  --http-proxy=HTTP_PROXY
            HTTP proxy in host:port format, e.g. squid.redhat.com:3128
  --http-username=HTTP_USERNAME
            The username for an authenticated proxy.
  --non-interactive
            For use only with --answer-file. If the --answer-file doesn't
            provide a required response, default answer is used.
  --populate-config-channel
            Create config chanel and save configuration files to that channel.
            Configuration channel name is rhn_proxy_config_\${SYSTEM_ID}.
  --rhn-password=RHN_PASSWORD
            Red Hat Network or Spacewalk password.
  --rhn-user=RHN_USER
            Red Hat Network or Spacewalk user account.
  --ssl-build-dir=SSL_BUILD_DIR
            The directory where we build SSL certificate. Default is /root/ssl-build
  --ssl-city=SSL_CITY
            City to be used in SSL certificate.
  --ssl-common=SSL_COMMON
            Common name to be used in SSL certificate.
  --ssl-country=SSL_COUNTRY
            Two letters country code to be used in SSL certificate.
  --ssl-email=SSL_EMAIL
            Email to be used in SSL certificate.
  --ssl-org=SSL_ORG
            Organization name to be used in SSL certificate.
  --ssl-orgunit=SSL_ORGUNIT
            Organization unit name to be used in SSL certificate.
  --ssl-password=SSL_PASSWORD
            Password to be used for SSL CA certificate.
  --ssl-state=SSL_STATE
            State to be used in SSL certificate.
  --ssl-cname=CNAME_ALIAS
            Cname alias of the machine. Can be specified multiple times.
  --start-services[=N]
            1 or Y to start all services after configuration. This is default.
            0 or N to not start services after configuration.
  --traceback-email=TRACEBACK_EMAIL
            Email to which tracebacks should be sent.
  --ssl-use-existing-certs
            Use custom SSL certificates instead of generating new ones (use
            --ssl-ca-cert, --ssl-server-key and --ssl-server-cert parameters to
            specify paths).
  --ssl-ca-cert
            Use a custom CA certificate from the given file.
  --ssl-server-key
            Use a server private SSL key from the given file.
  --ssl-server-cert
            Use a server public SSL certificate from the given file.
  --version=VERSION
            Version of Spacewalk Proxy Server you want to activate.
HELP
    exit 1
}

open_firewall_ports() {
echo "Open needed firewall ports..."
if [ -x /usr/bin/firewall-cmd ]; then
  firewall-cmd --state 2> /dev/null
  if [ $? -eq 0 ]; then
    firewall-cmd --permanent --zone=public --add-service=suse-manager-proxy
    firewall-cmd --reload
  else
    firewall-offline-cmd --zone=public --add-service=suse-manager-proxy
  fi
else
  echo "firewalld not installed" >&2
fi
}

parse_answer_file() {
    local FILE="$1"
    local ALIAS
    if [ ! -r "$FILE" ] ; then
       echo "Answer file '$FILE' is not readable."
       exit 1
    fi
    . "$FILE"
    for ALIAS in ${SSL_CNAME[@]}; do
        SSL_CNAME_PARSED[CNAME_INDEX++]=--set-cname=$ALIAS
    done
}

set_value() {
    local OPTION="$1"
    local VAR="$2"
    local ARG="$3"
    [[ "$ARG" =~ ^- ]] \
        && echo "$0: option $OPTION requires argument! Use answer file if your argument starts with '-'." \
        && print_help
    eval "$(printf "%q=%q" "$VAR" "$ARG")"
}

yes_no() {
    case "$1" in
        Y|y|Y/n|n/Y|1)
            echo 1
            ;;
        *)
            echo 0
            ;;
    esac
}

INTERACTIVE=1
INTERACTIVE_RETRIES=3
CNAME_INDEX=0
MANUAL_ANSWERS=0

OPTS=$(getopt --longoptions=help,activate-SLP,answer-file:,non-interactive,version:,traceback-email:,force-own-ca,http-proxy:,http-username:,http-password:,rhn-user:,rhn-password:,ssl-build-dir:,ssl-org:,ssl-orgunit:,ssl-common:,ssl-city:,ssl-state:,ssl-country:,ssl-email:,ssl-password:,ssl-cname:,ssl-use-existing-certs::,ssl-ca-cert:,ssl-server-key:,ssl-server-cert:,rhn-user:,rhn-password:,populate-config-channel::,start-services:: -n ${0##*/} -- h "$@")

if [ $? != 0 ] ; then
    print_help
fi

# It is getopt's responsibility to make this safe
eval set -- "$OPTS"

while : ; do
    case "$1" in
        --help|-h)  print_help;;
        --activate-SLP) ACTIVATE_SLP=1;;
        --answer-file) set_value "$1" ANSWER_FILE "$2";
                       parse_answer_file "$ANSWER_FILE"; shift;;
        --non-interactive) INTERACTIVE=0;;
        --version) set_value "$1" VERSION "$2"; shift;;
        --traceback-email) set_value "$1" TRACEBACK_EMAIL "$2"; shift;;
        --force-own-ca) FORCE_OWN_CA=1;;
        --http-proxy) set_value "$1" HTTP_PROXY "$2"; shift;;
        --http-username) set_value "$1" HTTP_USERNAME "$2"; shift;;
        --http-password) set_value "$1" HTTP_PASSWORD "$2"; shift;;
        --ssl-build-dir) set_value "$1" SSL_BUILD_DIR "$2"; shift;;
        --ssl-org) set_value "$1" SSL_ORG "$2"; shift;;
        --ssl-orgunit) set_value "$1" SSL_ORGUNIT "$2"; shift;;
        --ssl-common) set_value "$1" SSL_COMMON "$2"; shift;;
        --ssl-city) set_value "$1" SSL_CITY "$2"; shift;;
        --ssl-state) set_value "$1" SSL_STATE "$2"; shift;;
        --ssl-country) set_value "$1" SSL_COUNTRY "$2"; shift;;
        --ssl-email) set_value "$1" SSL_EMAIL "$2"; shift;;
        --ssl-password) set_value "$1" SSL_PASSWORD "$2"; shift;;
        --ssl-cname) SSL_CNAME_PARSED[CNAME_INDEX++]="--set-cname=$2"; shift;;
        --populate-config-channel) POPULATE_CONFIG_CHANNEL="${2:-Y}"; shift;;
        --start-services) START_SERVICES="${2:-Y}"; shift;;
        --rhn-user) set_value "$1" RHN_USER "$2"; shift;;
        --rhn-password) set_value "$1" RHN_PASSWORD "$2"; shift;;
        --ssl-use-existing-certs) USE_EXISTING_CERTS="${2:-Y}"; shift;;
        --ssl-ca-cert) set_value "$1" CA_CERT "$2"; shift;;
        --ssl-server-key) set_value "$1" SERVER_KEY "$2"; shift;;
        --ssl-server-cert) set_value "$1" SERVER_CERT "$2"; shift;;
        --) shift;
            if [ $# -gt 0 ] ; then
                echo "Error: Extra arguments found: $@"
                print_help
                exit 1
            fi
            break;;
        *) echo Error: Invalid option $1; exit 1;;
    esac
    shift
done

# params dep check
if [[ $INTERACTIVE == 0 && -z $ANSWER_FILE ]]; then
    echo "Option --non-interactive is for use only with option --answer-file."
    exit 1
fi


if [[ $INTERACTIVE == 0 ]]; then
    if [[ -z $POPULATE_CONFIG_CHANNEL ]]; then
        # if POPULATE_CONFIG_CHANNEL is not defined set its value to 'N'
        # because default value for this variable is 'Y'
        POPULATE_CONFIG_CHANNEL='N'
    elif [[ $(yes_no $POPULATE_CONFIG_CHANNEL) == 1 && ( -z  $RHN_USER || -z $RHN_PASSWORD ) ]]; then
        echo "Error: When --populate-config-channel is set to Yes both --rhn-user and --rhn-password have to be provided."
        exit 1
    fi
fi
ACCUMULATED_ANSWERS=""

generate_answers() {
    if [ "$INTERACTIVE" = 1 -a "$MANUAL_ANSWERS" = 1 ]; then
        local WRITE_ANSWERS
        echo "There were some answers you had to enter manually."
        echo "Would you like to have written those into file"
        echo -n "formatted as answers file? [Y/n]: "
        read WRITE_ANSWERS
        WRITE_ANSWERS=$(yes_no ${WRITE_ANSWERS:-Y})
        if [ "$WRITE_ANSWERS" = 1 ]; then
            local tmp=$(mktemp proxy-answers.txt.XXXXX)
            echo "Writing $tmp"
            echo "# Answer file generated by ${0##*/} at $(date)$ACCUMULATED_ANSWERS" > $tmp
        fi
    fi
}

default_or_input() {
    local MSG="$1"
    local VARIABLE="$2"
    local DEFAULT="$3"

    local INPUT
    local CURRENT_VALUE=${!VARIABLE}
    #in following code is used not so common expansion
    #var_a=${var_b:-word}
    #which is like: var_a = $var_b ? word
    DEFAULT=${CURRENT_VALUE:-$DEFAULT}
    local VARIABLE_ISSET=$(set | grep "^$VARIABLE=")

    echo -n "$MSG [$DEFAULT]: "
    if [ "$INTERACTIVE" = "1" -a  -z "$VARIABLE_ISSET" ]; then
        MANUAL_ANSWERS=1
        read INPUT
    elif [ -z "$VARIABLE_ISSET" ]; then
        echo "$DEFAULT"
    else
        DEFAULT=${!VARIABLE}
        echo "$DEFAULT"
    fi
    if [ -z "$INPUT" ]; then
        if [ "$DEFAULT" = "y/N" -o "$DEFAULT" = "Y/n" ]; then
            INPUT=$(yes_no "$DEFAULT")
        else
            INPUT="$DEFAULT"
        fi
    fi
    ACCUMULATED_ANSWERS+=$(printf "\n%q=%q" "$VARIABLE" "${INPUT:-$DEFAULT}")
    eval "$(printf "%q=%q" "$VARIABLE" "$INPUT")"
}

config_error() {
    if [ $1 -gt 0 ]; then
        echo "$2 Installation interrupted."
        /usr/sbin/rhn-proxy-activate \
            --server="$RHN_PARENT" \
            --http-proxy="$HTTP_PROXY" \
            --http-proxy-username="$HTTP_USERNAME" \
            --http-proxy-password="$HTTP_PASSWORD" \
            --ca-cert="$CA_CHAIN" \
            --deactivate --non-interactive
        generate_answers
        exit $1
    fi
}

# Return 0 if rhnParent is Hosted. Otherwise return 1.
is_hosted() {
    return 1
}

check_ca_conf() {
    if [ -f /root/ssl-build/rhn-ca-openssl.cnf ] \
        && awk '/^[[:space:]]*\[[[:space:]]*[_[:alnum:]]*[[:space:]]*]/ {CORRECT_SECTION=0} \
        /^[[:space:]]*\[[[:space:]]*CA_default[[:space:]]*]/ {CORRECT_SECTION=1} \
        /^[[:space:]]*copy_extensions[[:space:]]*=[[:space:]]*copy/ && CORRECT_SECTION==1 {exit 1}' \
        /root/ssl-build/rhn-ca-openssl.cnf > /dev/null \
            && [ ${#SSL_CNAME_PARSED[@]} -gt 0 ]; then
            cat <<WARNING
It seems you tried to use the --set-cname option. On inspection we noticed that the openssl configuration file we use is missing a critically important option. Without this option, not only will multi host SSL certificates not work, but the planet Earth will implode in a massive rip in the time/space continuum. To avoid this failure, we choose to gracefully exit here and request for you to edit the openssl configuration file
 /root/ssl-build/rhn-ca-openssl.cnf
and add this line:
 copy_extensions = copy
in
 [ CA_default ]
section.
Then re-run this script again.
WARNING
            generate_answers
            exit 3
    fi
}

YUM="yum install"
UPGRADE="yum upgrade"
# add -y for non-interactive installation
if [ "$INTERACTIVE" = "0" ]; then
    YUM="$YUM -y"
    UPGRADE="$UPGRADE -y"
fi
if [ -x /usr/bin/zypper ]; then
	YUM="zypper install"
	UPGRADE="zypper update"
	# add --non-interactive for non-interactive installation
	if [ "$INTERACTIVE" = "0" ]; then
		YUM="zypper --non-interactive install"
		UPGRADE="zypper --non-interactive update"
	fi
fi
SYSCONFIG_DIR=/etc/sysconfig/rhn
RHNCONF_DIR=/etc/rhn
HTTPDCONF_DIR=/etc/apache2
HTTPDCONFD_DIR=/etc/apache2/conf.d
#HTMLPUB_DIR=/var/www/html/pub
HTMLPUB_DIR=/srv/www/htdocs/pub
SQUID_DIR=/etc/squid
UP2DATE_FILE=$SYSCONFIG_DIR/up2date
SYSTEMID_PATH=$(awk -F '=[[:space:]]*' '/^[[:space:]]*systemIdPath[[:space:]]*=/ {print $2}' $UP2DATE_FILE)

PYTHON_CMD=""
systemctl is-active --quiet salt-minion && PYTHON_CMD="/usr/bin/python3"
systemctl is-active --quiet venv-salt-minion && PYTHON_CMD="/usr/lib/venv-salt-minion/bin/python"

if [[ -n $PYTHON_CMD ]]; then
    $PYTHON_CMD /usr/share/rhn/proxy-installer/fetch-certificate.py $SYSTEMID_PATH
    MASTER_CONF=/etc/salt/minion.d/susemanager.conf
    if [ -f /etc/venv-salt-minion/minion.d/susemanager.conf ]; then
        MASTER_CONF=/etc/venv-salt-minion/minion.d/susemanager.conf
    fi
    PROPOSED_PARENT=$(grep ^[[:blank:]]*master $MASTER_CONF | sed -e "s/.*:[[:blank:]]*//")
else
    PROPOSED_PARENT=$(awk -F= '/serverURL=/ {split($2, a, "/")} END { print a[3]}' $UP2DATE_FILE)
fi

if [ ! -r $SYSTEMID_PATH ]; then
    echo ERROR: This machine does not appear to be registered with SUSE Manager Server
    exit 2
fi

SYSTEM_ID=$(/usr/bin/xsltproc /usr/share/rhn/get_system_id.xslt $SYSTEMID_PATH | cut -d- -f2)

DIR=/usr/share/rhn/proxy-template
HOSTNAME=$(hostname -f)

default_or_input "SUSE Manager Parent" RHN_PARENT $PROPOSED_PARENT

sed -i -e "s/^serverURL=.*/serverURL=https:\/\/$RHN_PARENT\/XMLRPC/" /etc/sysconfig/rhn/up2date

CA_CHAIN=$(awk -F'[=;]' '/sslCACert=/ {a=$2} END {print a}' $UP2DATE_FILE)
echo "Using CA Chain (from $UP2DATE_FILE): $CA_CHAIN"

if ! /bin/su nobody -s /bin/sh --command="[ -r $CA_CHAIN ]" ; then

    echo Error: File $CA_CHAIN is not readable by nobody user.
    exit 1
fi

default_or_input "HTTP Proxy" HTTP_PROXY ''

if [ "$HTTP_PROXY" != "" ]; then

    default_or_input "HTTP username" HTTP_USERNAME ''

    if [ "$HTTP_USERNAME" != "" ]; then
        default_or_input "HTTP password" HTTP_PASSWORD ''
    fi
fi

VERSION=$(rpm -q --queryformat %{version} spacewalk-proxy-installer|cut -d. -f1-2)
ACCUMULATED_ANSWERS+=$(printf "\n%q=%q" "VERSION" "$VERSION")

default_or_input "Traceback email" TRACEBACK_EMAIL ''

# lets do SSL stuff
cat <<SSLCERT
You will now need to either generate or import an SSL certificate.
This SSL certificate will allow client systems to connect to this Uyuni Proxy
securely. Refer to the Uyuni Proxy Installation Guide for more information.
SSLCERT

default_or_input "Do you want to import existing certificates?" \
    USE_EXISTING_CERTS "y/N"
USE_EXISTING_CERTS=$(yes_no $USE_EXISTING_CERTS)

FORCE_OWN_CA=$(yes_no $FORCE_OWN_CA)

SSL_BUILD_DIR=${SSL_BUILD_DIR:-/root/ssl-build}
if ! [ -d $SSL_BUILD_DIR ] && [ 0$FORCE_OWN_CA -eq 0 ] && [ 0$USE_EXISTING_CERTS -eq 0 ]; then
    mkdir -p $SSL_BUILD_DIR
fi

if [ 0$FORCE_OWN_CA -eq 0 ] && \
    [ 0$USE_EXISTING_CERTS -eq 0 ] && \
    ! is_hosted "$RHN_PARENT" && \
    [ ! -f /root/ssl-build/RHN-ORG-PRIVATE-SSL-KEY ] && \
    ! diff $CA_CHAIN /root/ssl-build/RHN-ORG-TRUSTED-SSL-KEY &>/dev/null; then
        cat <<CA_KEYS

Please do copy your CA key and public certificate from $RHN_PARENT to
/root/ssl-build directory. You may want to execute this command:

 scp 'root@$RHN_PARENT:/root/ssl-build/{RHN-ORG-PRIVATE-SSL-KEY,RHN-ORG-TRUSTED-SSL-CERT,rhn-ca-openssl.cnf}' $SSL_BUILD_DIR

Please note that you need to re-run the proxy configure script after copying the certificate!

CA_KEYS
        exit 1
fi

check_ca_conf


if [ -n "$SSL_PASSWORD" ] ; then
    # use SSL_PASSWORD if already set
    RHN_SSL_TOOL_PASSWORD_OPTION="--password"
    RHN_SSL_TOOL_PASSWORD="$SSL_PASSWORD"
elif [ "$INTERACTIVE" = "0" ] && [ 0$USE_EXISTING_CERTS -eq 0 ] ; then
    # non-interactive mode but no SSL_PASSWORD :(
    config_error 4 "Please define SSL_PASSWORD."
fi

# get input for generating CA/server certs
if [ 0$USE_EXISTING_CERTS -eq 0 ]; then
    default_or_input "Organization" SSL_ORG ''
    default_or_input "Organization Unit" SSL_ORGUNIT "$HOSTNAME"
    default_or_input "Common Name" SSL_COMMON "$HOSTNAME"
    default_or_input "City" SSL_CITY ''
    default_or_input "State" SSL_STATE ''
    default_or_input "Country code" SSL_COUNTRY ''
    default_or_input "Email" SSL_EMAIL "$TRACEBACK_EMAIL"
    if [ ${#SSL_CNAME_PARSED[@]} -eq 0 ]; then
        VARIABLE_ISSET=$(set | grep "^SSL_CNAME=")
        if [ -z $VARIABLE_ISSET ]; then
            default_or_input "Cname aliases (separated by space)" SSL_CNAME_ASK ''
            CNAME=($SSL_CNAME_ASK)
            for ALIAS in ${CNAME[@]}; do
                SSL_CNAME_PARSED[CNAME_INDEX++]=--set-cname=$ALIAS
            done
            check_ca_conf
        fi
    fi
fi

if [ "$USE_EXISTING_CERTS" -eq "1" ]; then
    default_or_input "Path to CA SSL certificate:" CA_CERT ""
    if [ ! -e $CA_CERT ]; then
        config_error 1 "Given file doesn't exist!"
    fi

    default_or_input "Path to the Proxy Server's SSL key:" SERVER_KEY ""
    if [ ! -e $SERVER_KEY ]; then
        config_error 1 "Given file doesn't exist!"
    fi

    default_or_input "Path to the Proxy Server's SSL certificate:" SERVER_CERT ""
    if [ ! -e $SERVER_CERT ]; then
        config_error 1 "Given file doesn't exist!"
    fi
else
    if [ ! -f $SSL_BUILD_DIR/RHN-ORG-PRIVATE-SSL-KEY ]; then
        echo "Generating CA key and public certificate:"
        /usr/bin/rhn-ssl-tool --gen-ca --no-rpm -q \
            --dir="$SSL_BUILD_DIR" \
            --set-common-name="$SSL_COMMON" \
            --set-country="$SSL_COUNTRY" \
            --set-city="$SSL_CITY" \
            --set-state="$SSL_STATE" \
            --set-org="$SSL_ORG" \
            --set-org-unit="$SSL_ORGUNIT" \
            --set-email="$SSL_EMAIL" \
            $RHN_SSL_TOOL_PASSWORD_OPTION $RHN_SSL_TOOL_PASSWORD
        config_error $? "CA certificate generation failed!"
    fi
    CA_CERT=$SSL_BUILD_DIR/RHN-ORG-TRUSTED-SSL-CERT
fi

if [ "$USE_EXISTING_CERTS" -eq "0" ]; then
    echo "Using CA key at $SSL_BUILD_DIR/RHN-ORG-PRIVATE-SSL-KEY."

    IFS="."; arrIN=($HOSTNAME); unset IFS
    unset 'arrIN[${#arrIN[@]}-1]'
    unset 'arrIN[${#arrIN[@]}-1]'
    SYS_NAME=$(IFS=. eval 'echo "${arrIN[*]}"')

    echo "Generating SSL key and public certificate."
    /usr/bin/rhn-ssl-tool --gen-server -q --no-rpm \
        --set-hostname "$HOSTNAME" \
        --dir="$SSL_BUILD_DIR" \
        --set-country="$SSL_COUNTRY" \
        --set-city="$SSL_CITY" \
        --set-state="$SSL_STATE" \
        --set-org="$SSL_ORG" \
        --set-org-unit="$SSL_ORGUNIT" \
        --set-email="$SSL_EMAIL" \
        ${SSL_CNAME_PARSED[@]} \
        $RHN_SSL_TOOL_PASSWORD_OPTION $RHN_SSL_TOOL_PASSWORD
    config_error $? "SSL key generation failed!"
    SERVER_KEY=$SSL_BUILD_DIR/$SYS_NAME/server.key
    SERVER_CERT=$SSL_BUILD_DIR/$SYS_NAME/server.crt
fi

echo "Installing SSL certificates:"
/usr/bin/mgr-ssl-cert-setup --root-ca-file=$CA_CERT --server-cert-file=$SERVER_CERT --server-key-file=$SERVER_KEY

/usr/sbin/rhn-proxy-activate --server="$RHN_PARENT" \
                            --http-proxy="$HTTP_PROXY" \
                            --http-proxy-username="$HTTP_USERNAME" \
                            --http-proxy-password="$HTTP_PASSWORD" \
                            --ca-cert="$CA_CHAIN" \
                            --version="$VERSION" \
                            --non-interactive
config_error $? "Proxy activation failed!"

rpm -q rhn-apache >/dev/null
if [ $? -eq 0 ]; then
    echo "Package rhn-apache present - assuming upgrade:"
    echo "Force removal of /etc/httpd/conf/httpd.conf - backed up to /etc/httpd/conf/httpd.conf.rpmsave"
    mv /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf.rpmsave
fi

if [ -x /usr/sbin/rhn-proxy ]; then
    /usr/sbin/rhn-proxy stop
fi

$YUM spacewalk-proxy-management
# check if package install successfully
rpm -q spacewalk-proxy-management >/dev/null
if [ $? -ne 0 ]; then
    config_error 2 "Installation of package spacewalk-proxy-management failed."
fi
$UPGRADE

# size of squid disk cache will be 60% of free space on /var/cache/squid
# df -P give free space in kB
# * 60 / 100 is 60% of that space
# / 1024 is to get value in MB
SQUID_SIZE=$(df -P /var/cache/squid | awk '{a=$4} END {printf("%d", a * 60 / 100 / 1024)}')
SQUID_REWRITE="s|cache_dir ufs /var/cache/squid 15000 16 256|cache_dir ufs /var/cache/squid $SQUID_SIZE 16 256|g;"
SQUID_VER_MAJOR=$(squid -v | awk -F'[ .]' '/Version/ {print $4}')
if [ $SQUID_VER_MAJOR -ge 3 ] ; then
    # squid 3.X has acl 'all' built-in
    SQUID_REWRITE="$SQUID_REWRITE s/^acl all.*//;"
    # squid 3.2 and later need none instead of -1 for range_offset_limit
    SQUID_VER_MINOR=$(squid -v | awk -F'[ .]' '/Version/ {print $5}')
    if [[ $SQUID_VER_MAJOR -ge 4 || ( $SQUID_VER_MAJOR -eq 3 && $SQUID_VER_MINOR -ge 2 ) ]] ; then
        SQUID_REWRITE="$SQUID_REWRITE s/^range_offset_limit.*/range_offset_limit none/;"
    fi
fi
sed "$SQUID_REWRITE" < $DIR/squid.conf  > $SQUID_DIR/squid.conf
sed -e "s|\${session.ca_chain:/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT}|$CA_CHAIN|g" \
    -e "s/\${session.http_proxy}/$HTTP_PROXY/g" \
    -e "s/\${session.http_proxy_username}/$HTTP_USERNAME/g" \
    -e "s/\${session.http_proxy_password}/$HTTP_PASSWORD/g" \
    -e "s/\${session.rhn_parent}/$RHN_PARENT/g" \
    -e "s/\${session.traceback_mail}/$TRACEBACK_EMAIL/g" \
    < $DIR/rhn.conf  > $RHNCONF_DIR/rhn.conf

# systemid need to be readable by apache/proxy
for file in $SYSTEMID_PATH $UP2DATE_FILE; do
    chown root:www $file
    chmod 0640 $file
done

#Setup the cobbler stuff, needed to use koan through a proxy
sed -e "s/\$RHN_PARENT/$RHN_PARENT/g" < $DIR/cobbler-proxy.conf > $HTTPDCONFD_DIR/cobbler-proxy.conf

default_or_input "Do you want to use an existing ssh key for proxying ssh-push Salt minions ?" USE_EXISTING_SSH_PUSH_KEY 'y/N'
USE_EXISTING_SSH_PUSH_KEY=$(yes_no $USE_EXISTING_SSH_PUSH_KEY)

if [ "$USE_EXISTING_SSH_PUSH_KEY" -eq "1" ]; then
    default_or_input "Private SSH key for connecting to the next proxy in the chain (if any) for ssh-push minions" EXISTING_SSH_KEY ''
    while [[ -z "$EXISTING_SSH_KEY" || ( ! -r "$EXISTING_SSH_KEY" || ! -r "${EXISTING_SSH_KEY}.pub" ) ]]; do
        echo "'$EXISTING_SSH_KEY' or '${EXISTING_SSH_KEY}.pub' don't exist or are not readable."
        unset EXISTING_SSH_KEY
        default_or_input "Supply a valid path" EXISTING_SSH_KEY ''
    done
    /usr/sbin/mgr-proxy-ssh-push-init -k $EXISTING_SSH_KEY
else
    /usr/sbin/mgr-proxy-ssh-push-init
fi


CHANNEL_LABEL="rhn_proxy_config_$SYSTEM_ID"
default_or_input "Create and populate configuration channel $CHANNEL_LABEL?" POPULATE_CONFIG_CHANNEL 'Y/n'
POPULATE_CONFIG_CHANNEL=$(yes_no $POPULATE_CONFIG_CHANNEL)
if [ "$POPULATE_CONFIG_CHANNEL" = "1" ]; then
    RHNCFG_STATUS=1

    for i in $(seq 1 $INTERACTIVE_RETRIES) ; do
        default_or_input "SUSE Manager username:" RHN_USER ''
        CONFIG_CHANNELS=$(rhncfg-manager list-channels ${RHN_USER:+--username="${RHN_USER}"} ${RHN_PASSWORD:+--password="${RHN_PASSWORD}"} --server-name="$RHN_PARENT")

        RHNCFG_STATUS="$?"

        if [ "$RHNCFG_STATUS" != "0" ] ; then
            if [ "$INTERACTIVE" = "1" ] ; then
                # In case of incorrect username/password, we want to re-ask user
                unset RHN_USER
                unset RHN_PASSWORD
            fi
        else
            break
        fi
    done

    if [ "$RHNCFG_STATUS" != "0" ] ; then
        exit "$RHNCFG_STATUS"
    fi

    if ! grep -q -E "^ +$CHANNEL_LABEL$" <<<"$CONFIG_CHANNELS" ; then
        rhncfg-manager create-channel --server-name "$RHN_PARENT" "$CHANNEL_LABEL"
    fi
    arr_conf_list=( $HTTPDCONF_DIR/vhosts.d/ssl.conf
                    $RHNCONF_DIR/rhn.conf
                    $SQUID_DIR/squid.conf
                    $HTTPDCONFD_DIR/cobbler-proxy.conf
                    $HTTPDCONF_DIR/httpd.conf )
    for conf_file in ${arr_conf_list[*]}; do
        [ -e ${conf_file} ] && arr_conf=(${arr_conf[*]} ${conf_file})
    done
    rhncfg-manager update --server-name "$RHN_PARENT" \
        --channel="$CHANNEL_LABEL" \
        ${arr_conf[*]}
fi

open_firewall_ports

default_or_input "Activate advertising proxy via SLP?" ACTIVATE_SLP "Y/n"
ACTIVATE_SLP=$(yes_no $ACTIVATE_SLP)
if [ $ACTIVATE_SLP -ne 0 ]; then
    if [ -x /usr/bin/firewall-cmd ]; then
      firewall-cmd --state 2> /dev/null
      if [ $? -eq 0 ]; then
        firewall-cmd --permanent --zone=public --add-service=slp
        firewall-cmd --reload
      else
        firewall-offline-cmd --zone=public --add-service=slp
      fi
    else
      echo "firewalld not installed" >&2
    fi
    /usr/bin/systemctl enable slpd
    /usr/bin/systemctl start slpd
fi

echo "Enabling Spacewalk Proxy."
for service in squid apache2 salt-broker; do
    /usr/bin/systemctl enable $service
done

# default is 1
START_SERVICES=$(yes_no ${START_SERVICES:-1})
if [ "$START_SERVICES" = "1" ]; then
    /usr/sbin/rhn-proxy restart
else
    echo Skipping start of services.
    echo Use "/usr/sbin/rhn-proxy start" to manually start proxy.
fi

echo "Restarting salt-broker."
/usr/bin/systemctl restart salt-broker

# do not tell admin to configure proxy on next login anymore
rm -f /etc/motd

generate_answers
