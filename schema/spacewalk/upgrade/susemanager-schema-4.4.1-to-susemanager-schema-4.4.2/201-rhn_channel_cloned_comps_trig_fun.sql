create or replace function rhn_channel_cloned_comps_trig_fun() returns trigger
as
$$
begin
	new.modified := current_timestamp;

	if tg_op = 'INSERT' then
		-- if there are not comps in the cloned channel by now,
		-- we shall clone comps from the original channel
		insert into rhnChannelComps
			( id, channel_id, comps_type_id, relative_filename,
				last_modified, created, modified )
		select nextval('rhn_channelcomps_id_seq'), new.id, comps_type_id, relative_filename,
				last_modified, current_timestamp, current_timestamp
		from rhnChannelComps
		where channel_id = new.original_id
			and not exists (
				select 1
				from rhnChannelComps x
				where x.channel_id = new.id
			);
	end if;
        return new;
end;
$$
language plpgsql;

drop trigger if exists rhn_channel_cloned_comps_trig on rhnChannelCloned;

create trigger rhn_channel_cloned_comps_trig
before insert or update on rhnChannelCloned
for each row
execute procedure rhn_channel_cloned_comps_trig_fun();
