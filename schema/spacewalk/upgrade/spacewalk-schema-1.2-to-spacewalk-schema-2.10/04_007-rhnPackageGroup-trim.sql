
-- relink reference to group with trailing \n to correct one
update rhnpackage set package_group = (select n.id
                                         from rhnpackagegroup n
                                         join rhnpackagegroup o
                                           on substr(o.name, 1, length(o.name)-1) = n.name
                                          and o.id <> n.id
                                        where o.id = package_group)
                where package_group in (select o.id
                                          from rhnpackagegroup n
                                          join rhnpackagegroup o
                                            on substr(o.name, 1, length(o.name)-1) = n.name
                                           and o.id <> n.id);

update rhnpackagesource set package_group = (select n.id
                                         from rhnpackagegroup n
                                         join rhnpackagegroup o
                                           on substr(o.name, 1, length(o.name)-1) = n.name
                                          and o.id <> n.id
                                        where o.id = package_group)
                where package_group in (select o.id
                                          from rhnpackagegroup n
                                          join rhnpackagegroup o
                                            on substr(o.name, 1, length(o.name)-1) = n.name
                                           and o.id <> n.id);

-- delete unused groups
delete from rhnpackagegroup pg
      where not exists (select 1 from rhnpackage p where p.package_group = pg.id)
        and not exists (select 1 from rhnpackagesource p where p.package_group = pg.id)
        -- don't remove 'NoGroup'
        and id > 1;

-- remove trailing \n
update rhnpackagegroup set name = substr(name, 1, length(name)-1)
                     where name like '%' || chr(10);

-- relink reference to group with trailing space to correct one (without space)
update rhnpackage set package_group = (select n.id
                                         from rhnpackagegroup n
                                         join rhnpackagegroup o
                                           on trim(o.name) = n.name
                                          and o.id <> n.id
                                        where o.id = package_group)
                where package_group in (select o.id
                                          from rhnpackagegroup n
                                          join rhnpackagegroup o
                                            on trim(o.name) = n.name
                                           and o.id <> n.id);

update rhnPackageSource set package_group = (select n.id
                                         from rhnPackageGroup n
                                         join rhnPackageGroup o
                                           on trim(o.name) = n.name
                                          and o.id <> n.id
                                        where o.id = package_group)
                where package_group in (select o.id
                                          from rhnPackageGroup n
                                          join rhnPackageGroup o
                                            on trim(o.name) = n.name
                                           and o.id <> n.id);

-- delete unused groups
delete from rhnpackagegroup pg
      where not exists (select 1 from rhnpackage p where p.package_group = pg.id)
        and not exists (select 1 from rhnpackagesource p where p.package_group = pg.id)
        -- don't remove 'NoGroup'
        and id > 1;

-- remove trailing spaces from rest of packages
update rhnpackagegroup set name = trim(name)
                     where name like '% ';
