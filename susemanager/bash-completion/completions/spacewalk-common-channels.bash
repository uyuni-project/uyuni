__spacewalk_common_channels_options="c:config= u:user= p:password= s:server= k:keys= n:dry-run
    a:archs= v:verbose l:list d:default-channels h:help"

_spacewalk_common_channels_completions() {
    local cur IFS=$' \t\n'
    cur="${COMP_WORDS[COMP_CWORD]}"

    # Replace short options with long versions
    local short long c i
    for c in $__spacewalk_common_channels_options; do
        short="${c%%:*}"
        long="${c#*:}"
        if [ "-$short" = "$cur" ]; then
            _add_suffix $long
            COMPREPLY[i++]="--$opt"
            return
        fi
    done

    # Complete filenames for config
    if _is_option c config; then
        cur="${cur#=}"
        _complete_files
        return
    fi


    # Complete architectures
    if _is_option a archs; then
        cur="${cur#=}"
        _complete_list $(spacewalk-common-channels --list | tail -n +2 |
            sed 's/^[^:]*:\s\+//' | sed 's/, /\n/g' | sort | uniq)
        return
    fi

    # Long options
    case "$cur" in
    --*=) ;;
    -*)
        for o in $__spacewalk_common_channels_options; do
            o="${o#*:}"
            if [[ "--$o" = "$cur"* ]]; then
                _add_suffix $o
                COMPREPLY[i++]="--$opt"
            fi
        done
        ;;
    *)
        # Complete channels
        _complete_list $(spacewalk-common-channels --list | tail -n +2 | sed 's/:.*$//')
        ;;
    esac
}

complete -o nospace -F _spacewalk_common_channels_completions spacewalk-common-channels
