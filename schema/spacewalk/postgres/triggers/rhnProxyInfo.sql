--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- triggers for suseImageInfo

CREATE OR REPLACE FUNCTION rhn_proxy_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
    new.modified := current_timestamp;
    return new;
END;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER rhn_proxy_info_mod_trig BEFORE INSERT OR UPDATE ON rhnProxyInfo
    FOR EACH ROW EXECUTE PROCEDURE rhn_proxy_info_mod_trig_fun();
