# Edit deb822 file such as ubuntu.sources
# Example: awk -f edit-ubuntu-sources.awk \
#              -v action=disable \
#              -v distro=noble \
#              -v repo=universe \
#              ubuntu.sources

BEGIN            {
in_relevant_block = 0
enabled_processed = 0
}

/^Suites: */     {
    # Match the distro as substring in a suite
    in_relevant_block = (index($0, distro) != 0)
    print $0
    next
}

/^Components: */ {
    if (in_relevant_block) {
        if (action == "enable") {
            if (index($0, repo) == 0) {
                $0 = $0 " " repo
            }
        }
        if (action == "disable") {
            gsub(repo, "", $0)
            gsub("  ", " ", $0)
        }
    }
    print $0
    # Add the Enabled line after the components line if it's not present
    if (in_relevant_block && action == "enable" && index($0, "Enabled:") == 0) {
        print "Enabled: yes"
        enabled_processed = 1
    }
    next
}

/^Enabled: */ {
    if (enabled_processed) {
        next
    }
    if (in_relevant_block) {
        if (action == "enable") {
            print "Enabled: yes"
        } else {
            print "Enabled: no"
        }
    } else {
        print $0
    }
    next
}

# Remove Architectures section if it's empty
/^Architectures: *$/ {
    next
}

# Print any other line
{
    print
}
