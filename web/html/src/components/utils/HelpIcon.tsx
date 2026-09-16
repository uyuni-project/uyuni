/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

type Props = {
  /** Title of the icon */
  text?: string | null;
};

/** Display help icon with a title */
const HelpIcon = ({ text }: Props) => {
  return text ? <i className="fa fa-question-circle spacewalk-help-link" title={text}></i> : null;
};

export default HelpIcon;
