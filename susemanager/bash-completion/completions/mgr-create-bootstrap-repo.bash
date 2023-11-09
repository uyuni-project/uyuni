__mgr_create_bootstrap_repo_options="h:help n:dryrun i:interactive l:list c:create= a:auto
    datamodule= d:debug f:flush no-flush force with-custom-channels with-parent-channel="

_mgr_create_bootstrap_repo_completions() {
    local cur IFS=$' \t\n'
    cur="${COMP_WORDS[COMP_CWORD]}"

    # Replace short options with long versions
    local short long c i
    for c in $__mgr_create_bootstrap_repo_options; do
        short="${c%%:*}"
        long="${c#*:}"
        if [ "-$short" = "$cur" ]; then
            _add_suffix $long
            COMPREPLY[i++]="--$opt"
            return
        fi
    done

    # Complete channels
    if _is_option c create; then
        cur="${cur#=}"
        _complete_list $(mgr-create-bootstrap-repo --list | sed 's/^[^\.]*\. //')
        return
    fi

    # Long options
    case "$cur" in
        *=) ;;
        -*)
            for o in $__mgr_create_bootstrap_repo_options; do
                o="${o#*:}"
                if [[ "--$o" = "$cur"* ]]; then
                    _add_suffix $o
                    COMPREPLY[i++]="--$opt"
                fi
            done
            ;;
    esac
}

complete -o nospace -F _mgr_create_bootstrap_repo_completions mgr-create-bootstrap-repo
