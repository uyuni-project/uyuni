-- oracle equivalent source sha1 9fd256d697b3b403808a985d45a8966537c8eb1a
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

CREATE UNIQUE INDEX rhn_set_user_label_elem_unq
    ON rhnSet (user_id, label, element)
    WHERE element_two IS NULL AND element_three IS NULL;

CREATE UNIQUE INDEX rhn_set_user_label_elem_elem2_unq
    ON rhnSet (user_id, label, element, element_two)
    WHERE element_two IS NOT NULL AND element_three IS NULL;

CREATE UNIQUE INDEX rhn_set_user_label_elem_elem3_unq
    ON rhnSet (user_id, label, element, element_three)
    WHERE element_two IS NULL AND element_three IS NOT NULL;

CREATE UNIQUE INDEX rhn_set_user_label_elem_elem2_elem3_unq
    ON rhnSet (user_id, label, element, element_two, element_three)
    WHERE element_two IS NOT NULL AND element_three IS NOT NULL;

