
update rhnPackage
   set summary = rtrim(summary, chr(10))
 where summary <> rtrim(summary, chr(10));
