# Edit deb822 file such as ubuntu.sources
# Example: awk -f edit-ubuntu-sources.awk \
#              -v action=disable \
#              -v distro=noble \
#              -v repo=universe \
#              ubuntu.sources

BEGIN             { suites = ""
                    components = ""
                    in_relevant_block = 0
                  }

# Capture Suites and reset relevant block flag
/^Suites: */      { suites = $0
                    sub(/^Suites: */, "", suites)
                    if (match(suites " ", distro " ") != 0) {
                        in_relevant_block = 1
                    } else {
                        in_relevant_block = 0
                    }
                    print $0
                  }

/^Components: */  { components = $0
                    sub(/^Components: */, "", components)
                    if (in_relevant_block)
                    {
                      if (action == "enable")
                      {
                        if (match(components " ", repo " ") == 0)
                          components = components " " repo
                      }
                      if (action == "disable")
                      {
                        sub(repo, "", components)
                        sub(/  */, " ", components)
                      }
                    }
                    print "Components: " components
                  }

/^Enabled: */     {
                    if (in_relevant_block && action == "enable")
                    {
                        print "Enabled: yes"
                    } else {
                        print $0
                    }
                    in_relevant_block = 0
                  }

/^Architectures: *$/ {
                    next
                  }

!/^Suites: */ && !/^Components: */ && !/^Enabled: */ && !/^Architectures: *$/ {
                  print $0
                  }
