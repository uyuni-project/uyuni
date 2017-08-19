-- oracle equivalent source sha1 3e0a60267f929c070f742d21c1fb178b34decae4

delete from rhnServerGroupMembers where server_group_id in (select id from rhnServerGroup where name = 'Non-Linux Entitled Servers' and group_type is not null);

delete from rhnServerGroup where name = 'Non-Linux Entitled Servers' and group_type is not null;

delete from rhnServerGroupType where label = 'nonlinux_entitled';
