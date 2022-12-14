FROM opensuse/leap:15.4
# list of packages to installed are build and runtime dependencies from
# spacewalk-search package
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ Uyuni-Server-POOL-x86_64 && \
    zypper -n  ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_15.4/ Uyuni-Server-Master-Other-Build-Deps && \
    zypper -n install \
        jakarta-oro \
        ant \
        apache-commons-cli \
        apache-commons-codec \
        apache-commons-lang3 \
        apache-commons-logging \
        mybatis \
        apache-commons-ognl \
        javassist \
        c3p0 \
        cglib \
        javapackages-tools \
        junit \
        lucene \
        objectweb-asm \
        picocontainer \
        quartz \
        redstone-xmlrpc \
        simple-core \
        slf4j \
        systemd \
        java-devel \
        zip \
        ncat \
        log4j && \
    zypper ref -f && \ 
    zypper clean -a

