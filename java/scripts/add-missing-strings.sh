#! /bin/bash

for id in $(grep "trans-unit id=" /cvs/GALAXY/uyuni-fork/branding/java/code/src/com/redhat/rhn/branding/strings/StringResource_en_US.xml | sed 's/.*trans-unit id="\(.\+\)".*/\1/g'); do
	if ! grep -r "$id" /cvs/GALAXY/uyuni-fork/java/code/src/com/redhat/rhn/frontend/strings/ > /dev/null; then
		if ! grep -r "$id" /cvs/GALAXY/uyuni-fork/java/code/webapp/ > /dev/null; then
			echo "JSP: $id"
#			cat  /cvs/GALAXY/uyuni-fork/branding/java/code/src/com/redhat/rhn/branding/strings/StringResource_en_US.xml | awk -v id="$id" '
#  /^ *<\/trans-unit>/ {
#    if ( collect )
#      print
#    collect=0
#    next
#  }
#  /^ *<trans-unit / {
#    l=$0
#    sub( ".* id=\"", "", l )
#    sub( "\".*", "", l )
#    if ( l == id ) {
#      collect=1
#      print
#    }
#    next
#  }
#  (collect) {
#    print
#  }
#'
		else
			echo "JAVA: $id"
		fi
	fi
done
