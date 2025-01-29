insert into rhnActionType
  select 525, 'proxy_configuration.apply', 'Apply a proxy configuration to a system', 'N', 'N', 'N'
  where not exists(select 1  from rhnActionType where id = 525);