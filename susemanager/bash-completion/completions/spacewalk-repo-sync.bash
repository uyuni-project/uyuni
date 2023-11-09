__spacewalk_repo_sync_options="h:help l:list s:show-packages u:url= c:channel=
    p:parent-channel= d:dry-run :latest g:config= t:type= f:fail n:non-interactive
    i:include= e:exclude= :email :traceback-mail= :no-errata :no-packages :sync-kickstart
    :force-all-errata :batch-size= Y:deep-verify v:verbose"

_spacewalk_repo_sync_completions() {
    local cur IFS=$' \t\n'
    cur="${COMP_WORDS[COMP_CWORD]}"

    # Replace short options with long versions
    local short long c i
    for c in $__spacewalk_repo_sync_options; do
        short="${c%%:*}"
        long="${c#*:}"
        if [ "-$short" = "$cur" ]; then
            _add_suffix $long
            COMPREPLY[i++]="--$opt"
            return
        fi
    done


    # Complete channels
    if _is_option c channel; then
        cur="${cur#=}"
        _complete_list $(spacewalk-remove-channel --list | sed 's/\s+//')
        return
    fi

    # Complete base channels
    if _is_option p parent-channel; then
        cur="${cur#=}"
        _complete_list $(spacewalk-remove-channel --list | grep '^\w')
        return
    fi

    # Complete repo types
    if _is_option t type; then
        cur="${cur#=}"
        _complete_list "yum uln deb"
        return
    fi

    # Complete filenames for config
    if _is_option c config; then
        cur="${cur#=}"
        _complete_files
        return
    fi

    # Long options
    case "$cur" in
    *=) ;;
    *)
        for o in $__spacewalk_repo_sync_options; do
            o="${o#*:}"
            if [[ "--$o" = "$cur"* ]]; then
                _add_suffix $o
                COMPREPLY[i++]="--$opt"
            fi
        done
        ;;
    esac
}

complete -o nospace -F _spacewalk_repo_sync_completions spacewalk-repo-sync
