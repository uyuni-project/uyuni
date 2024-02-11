__spacewalk_remove_channel_options="v:verbose l:list c:channel= a:channel-with-children=
    u:unsubscribe justdb force p:skip-packages skip-kickstart-trees just-kickstart-trees
    skip-channels username= password= h:help"

_spacewalk_remove_channel_completions() {
    local cur IFS=$' \t\n'
    cur="${COMP_WORDS[COMP_CWORD]}"

    # Replace short options with long versions
    local short long c i
    for c in $__spacewalk_remove_channel_options; do
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
    if _is_option a channel-with-children; then
        cur="${cur#=}"
        _complete_list $(spacewalk-remove-channel --list | grep '^\w')
        return
    fi

    # Long options
    case "$cur" in
    *=) ;;
    *)
        for o in $__spacewalk_remove_channel_options; do
            o="${o#*:}"
            if [[ "--$o" = "$cur"* ]]; then
                _add_suffix $o
                COMPREPLY[i++]="--$opt"
            fi
        done
        ;;
    esac
}

complete -o nospace -F _spacewalk_remove_channel_completions spacewalk-remove-channel
