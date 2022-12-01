FROM opensuse/leap:15.4
# list of packages to installed are build and runtime dependencies from
# spacewalk-taskomatic (spacewalk-java) package
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ Uyuni-Server-POOL-x86_64 && \
    zypper -n  ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_15.4/ Uyuni-Server-Master-Other-Build-Deps && \
    zypper ref -f && \ 
    zypper -n install \
        apache-commons-compress \
        # apache-commons-discovery \
        apache-commons-fileupload \
        apache-commons-validator \
        ehcache \
        ant \
        ant-apache-regexp \
        ant-contrib \
        ant-junit \
        antlr \
        apache-commons-cli \
        apache-commons-codec \
        apache-commons-collections \
        apache-commons-el \
        apache-commons-io \
        apache-commons-jexl \
        apache-commons-lang3 \
        apache-commons-logging \
        bcel \
        byte-buddy \
        c3p0 \
        cglib \
        concurrent \
        dom4j \
        dwr \
        glassfish-jaxb-runtime \
        glassfish-jaxb-txw2 \
        google-gson \
        hibernate-commons-annotations \
        hibernate-types \
        httpcomponents-asyncclient \
        httpcomponents-client \
        ical4j \
        istack-commons-runtime \
        jade4j \
        java-11-openjdk-devel \
        java-saml \
        javamail \
        javapackages-tools \
        javassist \
        jboss-logging \
        jcommon \
        jdom \
        joda-time \
        jose4j \
        jpa-api \
        jsch \
        jta \
        libxml2 \
        log4j \
        log4j-slf4j \
        netty \
        objectweb-asm \
        perl \
        pgjdbc-ng \
        postgresql-jdbc \
        prometheus-client-java \
        quartz \
        redstone-xmlrpc \
        salt-netapi-client \
        simple-core \
        simple-xml \
        sitemesh \
        snakeyaml \
        spark-core \
        spark-template-jade \
        statistics \
        stringtree-json \
        struts \
        tomcat \
        tomcat-lib \
        tomcat-taglibs-standard \
        uyuni-base-server \
        woodstox \
        xmlsec \
        # jakarta-activation \
        # jaxb-api \
        velocity-engine-core \
        # hibernate-c3p0 \
        # hibernate-core \
        # hibernate-ehcache \
        ant-nodeps \
        classmate \
        ncat \
        libxml2-tools && \
    zypper clean -a

