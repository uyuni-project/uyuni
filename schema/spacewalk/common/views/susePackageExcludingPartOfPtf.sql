-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

--
-- Returns the packages excluding those that are part of a PTF
--

CREATE OR REPLACE VIEW susePackageExcludingPartOfPtf AS
  SELECT pkg.*
    FROM rhnpackage pkg
   WHERE pkg.is_part_of_ptf = FALSE
;
