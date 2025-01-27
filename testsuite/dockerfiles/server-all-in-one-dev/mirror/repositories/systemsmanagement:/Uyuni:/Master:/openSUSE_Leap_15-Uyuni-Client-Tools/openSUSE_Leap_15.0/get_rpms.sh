cd noarch
wget -c https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/openSUSE_Leap_15-Uyuni-Client-Tools/openSUSE_Leap_15.0/noarch/mgr-push-5.1.1-2.1.uyuni.noarch.rpm .
wget -c https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/openSUSE_Leap_15-Uyuni-Client-Tools/openSUSE_Leap_15.0/noarch/python3-mgr-push-5.1.1-2.1.uyuni.noarch.rpm .
wget -c https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/openSUSE_Leap_15-Uyuni-Client-Tools/openSUSE_Leap_15.0/noarch/python3-rhnlib-5.1.1-3.1.uyuni.noarch.rpm .
wget -c https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/openSUSE_Leap_15-Uyuni-Client-Tools/openSUSE_Leap_15.0/noarch/python3-spacewalk-client-tools-5.1.1-4.1.uyuni.noarch.rpm .
wget -c https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/openSUSE_Leap_15-Uyuni-Client-Tools/openSUSE_Leap_15.0/noarch/spacewalk-client-tools-5.1.1-4.1.uyuni.noarch.rpm .
cd -
createrepo .
