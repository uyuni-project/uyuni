#!/bin/bash
#
# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

set -eo pipefail

# Compute the required directories paths
SCRIPT_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
UYUNI_DIR="$(realpath "$SCRIPT_DIR/../../..")"

# Default configuration values
DEPLOY_TARGET="backend"
DEPLOY_HOST="server.tf.local"
DEPLOY_MODE="remote-container"
DEPLOY_NAMESPACE="default"
CONTAINER_BACKEND="podman"
RESTART_TOMCAT=false
RESTART_TASKOMATIC=false
VERBOSE=false

# SSH configuration if needed by the chosen deploy mode
SSH_PORT="22"
SSH_USER="root"

print() {
    echo "$1"
}

print_detailed() {
    if [ "$VERBOSE" = true ]; then
        echo "$1"
    fi
}

print_error() {
    echo "$1" >&2
}

usage() {
    print "Usage: $0 <type> [options]"
    print ""
    print "Deploys the Uyuni webapp."
    print ""
    print "Mandatory Arguments:"
    print "  <type>                 The type of deployment to perform: backend, frontend, salt or all."
    print ""
    print "Optional Arguments:"
    print "  -m,--mode <mode>        Deployment mode: local, remote, container, remote-container, kubectl (default: $DEPLOY_MODE)"
    print "  -h,--host <hostname>    The target host for the deployment."
    print "  -b,--backend <backend>  Container backend: podman, podman-remote, kubectl (default: $CONTAINER_BACKEND)"
    print "  -n,--namespace <ns>     Kubernetes namespace where to look for the pod (default: $DEPLOY_NAMESPACE)"
    print "  -r,--restart            Restart tomcat and taskomatic at the end of the deployment"
    print "  --restart-tomcat        Restart only tomcat at the end of the deployment"
    print "  --restart-taskomatic    Restart only taskomatic at the end of the deployment"
    print "  -v,--verbose            Print detailed messages"
    print "  --help                  Show this help message."
    print
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--host)
            DEPLOY_HOST="$2"
            shift 2
            ;;
        -m|--mode)
            DEPLOY_MODE="$2"
            shift 2
            ;;
        -b|--backend)
            CONTAINER_BACKEND="$2"
            shift 2
            ;;
        -n|--namespace)
            DEPLOY_NAMESPACE="$2"
            shift 2
            ;;
        -r|--restart)
            RESTART_TOMCAT=true
            RESTART_TASKOMATIC=true
            shift
            ;;
        --restart-tomcat)
            RESTART_TOMCAT=true
            shift
            ;;
        --restart-taskomatic)
            RESTART_TASKOMATIC=true
            shift
            ;;
         --help)
            usage
            exit 0
            ;;
        frontend|backend|salt|all)
            DEPLOY_TARGET="$1"
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Check mandatory parameters
if [ -z "$DEPLOY_HOST" ]; then
    print_error "Error: --host parameter is mandatory."
    usage
    exit 1
fi

SSH_SOCKET_FILE="${HOME}/.ssh/manager-build-tunnel-${DEPLOY_HOST}-${SSH_USER}"
SSH_SOCKET_OPTION="-o ControlPath=${SSH_SOCKET_FILE}"
# Note: This is a string, but we will split it into an array for ssh
SSH_COMMAND_ARGS_STRING="${SSH_SOCKET_OPTION} -p ${SSH_PORT} ${SSH_USER}@${DEPLOY_HOST}"

# Executor depends on the deploy mode
EXECUTOR_COMMAND=""
# This must be a Bash array to handle arguments correctly
EXECUTOR_PARAMETERS=()

# Set by parse_versions
SPACEWALK_JAVA_VERSION=""
BRANDING_VERSION=""

# Executes a command on the target system using the configured executor.
deploy_execute() {
    # Execute the command with parameters as a proper array
    "$EXECUTOR_COMMAND" "${EXECUTOR_PARAMETERS[@]}" $@
}

# Deploys a local directory to the target
deploy_directory() {
    local source_dir="$1"
    local dest_dir="$2"
    local rsync_params="$3"

    print "Deploying directory $source_dir to $dest_dir..."

    # Create a remote temporary directory
    local temp_dir
    temp_dir=$(deploy_execute mktemp -d | tr -d "[:space:]")

    print_detailed "  Streaming local $source_dir to remote $temp_dir..."
    tar c -C "$source_dir" -f - . | deploy_execute tar xf - -C ${temp_dir}/ --no-same-owner --no-same-permissions

    print_detailed "  Syncing from remote $temp_dir to $dest_dir..."
    deploy_execute rsync -a ${rsync_params} ${temp_dir}/ ${dest_dir}

    print_detailed "  Cleaning up remote temp dir..."
    deploy_execute rm -rf $temp_dir

    print_detailed "  Deploy directory finished."
}

