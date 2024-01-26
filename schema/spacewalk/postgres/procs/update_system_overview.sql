--
-- Copyright (c) 2022 SUSE, LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

create or replace
function update_system_overview (
    sid in numeric
) returns void as
$$
declare
    new_id                              numeric;
    new_server_name                     varchar;
    new_created                         timestamptz;
    new_creator_name                    varchar;
    new_modified                        timestamptz;
    new_group_count                     numeric;
    new_channel_id                      numeric;
    new_channel_labels                  varchar;
    new_security_errata                 numeric;
    new_bug_errata                      numeric;
    new_enhancement_errata              numeric;
    new_outdated_packages               numeric;
    new_config_files_with_differences   numeric;
    new_last_checkin                    timestamptz;
    new_entitlement_level               VARCHAR(256);
    new_virtual_guest                   BOOLEAN;
    new_virtual_host                    BOOLEAN;
    new_proxy                           BOOLEAN;
    new_mgr_server                      BOOLEAN;
    new_selectable                      BOOLEAN;
    new_extra_pkg_count                 NUMERIC;
    new_requires_reboot                 BOOLEAN;
    new_kickstarting                    BOOLEAN;
    new_actions_count                   NUMERIC;
    new_package_actions_count           NUMERIC;
    new_unscheduled_errata_count        NUMERIC;
    new_status_type                     VARCHAR(20);
    awol                                BOOLEAN;
