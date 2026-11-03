/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages/messages";

import FormulaCatalog from "./org-formula-catalog";

type RendererProps = {
  flashMessage?: ServerMessageType;
  warningMessage?: ServerMessageType;
};

export const renderer = (id: string, { flashMessage, warningMessage }: RendererProps = {}) => {
  SpaRenderer.renderNavigationReact(
    <FormulaCatalog flashMessage={flashMessage} warningMessage={warningMessage} />,
    document.getElementById(id)
  );
};
