# Edit deb822 file such as ubuntu.sources
# Example: awk -f edit-ubuntu-sources.awk \
#              -v action=disable \
#              -v distro=noble \
#              -v repo=universe \
#              ubuntu.sources

BEGIN             { suites = ""
                    components = ""
                  }

/^Suites: */      { suites = $0
                    sub(/^Suites: */, "", suites)
                  }

/^Components: */  { components = $0
                    sub(/^Components: */, "", components)
                    if (match(suites " ", distro " ") != 0)
                    { if (action == "enable")
                      { if (match(components " ", repo " ") == 0)
                          components = components " " repo
                      }
                      if (action == "disable")
                      { sub(repo, "", components)
                        sub(/  */, " ", components)
                      }
                    }
                    print "Components: " components
                  }

!/^Components: */ { print $0
                  }
