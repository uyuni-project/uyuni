__mgr_sync_cmds="list add refresh delete sync"
__mgr_sync_add_cmds="channels credentials products" # list, add
__mgr_sync_delete_cmds="credentials"
__mgr_sync_sync_cmds="channels"

__mgr_sync_options="h:help version v:verbose s:store-credentials d:debug"
__mgr_sync_add_options="h:help from-mirror primary no-optional no-recommends no-sync"
__mgr_sync_list_options="h:help e:expand f:filter no-optional c:compact"
__mgr_sync_refresh_options="h:help refresh-channels from-mirror= schedule"
__mgr_sync_delete_options="h:help"
__mgr_sync_sync_options="h:help with-children"

__mgr_sync_arg_opts="d:debug f:filter f:from-mirror"

# Returns success if a word is an option that expects an argument
# 1: The word to be tested
_is_arg_opt() {
    local short long o opt
    opt=${1##+(-)}
    for o in ${__mgr_sync_arg_opts}; do
        short="${o%%:*}"
        long="${o#*:}"
        if [ "$opt" = "$short" ] || [ "$opt" = "$long" ]; then
            return 0
        fi
    done
    return 1
}

# Perform option completion for the word stored in the 'cur' variable
# @: List of available options
_complete_options() {
    # Replace short options with long versions
    local short long c i
    for c in "$@"; do
        short="${c%%:*}"
        long="${c#*:}"
        if [ "-$short" = "$cur" ]; then
            _add_suffix $long
            COMPREPLY[i++]="--$opt"
            return
        fi
    done

    # Complete long options
    local o
    case "$cur" in
        --*=) ;;
        *)
            for o in "$@"; do
                o="${o#*:}"
                if [[ "--$o" = "$cur"* ]]; then
                    _add_suffix $o
                    COMPREPLY[i++]="--$opt"
                fi
            done
            ;;
    esac
}

# Perform a context-sensitive option completion for the word stored in the
# 'cur' variable, depending on the command
# 1: The command
_complete_cmd_options() {
    case "$1" in
        list)
            _complete_options $__mgr_sync_list_options
            ;;
        add)
            _complete_options $__mgr_sync_add_options
            ;;
        refresh)
            _complete_options $__mgr_sync_refresh_options
            ;;
        delete)
            _complete_options $__mgr_sync_delete_options
            ;;
        sync)
            _complete_options $__mgr_sync_sync_options
            ;;
        *)
            _complete_options $__mgr_sync_options
            ;;
    esac
}

_mgr_sync_completions() {
    local cur word cmd1 cmd2 i IFS=$' \t\n'
    cur="${COMP_WORDS[COMP_CWORD]}"

    # Do not complete for options that expect args
    local short long o
    for o in ${__mgr_sync_arg_opts}; do
        short="${o%%:*}"
        long="${o#*:}"
        _is_option "$short" "$long" && return
    done

    # Parse the command and subcommand
    for i in $(seq 1 $(($COMP_CWORD-1)) ); do
        word=${COMP_WORDS[i]}
        if [[ "$word" != -* ]]; then
            # Skip the option arguments
            _is_arg_opt ${COMP_WORDS[i-1]} && continue
            if [ -z "$cmd1" ]; then
                cmd1=$word
            else
                cmd2=$word
                break
            fi
        fi
    done

    case "$cur" in
        --*=) ;;
        -*)
            # Complete options
            _complete_cmd_options $cmd1 ;;
        *)
            # Complete commands
            if [ -z "$cmd2" ]; then
                case "$cmd1" in
                    list)
                        _complete_list $__mgr_sync_add_cmds
                        ;;
                    add)
                        _complete_list $__mgr_sync_add_cmds
                        ;;
                    refresh)
                        ;;
                    delete)
                        _complete_list $__mgr_sync_delete_cmds
                        ;;
                    sync)
                        _complete_list $__mgr_sync_sync_cmds
                        ;;
                    *)
                        _complete_list $__mgr_sync_cmds
                        ;;
                esac
            else
                _complete_cmd_options $cmd1
            fi
            ;;
    esac
}

complete -o nospace -F _mgr_sync_completions mgr-sync