show_configuration() {
    print_detailed "Uyuni Deployment Script"

    print_detailed "  Repository:  $UYUNI_DIR"
    print_detailed "  Host:        $DEPLOY_HOST"
    print_detailed "  Mode:        $DEPLOY_MODE"

    if [ "$DEPLOY_MODE" = "remote" ] || [ "$DEPLOY_MODE" = "remote-container" ]; then
        print_detailed "  User:        $SSH_USER"
        print_detailed "  Port:        $SSH_PORT"
    fi

    if [ "$DEPLOY_MODE" = "container" ] || [ "$DEPLOY_MODE" = "remote-container" ]; then
        print_detailed "  Backend:     $CONTAINER_BACKEND"
    fi
    
    if [ "$DEPLOY_MODE" = "kubectl" ]; then
        print_detailed "  Namespace:   $DEPLOY_NAMESPACE"
    fi

    print_detailed
}

parse_versions() {
    # Prefer xmllint, it's faster than calling maven
    if command -v xmllint &> /dev/null; then
        SPACEWALK_JAVA_VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" "$UYUNI_DIR/java/pom.xml")
        BRANDING_VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" "$UYUNI_DIR/branding/pom.xml")
    else
        SPACEWALK_JAVA_VERSION=$(mvn -f "$UYUNI_DIR/java" -q help:evaluate -DforceStdout -Dexpression=project.version)
        BRANDING_VERSION=$(mvn -f "$UYUNI_DIR/branding" -q help:evaluate -DforceStdout -Dexpression=project.version)
    fi

    print_detailed "Using spacewalk-java version ${SPACEWALK_JAVA_VERSION} and branding version ${BRANDING_VERSION}"
}

check_prerequisites() {
    print_detailed "Checking deploy mode and server access..."

    # Handle mgrctl backend parameter
    local mgrctl_params=()
    if [[ -n "$CONTAINER_BACKEND" ]]; then
        if [[ "$CONTAINER_BACKEND" =~ ^(podman|podman-remote|kubectl)$ ]]; then
            mgrctl_params=("--backend" "$CONTAINER_BACKEND")
        else
            print "Warning: Invalid CONTAINER_BACKEND '$CONTAINER_BACKEND'. Ignoring."
        fi
    fi

    # Split the string into an array
    read -r -a ssh_args <<< "$SSH_COMMAND_ARGS_STRING"

    case "$DEPLOY_MODE" in
        local)
            print_detailed "  Mode: local"
            EXECUTOR_COMMAND="sh"
            EXECUTOR_PARAMETERS=("-c")
            ;;

        remote)
            print_detailed "  Mode: remote"
            EXECUTOR_COMMAND="ssh"
            EXECUTOR_PARAMETERS=("${ssh_args[@]}")

            # Check/open SSH socket
            if [[ ! -S "$SSH_SOCKET_FILE" ]]; then
                print_detailed "  Opening new SSH control socket: $SSH_SOCKET_FILE"
                ssh -M -f -N -C "${ssh_args[@]}"
            else
                print_detailed "  Using existing SSH control socket: $SSH_SOCKET_FILE"
            fi
            ;;

        container)
            print_detailed "  Mode: container"
            # Check for local mgrctl
            if ! command -v mgrctl &> /dev/null; then
                print_error "Error: mgrctl is not in the PATH. Please install mgrctl first."
                exit 1
            fi
            print_detailed "  Local mgrctl found."

            EXECUTOR_COMMAND="mgrctl"
            EXECUTOR_PARAMETERS=("exec" "${mgrctl_params[@]}" "-i" "--")
            ;;

        remote-container)
            print_detailed "  Mode: remote-container"

            # Check/open SSH socket first
            if [[ ! -S "$SSH_SOCKET_FILE" ]]; then
                print_detailed "  Opening new SSH control socket: $SSH_SOCKET_FILE"
                ssh -M -f -N -C "${ssh_args[@]}"
            else
                print_detailed "  Using existing SSH control socket: $SSH_SOCKET_FILE"
            fi

            # Check for remote mgrctl
            print_detailed "  Checking for remote mgrctl..."
            if ! ssh "${ssh_args[@]}" "mgrctl --version" &> /dev/null; then
                print_error "Error: mgrctl is not in the PATH on $DEPLOY_HOST. Please install mgrctl first."
                exit 1
            fi
            print_detailed "  Remote mgrctl found."

            EXECUTOR_COMMAND="ssh"
            # Parameters are SSH args *plus* the remote mgrctl command
            EXECUTOR_PARAMETERS=("${ssh_args[@]}" "mgrctl" "exec" "${mgrctl_params[@]}" "-i" "--")
            ;;
        kubectl)
            print_detailed "  Mode: kubectl"
            # Check for local kubectl
            if ! command -v kubectl &> /dev/null; then
                print_error "Error: kubectl is not in the PATH. Please install kubectl first."
                exit 1
            fi
            print_detailed "  Local kubectl found."

            EXECUTOR_COMMAND="kubectl"

            EXECUTOR_POD=`kubectl get pod -n ${DEPLOY_NAMESPACE} -l 'app.kubernetes.io/part-of=uyuni,app.kubernetes.io/component=server' -o name`
            if test $? -ne 0; then
                print_error "Error: failed to find pod to work with."
            fi
            EXECUTOR_PARAMETERS=("exec" "-n" ${DEPLOY_NAMESPACE} "-ti" ${EXECUTOR_POD} "-c" "uyuni" "--")
            ;;


        *)
            print_error "Error: The deploy mode '$DEPLOY_MODE' is invalid."
            print_error "Valid modes are: local, remote, container, remote-container, kubectl"
            exit 1
            ;;
    esac

    print_detailed "  Executor command: $EXECUTOR_COMMAND"
    print_detailed "  Executor parameters: ${EXECUTOR_PARAMETERS[*]}"
}

