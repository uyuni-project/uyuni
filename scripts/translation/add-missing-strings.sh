#! /bin/bash

GIT_ROOT="$( git -c alias.a='!pwd' a )"
GIT_ROOT_BRAND=$GIT_ROOT

readarray -t arr2 < <(grep "trans-unit id=" $GIT_ROOT_BRAND/branding/java/code/src/com/redhat/rhn/branding/strings/StringResource_en_US.xml | sed 's/.*trans-unit id="\(.\+\)".*/\1/g')

for id in "${arr2[@]}"; do
	if ! grep -r "$id" $GIT_ROOT/java/code/src/com/redhat/rhn/frontend/strings/ > /dev/null; then
		if ! grep -r "$id" $GIT_ROOT/java/code/webapp/ > /dev/null; then
			echo "JSP: $id"
#			cat  $GIT_ROOT_BRAND/branding/java/code/src/com/redhat/rhn/branding/strings/StringResource_en_US.xml | awk -v id="$id" '
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
