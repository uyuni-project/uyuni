import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { SsmCounter } from "./ssm-counter";

type RendererProps = {
  count?: number;
};

export const renderer = (parent: Element, { count }: RendererProps = {}) =>
  SpaRenderer.renderNavigationReact(<SsmCounter count={count} />, parent);
