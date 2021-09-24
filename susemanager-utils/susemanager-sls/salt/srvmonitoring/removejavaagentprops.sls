remove_{{remove_javaagent_props.service}}_javaagent:
  cmd.run:
    - name: sed -ri 's/JAVA_OPTS="(.*)-javaagent:\S*jmx_prometheus_javaagent.jar=[0-9]+:\S*(.*)"/JAVA_OPTS="\1 \2"/' {{remove_javaagent_props.file}}
    - onlyif: grep -E -- 'jmx_prometheus_javaagent.jar=[0-9]+' {{remove_javaagent_props.file}}
