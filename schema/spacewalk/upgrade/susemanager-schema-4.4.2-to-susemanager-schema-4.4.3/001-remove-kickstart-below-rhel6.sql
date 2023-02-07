DELETE FROM rhnKickstartTimezone WHERE install_type in (SELECT IT.id FROM rhnKSInstallType IT WHERE IT.label in ('rhel_2.1', 'rhel_3', 'rhel_4', 'rhel_5'));

DELETE FROM rhnKSInstallType WHERE label in ('rhel_2.1', 'rhel_3', 'rhel_4', 'rhel_5');
