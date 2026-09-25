/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

type Props = {
  name?: string;
  htmlFor?: string;
  className?: string;
  required?: boolean;
};

export function Label({ required = false, ...props }: Props) {
  return (
    <label className={`control-label${props.className ? ` ${props.className}` : ""}`} htmlFor={props.htmlFor}>
      {props.name}
      {required ? <span className="required-form-field"> *</span> : null}
      {props.name || required ? ":" : null}
    </label>
  );
}
