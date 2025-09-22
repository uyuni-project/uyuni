import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { SsmCounter } from "./ssm-counter";

interface RendererProps {
  count?: number;
}

export const renderer = (id: string, { count }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SsmCounter count={count} />, document.getElementById(id));
