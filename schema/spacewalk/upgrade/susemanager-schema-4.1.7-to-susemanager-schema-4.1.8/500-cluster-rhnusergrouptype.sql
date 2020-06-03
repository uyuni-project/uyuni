insert into rhnUserGroupType (id, label, name) (
	select sequence_nextval('rhn_usergroup_type_seq'),
	'cluster_admin',
	'Cluster Administrator'
	from dual
	where not exists (select 1 from rhnUserGroupType where label = 'cluster_admin')
);

-- create cluster_admin role to existing orgs
insert into rhnUserGroup (id, name, description, max_members, group_type, org_id)
   (select sequence_nextval('rhn_user_group_id_seq'), 'Cluster Administrators',
          'Cluster Administrators for Org ' || wc.name, NULL,
          (select id from rhnUserGroupType where label = 'cluster_admin'), wc.id
   from web_customer wc,
        (select distinct ug.org_id
         from rhnUserGroup ug
         where ug.org_id not in (select org_id
                                 from rhnUserGroup xug
                                 join rhnUserGroupType xugt ON xugt.id = xug.group_type
                                 where xugt.label = 'cluster_admin')) X
  where X.org_id = wc.id);
