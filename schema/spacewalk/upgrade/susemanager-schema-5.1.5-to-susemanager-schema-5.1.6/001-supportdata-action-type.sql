insert into rhnActionType
SELECT 525, 'supportdata.get', 'Get supportdata from a system', 'N', 'N', 'N'
WHERE NOT EXISTS (SELECT 1
                  FROM rhnActionType
                  WHERE id = 525);
