remove_{{remove_jmx_props.service}}_jmx_host:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-Dcom\.sun\.management\.jmxremote\.host=\S*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: grep -F -- '-Dcom.sun.management.jmxremote.host=' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_port:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-Dcom\.sun\.management\.jmxremote\.port=[0-9]*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: grep -E -- '-Dcom\.sun\.management\.jmxremote\.port=[0-9]+' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_ssl:
  cmd.run:
    - name: sed -i 's/JAVA_OPTS="\(.*\)-Dcom\.sun\.management\.jmxremote\.ssl=false\(.*\)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: grep -F -- '-Dcom.sun.management.jmxremote.ssl=false' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_auth:
  cmd.run:
    - name: sed -i 's/JAVA_OPTS="\(.*\)-Dcom\.sun\.management\.jmxremote\.authenticate=false\(.*\)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: grep -F -- '-Dcom.sun.management.jmxremote.authenticate=false' {{remove_jmx_props.file}}

remove_{{remove_jmx_props.service}}_jmx_hostname:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-Djava\.rmi\.server\.hostname=\S*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_jmx_props.file}}
    - onlyif: grep -F -- '-Djava.rmi.server.hostname=' {{remove_jmx_props.file}}
