/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { ReactNode } from "react";

import { IconTag } from "components/icontag";
import { HelpLink } from "components/utils/HelpLink";

type Props = {
  helpUrl?: string;
  button?: ReactNode;
  title: string;
  icon?: string;
  iconType?: string;
  children?: ReactNode;
};

export function TopPanel(props: Props) {
  const help = props.helpUrl ? <HelpLink url={props.helpUrl} /> : null;

  return (
    <>
      <div className="spacewalk-toolbar-h1">
        <div className="pull-right btn-group">{props.button}</div>
        <h1>
          {props.iconType ? <IconTag type={props.iconType} /> : props.icon && <i className={`fa ${props.icon}`} />}
          {t(props.title)}
          &nbsp;
          {help}
        </h1>
      </div>
      {props.children}
    </>
  );
}
