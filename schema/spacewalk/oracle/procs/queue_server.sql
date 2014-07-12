--
-- Copyright (c) 2008--2013 Red Hat, Inc.
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

-- the next two views are basically the same.  the first, though, has an outer join to
-- the errata stuff, in case there are packages the server needs that haven't been
-- errata'd (ie, the fringe case)

CREATE OR REPLACE PROCEDURE
queue_server(server_id_in IN NUMBER, immediate_in IN NUMBER := 1)
IS
    org_id_tmp NUMBER;
BEGIN
    IF immediate_in > 0
    THEN
          rhn_server.update_needed_cache(server_id_in);
    ELSE
          SELECT org_id INTO org_id_tmp FROM rhnServer WHERE id = server_id_in;
	  
	  INSERT
            INTO rhnTaskQueue
                 (org_id, task_name, task_data)
          VALUES (org_id_tmp, 
                  'update_server_errata_cache',
                  server_id_in);
    END IF;
END queue_server;
/
SHOW ERRORS

--
-- Revision 1.8  2004/11/09 18:16:21  pjones
-- bugzilla: none -- make this faster by using the table the second time.
--
-- Revision 1.7  2004/07/13 21:29:35  pjones
-- bugzilla: 125938 -- make queue_server handle new EP table, too
--
-- Revision 1.6  2002/05/13 22:53:38  pjones
-- cvs id/log
-- some (note enough) readability fixes
--
