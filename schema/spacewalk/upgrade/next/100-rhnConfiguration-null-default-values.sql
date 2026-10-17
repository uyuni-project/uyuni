-- boolean flags use 'true' and 'false' like the values written by Java
UPDATE rhnConfiguration
   SET default_value = CASE default_value WHEN '1' THEN 'true' ELSE 'false' END
 WHERE key LIKE 'PSW_CHECK_%_FLAG'
   AND default_value IN ('0', '1');

UPDATE rhnConfiguration
   SET value = CASE value WHEN '1' THEN 'true' ELSE 'false' END
 WHERE key LIKE 'PSW_CHECK_%_FLAG'
   AND value IN ('0', '1');

-- a NULL value means the default value is used
UPDATE rhnConfiguration
   SET value = NULL
 WHERE value = default_value;
