-- oracle equivalent source sha1 f3014e0f9f8a0b0d6eafd4064c492b1f1e6f2d7e

update rhnPackage
   set summary = rtrim(summary, chr(10))
 where summary <> rtrim(summary, chr(10));
