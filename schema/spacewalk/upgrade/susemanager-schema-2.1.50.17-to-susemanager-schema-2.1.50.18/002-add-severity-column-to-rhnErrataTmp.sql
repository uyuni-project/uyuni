--
-- Adding the missing columnt 'severity_id' to rhnErrataTmp
-- table to be consistent with rhnErrata table

ALTER TABLE rhnerratatmp
   ADD severity_id NUMERIC;
