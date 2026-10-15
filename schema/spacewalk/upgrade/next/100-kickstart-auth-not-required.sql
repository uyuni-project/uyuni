-- auth (authconfig) is deprecated in RHEL 8 kickstart and needs the
-- authselect-compat package, so profiles must be able to leave it out.
UPDATE rhnKickstartCommandName
   SET required = 'N'
 WHERE name = 'auth'
   AND required = 'Y';
