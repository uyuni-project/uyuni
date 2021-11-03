spacewalk-cobbler-setup
=======================

Synopsis
########

spacewalk-cobbler-setup [-h] [-c cobbler-config-dir] [-a apache2-config-dir]

Description
###########

A script to do Cobbler related configuration in the context of the Uyuni Project. This is only thought for inital use
and not for maintenance. It's intended use is the main ``spacewalk-setup`` script not an end user.

Options
#######

--help

--cobbler-config-directory cobbler-config-dir

--apache2-config-directory apache2-config-dir

Examples
########

Uses the defaults to configure the Cobbler Server:

.. code::

    spacewalk-cobbler-setup

Uses a different Cobbler configuration directory:

.. code::


    spacewalk-cobbler-setup -c /etc/different/cobbler/directory/

Uses a different Apache2 configuration directory:

.. code::

    spacewalk-cobbler-setup -a /etc/httpd/conf.d/
