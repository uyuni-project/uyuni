--
-- Copyright (c) 2017 Red Hat, Inc.
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


CREATE TABLE rhnContentSourceSsl
(
	content_source_id NUMERIC not null
		constraint rhn_csssl_csid_fk references rhnContentSource(id) on delete cascade,
	ssl_ca_cert_id NUMERIC not null
		constraint rhn_csssl_cacertid_fk references rhnCryptoKey(id) on delete cascade,
	ssl_client_cert_id NUMERIC
		constraint rhn_csssl_clcertid_fk references rhnCryptoKey(id) on delete cascade,
	ssl_client_key_id NUMERIC
		constraint rhn_csssl_clkeyid_fk references rhnCryptoKey(id) on delete cascade,
	constraint rhn_csssl_client_chk check(ssl_client_key_id is null or ssl_client_cert_id is not null),
	created TIMESTAMPTZ default(current_timestamp) not null,
	modified TIMESTAMPTZ default(current_timestamp) not null
)

;

CREATE INDEX rhn_csssl_cs
    ON rhnContentSourceSsl (content_source_id)
    ;
