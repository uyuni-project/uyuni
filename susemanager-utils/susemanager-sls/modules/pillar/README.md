Overview
========

1. In the "/etc/salt/master" add the following:

   extension_modules: /path/to/the/extension_pillar_modules

2. Copy *.py from this directory to the `extension_modules` directory.

3. Then, in the "/etc/salt/master" add the following:

   ext_pillar:
     - suma_minion: /another/path/with/the/pillar/files
