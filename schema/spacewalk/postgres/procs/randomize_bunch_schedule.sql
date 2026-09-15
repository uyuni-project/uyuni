--
-- Copyright (c) 2014 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

CREATE OR REPLACE FUNCTION
RANDOMIZE_BUNCH_SCHEDULE(label_in IN VARCHAR)
RETURNS void
AS
$$
DECLARE
        new_hour          NUMERIC;
        new_minute        NUMERIC;
BEGIN
        SELECT floor(random()*6)
          INTO new_hour
          FROM dual;

        SELECT floor(random()*59)
          INTO new_minute
          FROM dual;

        UPDATE rhnTaskoSchedule
           SET cron_expr = (select '0 ' || new_minute || ' ' || new_hour || ' ? * *' from dual)
         WHERE job_label = label_in;
END;
$$ LANGUAGE PLPGSQL;

