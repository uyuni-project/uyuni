/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
// Note: To use this component make sure it's used in a placed wrapped by the roles-context-provider
import { useContext } from "react";

import { RolesContext } from "./roles-context";

const useRoles = () => useContext(RolesContext);

export default useRoles;
