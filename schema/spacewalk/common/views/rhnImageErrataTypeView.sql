--
-- Copyright (c) 2017 SUSE LLC
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

CREATE OR REPLACE VIEW rhnImageErrataTypeView
(
    	image_id,
	errata_id,
	errata_type
)
AS
SELECT
    	INEC.image_id,
	INEC.errata_id,
	E.advisory_type
FROM    rhnErrata E,
    	rhnImageNeededErrataCache INEC
WHERE   E.id = INEC.errata_id
GROUP BY INEC.image_id, INEC.errata_id, E.advisory_type
;

