
alter table rhnpackage alter compat type numeric(1) ;

alter table rhnpackage drop constraint rhn_package_compat_check;
alter table rhnpackage add constraint rhn_package_compat_check CHECK (compat in ( 1 , 0 ));

