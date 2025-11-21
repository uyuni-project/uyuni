--
-- Copyright (c) 2023 SUSE LLC
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


CREATE TABLE suseVEXAnnotations
(
    cve_id               NUMERIC NOT NULL
                            REFERENCES rhnCve (id),
    platform_id          NUMERIC NOT NULL
                            REFERENCES suseOVALPlatform (id),
    package_name    NUMERIC NOT NULL
                            REFERENCES rhnPackageName (id),
    fix_version          evr_t,
    vex_status          VARCHAR(32) NOT NULL
                            CHECK (vex_status IN (
                                'AFFECTED',
                                'NOT_AFFECTED',
                                'PATCHED',
                                'UNDER_INVESTIGATION'
                                --'AFFECTED_PATCH_INAPPLICABLE',
                                --'AFFECTED_PATCH_APPLICABLE',
                                --'AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT',
                                --'AFFECTED_PATCH_UNAVAILABLE',
                                --'AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI',
                                --'AFFECTED_PARTIAL_PATCH_APPLICABLE'
                            )),
    
    
    CONSTRAINT suse_vex_annotations_id_pk PRIMARY KEY (platform_id, cve_id, package_name)
);