deploy_backend() {
    local TARGET_DIR="/usr/share/susemanager/www/tomcat/webapps/rhn"
    local SOURCE_WEBAPP_DIR="${UYUNI_DIR}/java/webapp/target/webapp-${SPACEWALK_JAVA_VERSION}"

    # Check if the file to deploy exists
    if [ ! -d "$SOURCE_WEBAPP_DIR" ]; then
        print_error "Error: Webapp directory $SOURCE_WEBAPP_DIR does not exist."
        print_error "Please build the webapp first by calling mvn package."
        exit 1
    fi

    print_detailed "  Webapp found: $SOURCE_WEBAPP_DIR"

    print "Deploying webapp on Tomcat and Taskomatic..."
    deploy_directory "$SOURCE_WEBAPP_DIR" "$TARGET_DIR" "--delete --exclude=log4j2.xml"

    print "Linking branding jar..."
    deploy_execute mv "${TARGET_DIR}/WEB-INF/lib/branding-${BRANDING_VERSION}.jar" /usr/share/rhn/lib/java-branding.jar
    deploy_execute ln -sf /usr/share/rhn/lib/java-branding.jar ${TARGET_DIR}/WEB-INF/lib/java-branding.jar

    print "Linking rhn jar..."
    deploy_execute mv ${TARGET_DIR}/WEB-INF/lib/core-${SPACEWALK_JAVA_VERSION}.jar /usr/share/rhn/lib/rhn.jar
    deploy_execute ln -sf /usr/share/rhn/lib/rhn.jar "${TARGET_DIR}/WEB-INF/lib/rhn.jar"

    print "Linking jars for Taskomatic..."
    deploy_execute ln -sf "${TARGET_DIR}/WEB-INF/lib/*.jar" /usr/share/spacewalk/taskomatic
}

deploy_frontend() {
    local FRONTEND_DIR="$UYUNI_DIR/web/html/src/dist"
    local TARGET_DIR="/usr/share/susemanager/www/htdocs"

    if [ ! -d "$FRONTEND_DIR" ]; then
        print_error "Error: Frontend directory $SOURCE_WEBAPP_DIR does not exist."
        print_error "Please build the frontend first by calling npm."
        exit 1
    fi

    print "Deploying frontend files to remote host..."
    deploy_directory "$FRONTEND_DIR" "$TARGET_DIR"
}

deploy_salt() {
    local SALT_SOURCE_DIR="$UYUNI_DIR/susemanager-utils/susemanager-sls"
    local TARGET_DIR="/usr/share/susemanager"

    print "Deploying Salt SLS files to remote host..."
    deploy_directory "$SALT_SOURCE_DIR/salt" "$TARGET_DIR/salt"

    print "Deploying Salt grains, beacons, modules and pillars to remote host..."
    deploy_directory "$SALT_SOURCE_DIR/src/grains" "$TARGET_DIR/salt/_grains"
    deploy_directory "$SALT_SOURCE_DIR/src/beacons" "$TARGET_DIR/salt/_beacons"
    deploy_directory "$SALT_SOURCE_DIR/src/modules" "$TARGET_DIR/salt/_modules"
    deploy_directory "$SALT_SOURCE_DIR/modules" "$TARGET_DIR/modules"

    print "Deploying Salt reactor to remote host..."
    deploy_directory "$SALT_SOURCE_DIR/reactor" "$TARGET_DIR/reactor"
}

restart_services() {
    if [ "$RESTART_TOMCAT" = true ]; then
        print "Launching Tomcat restart..."
        deploy_execute nohup rctomcat restart
    fi

    if [ "$RESTART_TASKOMATIC" = true ]; then
        print "Launching Taskomatic restart..."
        deploy_execute nohup rctaskomatic restart
    fi
}

main() {
    show_configuration
    parse_versions
    check_prerequisites

    case $DEPLOY_TARGET in
        backend)
            deploy_backend
            ;;

        frontend)
            deploy_frontend
            ;;

        salt)
            deploy_salt
            ;;

        all)
            deploy_backend
            deploy_frontend
            deploy_salt
            ;;

    esac

    restart_services
    exit 0
}

main
