--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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
--
--
--


CREATE OR REPLACE FUNCTION
lookup_snapshot_invalid_reason(label_in IN VARCHAR)
RETURNS NUMERIC
AS
$$
DECLARE
        snapshot_invalid_reason_id numeric;
BEGIN
        SELECT id
          INTO snapshot_invalid_reason_id
          FROM rhnSnapshotInvalidReason
         WHERE label = label_in;

         IF NOT FOUND THEN
		PERFORM rhn_exception.raise_exception('invalid_snapshot_invalid_reason');
         END IF;

	RETURN snapshot_invalid_reason_id;
END;
$$ LANGUAGE PLPGSQL;
