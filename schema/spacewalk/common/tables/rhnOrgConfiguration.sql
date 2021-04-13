--
-- Copyright (c) 2013--2015 Red Hat, Inc.
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

create table rhnOrgConfiguration
(
    org_id                     NUMERIC not null
                                   constraint rhn_org_conf_org_id_fk
                                   references web_customer(id)
                                   on delete cascade,
    staging_content_enabled    char(1)
                                   default ('N') not null
                                   constraint rhn_org_conf_stage_content_chk
                                   check (staging_content_enabled in ('Y', 'N')),
    errata_emails_enabled      char(1)
                                    default ('Y') not null
                                    constraint rhn_org_conf_errata_emails_chk
                                    check (errata_emails_enabled in ('Y', 'N')),
    scapfile_upload_enabled    char(1)
                                   default ('N') not null
                                   constraint rhn_org_conf_scap_upload_chk
                                   check (scapfile_upload_enabled in ('Y', 'N')),
    clm_sync_patches           char(1)
                                   default ('Y') not null
                                   constraint rhn_org_conf_clm_sync_patches
                                   check (clm_sync_patches in ('Y', 'N')),
    scap_file_sizelimit        NUMERIC
                                   default(2097152) not null
                                   constraint rhn_org_conf_scap_szlmt_chk
                                   check (scap_file_sizelimit >= 0),
    scap_retention_period_days NUMERIC
                                   default(90)
                                   constraint rhn_org_conf_scap_reten_chk
                                   check (scap_retention_period_days >= 0),
    create_default_sg          char(1)
                                    default('N') not null
                                    constraint rhn_org_cong_deforg_chk
                                    check (create_default_sg in ('Y', 'N')),
    created                    TIMESTAMPTZ
                                   default (current_timestamp) not null,
    modified                   TIMESTAMPTZ
                                   default (current_timestamp) not null
)

;

create unique index rhn_org_conf_org_id
    on rhnOrgConfiguration (org_id)
    ;
