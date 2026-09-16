/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

declare module "*.css";
declare module "*.scss";
declare module "*.scss?lazy" {
  const stylesheet: {
    use(): void;
    unuse(): void;
  };
  export default stylesheet;
}
declare module "*.svg";
declare module "*.png";
declare module "*?raw";
