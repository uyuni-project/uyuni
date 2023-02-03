FROM registry.opensuse.org/opensuse/leap:15.4
RUN mkdir jars
# For some reason install all in one command tries to pull Java8 IBM and fails validating the license
RUN zypper ar https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_15.4/systemsmanagement:Uyuni:Master:Other.repo && \
    zypper ar https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/openSUSE_Leap_15.4/systemsmanagement:Uyuni:Master.repo && \
    zypper --gpg-auto-import-keys ref && \
    zypper -n install hibernate5-core hibernate5-c3p0 hibernate5-ehcache \
        ehcache hibernate-commons-annotations classmate postgresql-jdbc \
        log4j apache-commons-io apache-commons-lang3 jpa-api geronimo-jta-1_1-api && \
    zypper -n install httpcomponents-core ical4j hibernate-types snakeyaml google-gson struts \
        statistics salt-netapi-client redstone-xmlrpc jose4j apache-commons-compress && \
    find /usr/share/java/ -name '*.jar' -exec cp {} jars \;
COPY *.jar jars
COPY log4j2.xml .
CMD ["java", "-Dlog4j2.configurationFile=file:///log4j2.xml", "-classpath", "jars/*", "com.suse.manager.tasks.SessionCleanup"]
