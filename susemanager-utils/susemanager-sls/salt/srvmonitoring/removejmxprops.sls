remove_{{remove_jmx_props.service}}_jmx:
  cmd.run:
    - name: rm /etc/sysconfig/{{remove_jmx_props.service}}/systemd/jmx.conf
    - onlyif: test -f /etc/sysconfig/{{remove_jmx_props.service}}/systemd/jmx.conf

# Workaround for previous tomcat configuration
{% if remove_jmx_props.service == 'tomcat' %}
remove_{{remove_jmx_props.service}}_previous:
  cmd.run:
    - name: mv /etc/sysconfig/tomcat /etc/sysconfig/tomcat.bak
    - onlyif: test -f /etc/sysconfig/tomcat
{% endif %}

# Now that all jmx conf are inside /etc/sysconfig/{{remove_jmx_props.service}}/systemd/jmx.conf
# all these actions might be deleted. Keeping them for a while for taskomatic backward compatibility

remove_{{remove_jmx_props.service}}_jmx_host:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-Dcom\.sun\.management\.jmxremote\.host=\S*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: test -f {{remove_jmx_props.file}} && grep -F -- '-Dcom.sun.management.jmxremote.host=' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_port:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-Dcom\.sun\.management\.jmxremote\.port=[0-9]*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: test -f {{remove_jmx_props.file}} && grep -E -- '-Dcom\.sun\.management\.jmxremote\.port=[0-9]+' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_ssl:
  cmd.run:
    - name: sed -i 's/JAVA_OPTS="\(.*\)-Dcom\.sun\.management\.jmxremote\.ssl=false\(.*\)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: test -f {{remove_jmx_props.file}} && grep -F -- '-Dcom.sun.management.jmxremote.ssl=false' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_auth:
  cmd.run:
    - name: sed -i 's/JAVA_OPTS="\(.*\)-Dcom\.sun\.management\.jmxremote\.authenticate=false\(.*\)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: test -f {{remove_jmx_props.file}} && grep -F -- '-Dcom.sun.management.jmxremote.authenticate=false' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_hostname:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-Djava\.rmi\.server\.hostname=\S*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: test -f {{remove_jmx_props.file}} && grep -F -- '-Djava.rmi.server.hostname=' {{remove_jmx_props.file}}
