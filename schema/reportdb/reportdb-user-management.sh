#! /bin/bash

DBNAME="reportdb"
INTERACTIVE=1
INTERACTIVE_RETRIES=3

if [ 0$UID -gt 0 ]; then
    echo Run as root.
    exit 1
fi


print_help() {
    cat <<HELP
usage: reportdb-user-management.sh [options]

options:
  --help 
            show this help message and exit
  --non-interactive
            Switches to non-interactive mode
  --dbuser=DBUSER
            Report DB User
  --dbpassword=DBPASSWORD
            Report DB Password
  --add
            Add the new user
  --delete
            Delete the user
  --modify
            Set a new password

HELP
    exit 1
}

default_or_input() {
    local MSG="$1"
    local VARIABLE="$2"
    local DEFAULT="$3"
    local SILENT="$4"

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
        if [ "$SILENT" = 1 ]; then
          read -s INPUT
        else
          read INPUT
        fi
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

set_value() {
    local OPTION="$1"
    local VAR="$2"
    local ARG="$3"
    [[ "$ARG" =~ ^- ]] \
        && echo "$0: option $OPTION requires argument! Use answer file if your argument starts with '-'." \
        && print_help
    eval "$(printf "%q=%q" "$VAR" "$ARG")"
}

OPTS=$(getopt --longoptions=help,non-interactive,add,delete,modify,action:,dbuser:,dbpassword: -n ${0##*/} -- h "$@")

if [ $? != 0 ] ; then
    print_help
fi

# It is getopt's responsibility to make this safe
eval set -- "$OPTS"

while : ; do
    case "$1" in
        --help|-h)  print_help;;
        --non-interactive) INTERACTIVE=0;;
        --dbuser) set_value "$1" DBUSER "$2"; shift;;
        --dbpassword) set_value "$1" DBPASSWORD "$2"; shift;;
        --add) ACTION="a";;
        --delete) ACTION="d";;
        --modify) ACTION="m";;
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


if [ "$INTERACTIVE" = "1" ] ; then
  for i in $(seq $INTERACTIVE_RETRIES) ; do
    default_or_input "[a]dd/[m]odify/[d]elete user. Default is " ACTION 'm'
      case "$ACTION" in
        a|m|d)
        break
        ;;
        *)
          echo "Invalid input. Please [a]dd/[m]odify/[d]elete"
          unset ACTION
        ;;
      esac
  done
    
  if [ -z $ACTION ]; then
    echo "Invalid input. Exit"
    exit 1
  fi
  
  default_or_input "User:" DBUSER ''
  
  if [ "$ACTION" != "d" ]; then
    default_or_input "Password:" DBPASSWORD '' '1'
    echo
  fi
  
  default_or_input "Confirm? [y/n]" CONFIRM 'y'
  
  if [ "$CONFIRM" != "y" ]; then
    echo "Answer is not y. Exiting"
    exit 0
  fi
fi

QUERY=""
case "$ACTION" in
  a)
    QUERY="CREATE ROLE ${DBUSER} WITH LOGIN PASSWORD '${DBPASSWORD}' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION VALID UNTIL infinity;
      GRANT CONNECT ON DATABASE ${DBNAME} TO ${DBUSER};
      GRANT USAGE ON SCHEMA public TO ${DBUSER};
      GRANT SELECT ON ALL TABLES IN SCHEMA public TO ${DBUSER};
      GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO ${DBUSER};"
  ;;
  m)
    QUERY="ALTER USER ${DBUSER} PASSWORD '${DBPASSWORD}';"
  ;;
  d)
    QUERY="DROP OWNED BY ${DBUSER}; DROP ROLE ${DBUSER};"
  ;;
esac

echo ${QUERY} | spacewalk-sql --select-mode -