begin
    SELECT s.id, s.name, s.modified
    INTO
        new_id,
        new_server_name,
        new_modified
    FROM rhnServer s
    WHERE s.id = sid;

    -- The system has likely been removed after filing the update request
    IF new_id IS NULL THEN
        return;
    END IF;

    SELECT count(server_group_id)
    INTO new_group_count
    FROM rhnVisibleServerGroupMembers
    WHERE server_id = sid;

    SELECT C.id
    INTO new_channel_id
    FROM rhnChannel C,
	     rhnServerChannel SC
    WHERE SC.server_id = sid AND SC.channel_id = C.id AND C.parent_channel IS NULL;

    SELECT coalesce(C.name, '(none)')
    INTO new_channel_labels
    FROM rhnChannel C,
	     rhnServerChannel SC
    WHERE SC.server_id = sid AND SC.channel_id = C.id AND C.parent_channel IS NULL;

    SELECT count(*)
    INTO new_security_errata
    FROM rhnServerErrataTypeView setv
    WHERE setv.server_id = sid AND setv.errata_type = 'Security Advisory';

    SELECT count(*)
    INTO new_bug_errata
    FROM rhnServerErrataTypeView setv
    WHERE setv.server_id = sid AND setv.errata_type = 'Bug Fix Advisory';

    SELECT count(*)
    INTO new_enhancement_errata
    FROM rhnServerErrataTypeView setv
    WHERE setv.server_id = sid AND setv.errata_type = 'Product Enhancement Advisory';

    SELECT count(DISTINCT p.name_id)
    INTO new_outdated_packages
    FROM rhnPackage p, rhnServerNeededCache snc
    WHERE snc.server_id = sid AND p.id = snc.package_id;

    SELECT count(*)
    INTO new_config_files_with_differences
    FROM rhnActionConfigRevision ACR
    INNER JOIN rhnActionConfigRevisionResult ACRR on ACR.id = ACRR.action_config_revision_id
    WHERE ACR.server_id = sid
      AND ACR.action_id = (
           SELECT MAX(rA.id)
             FROM rhnAction rA
                  INNER JOIN rhnServerAction rSA ON rSA.action_id = rA.id
                  INNER JOIN rhnActionStatus rAS ON rAS.id = rSA.status
                  INNER JOIN rhnActionType rAT ON rAT.id = rA.action_type
            WHERE rSA.server_id = sid
              AND rAS.name in ('Completed', 'Failed')
              AND rAT.label = 'configfiles.diff'
      )
      AND ACR.failure_id is null
      AND ACRR.result is not null;

    SELECT TO_CHAR(checkin, 'YYYY-MM-DD HH24:MI:SS')
    INTO new_last_checkin
    FROM rhnServerInfo WHERE server_id = sid;

    SELECT date_diff_in_days(CAST(new_last_checkin AS TIMESTAMP), NOW()) > C.threshold INTO awol
    FROM (SELECT CAST(coalesce(value, default_value) AS INTEGER) AS threshold
            FROM rhnconfiguration WHERE key = 'system_checkin_threshold') C;

    select created,
           (SELECT wc.login FROM web_contact wc WHERE wc.id = s.creator_id) as creator_name
    into new_created, new_creator_name from rhnServer as S where S.id = sid;

    select TRUE into new_virtual_guest from rhnVirtualInstance where virtual_system_id = sid;

    select TRUE into new_virtual_host
    from rhnServerGroup sg
        INNER JOIN rhnServerGroupMembers sgm ON sg.id = sgm.server_group_id
            INNER JOIN rhnServerGroupType sgt ON sgt.id = sg.group_type
            where
                sgm.server_id = sid and
                    sgt.label='virtualization_host'
    union
    select TRUE as virtual_host
        from rhnVirtualInstance VI
            where VI.host_system_id = sid;

    select TRUE into new_proxy FROM rhnProxyInfo PI WHERE PI.server_id = sid;

    select TRUE into new_mgr_server FROM suseMgrServerInfo SI WHERE SI.server_id = sid;

    SELECT TRUE into new_selectable
    FROM rhnServerFeaturesView SFV
    WHERE SFV.server_id = sid AND SFV.label = 'ftr_system_grouping';

    SELECT string_agg(ordered.label, ',') INTO new_entitlement_level
    FROM (
        SELECT label
        FROM rhnServerEntitlementView AS SEV
        WHERE SEV.server_id = sid
        ORDER BY CASE SEV.is_base WHEN 'Y' THEN 1 WHEN 'N' THEN 2 END, SEV.label
    ) AS ordered;

    SELECT count(sp.name_id) AS extra_pkg_count
    INTO new_extra_pkg_count
    FROM rhnServerPackage sp
    LEFT OUTER JOIN (SELECT sc.server_id,
                            cp.package_id,
                            p.name_id,
                            p.evr_id,
                            p.package_arch_id
                     FROM rhnPackage p,
                          rhnServerChannel sc,
                          rhnServerPackage sp2,
                          rhnChannelPackage cp,
                          rhnUserServerPerms usp2
                     WHERE cp.package_id = p.id
                       AND cp.channel_id = sc.channel_id
                       AND sc.server_id = usp2.server_id
                       AND sc.server_id = sp2.server_id
                       AND sp2.server_id = sid
                       AND sp2.name_id = p.name_id
                       AND sp2.evr_id = p.evr_id
                       AND sp2.package_arch_id = p.package_arch_id
                     ) scp ON (scp.server_id = sp.server_id AND
                               sp.name_id = scp.name_id AND
                               sp.evr_id = scp.evr_id AND
                               sp.package_arch_id = scp.package_arch_id)
  WHERE scp.package_id IS NULL AND sp.server_id = sid
  GROUP BY sp.server_id;

  SELECT TRUE into new_requires_reboot
  FROM rhnServer S
  WHERE
    S.id = sid
    AND (EXISTS (SELECT 1
                FROM rhnServerPackage SP
                  JOIN rhnPackage P ON (P.evr_id = SP.evr_id AND P.name_id = SP.name_id)
                  JOIN rhnErrataPackage EP ON EP.package_id = P.id
                  JOIN rhnErrata E ON EP.errata_id = E.id
                  JOIN rhnerratakeyword EK ON E.id = EK.errata_id
                WHERE SP.server_id = S.id
                  AND EK.keyword = 'reboot_suggested'
                  AND (to_date('1970-01-01', 'YYYY-MM-DD')
                       + numtodsinterval(S.last_boot, 'second')) < SP.installtime at time zone 'UTC'
           )
         OR EXISTS
               (SELECT 1
                FROM rhnServerPackage SP
                  JOIN rhnPackage P ON (P.evr_id = SP.evr_id AND P.name_id = SP.name_id)
                  JOIN rhnPackageProvides PP ON P.id = PP.package_id
                  JOIN rhnPackageCapability PC ON PP.capability_id = PC.id
                WHERE SP.server_id = S.id
                  AND PC.name = 'installhint(reboot-needed)'
                  AND (to_date('1970-01-01', 'YYYY-MM-DD')
                       + numtodsinterval(S.last_boot, 'second')) < SP.installtime at time zone 'UTC'
                )
         OR EXISTS
               (SELECT 1
                FROM suseMinionInfo smi
                WHERE smi.server_id = S.id
                AND to_date('1970-01-01', 'YYYY-MM-DD')
                       + numtodsinterval(S.last_boot, 'second') < smi.reboot_required_after at time zone 'UTC'
               )
    );

    SELECT TRUE into new_kickstarting
    FROM rhnKickstartSession KSS, rhnKickstartSessionState KSSS
    WHERE (KSS.old_server_id = sid OR KSS.new_server_id = sid)
        AND KSSS.id = KSS.state_id
        AND KSSS.label NOT IN ('complete', 'failed');

    SELECT count(distinct SA.action_id) INTO new_actions_count
    FROM rhnServerAction SA, rhnActionStatus AST
    WHERE SA.server_id = sid
        AND AST.id = SA.status
        AND AST.name = 'Queued';

    SELECT count(A.id) INTO new_package_actions_count
    FROM rhnServerAction SA, rhnActionStatus AST, rhnActionType AT, rhnAction A
    WHERE SA.server_id = sid
        AND AST.id = SA.status
        AND AST.name = 'Queued'
        AND A.id = SA.action_id
        AND AT.id = A.action_type
        AND AT.label IN('packages.refresh_list', 'packages.update',
                       'packages.remove', 'errata.update', 'packages.delta');

    SELECT COUNT(DISTINCT E.id) INTO new_unscheduled_errata_count
    FROM rhnErrata E, rhnServerNeededErrataCache SNPC
    WHERE SNPC.server_id = sid
        AND SNPC.errata_id = E.id
        AND NOT EXISTS (SELECT SA.server_id
                        FROM rhnActionErrataUpdate AEU,
                           rhnServerAction SA,
                           rhnActionStatus AST
                        WHERE SA.server_id = sid
                            AND SA.status = AST.id
                            AND AST.name IN('Queued', 'Picked Up')
                            AND AEU.action_id = SA.action_id
                            AND AEU.errata_id = E.id);

    if new_entitlement_level = '' then
        new_status_type = 'unentitled';
    elsif awol then
        new_status_type = 'awol';
    elsif coalesce(new_kickstarting, FALSE) then
        new_status_type = 'kickstarting';
    elsif new_requires_reboot then
        new_status_type = 'reboot needed';
    elsif new_enhancement_errata + new_bug_errata + new_security_errata > 0 and new_unscheduled_errata_count = 0 then
        new_status_type = 'updates scheduled';
    elsif new_actions_count > 0 then
        new_status_type = 'actions scheduled';
    elsif new_enhancement_errata + new_bug_errata + new_security_errata + new_outdated_packages + new_package_actions_count = 0 then
        new_status_type = 'up2date';
    elsif new_security_errata > 0 then
        new_status_type = 'critical';
    elsif new_outdated_packages > 0 then
        new_status_type = 'updates';
    else
        new_status_type = null;
    end if;

    insert into suseSystemOverview (
        id,
        server_name,
        created,
        creator_name,
        modified,
        group_count,
        channel_id,
        channel_labels,
        security_errata,
        bug_errata,
        enhancement_errata,
        outdated_packages,
        config_files_with_differences,
        last_checkin,
        entitlement_level,
        virtual_guest,
        virtual_host,
        proxy,
        mgr_server,
        selectable,
        extra_pkg_count,
        requires_reboot,
        kickstarting,
        actions_count,
        package_actions_count,
        unscheduled_errata_count,
        status_type
    ) values (
        new_id,
        new_server_name,
        new_created,
        new_creator_name,
        new_modified,
        new_group_count,
        new_channel_id,
        new_channel_labels,
        new_security_errata,
        new_bug_errata,
        new_enhancement_errata,
        new_outdated_packages,
        new_config_files_with_differences,
        new_last_checkin,
        new_entitlement_level,
        coalesce(new_virtual_guest, FALSE),
        coalesce(new_virtual_host, FALSE),
        coalesce(new_proxy, FALSE),
        coalesce(new_mgr_server, FALSE),
        coalesce(new_selectable, FALSE),
        new_extra_pkg_count,
        coalesce(new_requires_reboot, FALSE),
        coalesce(new_kickstarting, FALSE),
        new_actions_count,
        new_package_actions_count,
        new_unscheduled_errata_count,
        new_status_type
    ) on conflict (id)
    do update set
        server_name = EXCLUDED.server_name,
        created = EXCLUDED.created,
        creator_name = EXCLUDED.creator_name,
        modified = EXCLUDED.modified,
        group_count = EXCLUDED.group_count,
        channel_id = EXCLUDED.channel_id,
        channel_labels = EXCLUDED.channel_labels,
        security_errata = EXCLUDED.security_errata,
        bug_errata = EXCLUDED.bug_errata,
        enhancement_errata = EXCLUDED.enhancement_errata,
        outdated_packages = EXCLUDED.outdated_packages,
        config_files_with_differences = EXCLUDED.config_files_with_differences,
        last_checkin = EXCLUDED.last_checkin,
        entitlement_level = EXCLUDED.entitlement_level,
        virtual_guest = EXCLUDED.virtual_guest,
        virtual_host = EXCLUDED.virtual_host,
        proxy = EXCLUDED.proxy,
        mgr_server = EXCLUDED.mgr_server,
        selectable = EXCLUDED.selectable,
        extra_pkg_count = EXCLUDED.extra_pkg_count,
        requires_reboot = EXCLUDED.requires_reboot,
        kickstarting = EXCLUDED.kickstarting,
        actions_count = EXCLUDED.actions_count,
        package_actions_count = EXCLUDED.package_actions_count,
        unscheduled_errata_count = EXCLUDED.unscheduled_errata_count,
        status_type = EXCLUDED.status_type;
end;
$$
language plpgsql;
