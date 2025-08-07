# Edit deb822 file such as ubuntu.sources
# Example: awk -f edit-ubuntu-sources.awk \
#              -v action=enable \
#              -v distro=noble \
#              -v repo=universe \
#              ubuntu.sources

#!/usr/bin/awk -f

BEGIN {
    RS = "\n\n"
    FS = "\n"
    OFS = "\n"
}

{
    current_record = $0

    for (i = 1; i <= NF; i++) {
        if ($i ~ /^Enabled:/) {
            enabled = tolower($i)
            enabled_line = i
        }
        if ($i ~ /^Suites:/) {
            suites = tolower($i)
        }
        if ($i ~ /^Components:/) {
            components = tolower($i)
        }
        if ($i ~ /^Architectures:[[:space:]]*$/) {
            $i = "Architectures: amd64"
        }
    }

    if (suites ~ distro && components ~ repo) {
        if (action == "enable" && enabled ~ /no|false/) {
            $enabled_line = "Enabled: yes"
        } else if (action == "disable" && enabled ~ /yes|true/) {
            $enabled_line = "Enabled: no"
        }
    }

    print $0 "\n"
}
