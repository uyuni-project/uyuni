insert into rhnUserGroupType (id, label, name) values (
	sequence_nextval('rhn_usergroup_type_seq'),
	'image_admin',
	'Image Administrator'
);

-- create image_admin role to existing orgs
insert into rhnUserGroup ( id, name, description, max_members, group_type, org_id )
  select sequence_nextval('rhn_user_group_id_seq'), 'Image Administrators',
         'Image Administrators for Org ' || wc.name, NULL,
         (select id from rhnUserGroupType where label = 'image_admin'), wc.id
  from web_customer wc,
       (select distinct ug.org_id
        from rhnUserGroup ug
        where ug.org_id not in (select org_id
                                from rhnUserGroup xug
                                join rhnUserGroupType xugt ON xugt.id = xug.group_type
                                where xugt.label = 'image_admin')) X
  where X.org_id = wc.id;
