--
--update pg_setting
update pg_settings set setting = 'rhn_channel,' || setting where name = 'search_path';

    -- Converts server channel_family to use a flex entitlement
    create or replace function convert_to_fve(server_id_in IN NUMERIC, channel_family_id_val IN NUMERIC)
    returns void
    as $$
    declare
        available_fve_subs      NUMERIC;
        server_org_id_val       NUMERIC;
    BEGIN

        --
        -- Use the org_id of the server only if the org_id of the channel = NULL.
        -- This is required for subscribing to shared channels.
        --
        SELECT org_id
          INTO server_org_id_val
          FROM rhnServer
         WHERE id = server_id_in;


        perform rhn_channel.obtain_read_lock(channel_family_id_val, server_org_id_val);
        if not found then
                perform rhn_exception.raise_exception('channel_family_no_subscriptions');
        end if;
        IF (rhn_channel.can_convert_to_fve(server_id_in, channel_family_id_val ) = 0)
            THEN
                perform rhn_exception.raise_exception('server_cannot_convert_to_flex');
        END IF;

        available_fve_subs := rhn_channel.available_fve_family_subs(channel_family_id_val, server_org_id_val);

        IF (available_fve_subs > 0)
        THEN

            insert into rhnServerHistory (id,server_id,summary,details) (
                select  nextval('rhn_event_id_seq'),
                        server_id_in,
                        'converted to flex entitlement' || SUBSTR(cf.label, 0, 99),
                        cf.label
                from    rhnChannelFamily cf
                where   cf.id = channel_family_id_val
            );

            UPDATE rhnServerChannel sc set is_fve = 'Y'
                           where sc.server_id = server_id_in and
                                 sc.channel_id in
                                    (select cfm.channel_id from rhnChannelFamilyMembers cfm
                                                where cfm.CHANNEL_FAMILY_ID = channel_family_id_val);

            perform rhn_channel.update_family_counts(channel_family_id_val, server_org_id_val);
        ELSE
            perform rhn_exception.raise_exception('not_enough_flex_entitlements');
        END IF;

    END$$ language plpgsql;

    drop function user_role_check_debug(channel_id_in in numeric,
                                   user_id_in in numeric,
                                   role_in in varchar,
                                   status out numeric,
                                   reason_out out varchar);

    create or replace function user_role_check_debug(channel_id_in in numeric,
                                   user_id_in in numeric,
                                   role_in in varchar)
    returns varchar
    as $$
    declare
        org_id numeric;
    begin
        org_id := rhn_user.get_org_id(user_id_in);

        -- channel might be shared
        if role_in = 'subscribe' and
           rhn_channel.shared_user_role_check(channel_id_in, user_id_in, role_in) = 1 then
            return NULL;
        end if;

        if role_in = 'manage' and
           COALESCE(rhn_channel.get_org_id(channel_id_in), -1) <> org_id then
               return 'channel_not_owned';
        end if;

        if role_in = 'subscribe' and
           rhn_channel.get_org_access(channel_id_in, org_id) = 0 then
                return 'channel_not_available';
        end if;

        -- channel admins have all roles
        if rhn_user.check_role_implied(user_id_in, 'channel_admin') = 1 then
            return NULL;
        end if;

        -- the subscribe permission is inferred
        -- UNLESS the not_globally_subscribable flag is set
        if role_in = 'subscribe'
        then
            if rhn_channel.org_channel_setting(channel_id_in,
                       org_id,
                       'not_globally_subscribable') = 0 then
                return NULL;
            end if;
        end if;

        -- all other roles (manage right now) are explicitly granted
        if rhn_channel.direct_user_role_check(channel_id_in,
                                              user_id_in, role_in) = 1 then
            return NULL;
        end if;
        return 'direct_permission';
    end$$ language plpgsql;

    -- same as above, but with 1/0 output; useful in views, etc
    create or replace function user_role_check(channel_id_in in numeric, user_id_in in numeric, role_in in varchar)
    returns numeric
    as $$
    begin
        if rhn_channel.user_role_check_debug(channel_id_in,
                                             user_id_in, role_in) is NULL then
            return 1;
        else
            return 0;
        end if;
    end$$ language plpgsql;

-- restore the original setting
update pg_settings set setting = overlay( setting placing '' from 1 for (length('rhn_channel')+1) ) where name = 'search_path';
