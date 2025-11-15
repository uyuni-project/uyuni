--
-- Copyright (c) 2023 SUSE
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

CREATE TABLE susePaygDimensionResult
(
    id                NUMERIC NOT NULL
                          CONSTRAINT susePaygDimensionResult_id_pk PRIMARY KEY,
    computation_id    NUMERIC NOT NULL
                          CONSTRAINT susePaygDimensionResult_computation_fk
                              REFERENCES susePaygDimensionComputation (id),
    dimension         billing_dimension_t NOT NULL,
    count             NUMERIC NOT NULL
);

CREATE SEQUENCE susePaygDimensionResult_id_seq;
