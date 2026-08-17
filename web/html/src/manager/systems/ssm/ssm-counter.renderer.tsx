import SpaRenderer from "core/spa/spa-renderer";

import { SsmCounter } from "./ssm-counter";

type RendererProps = {
  count?: number;
};

export const renderer = (id: string, { count }: RendererProps = {}) =>
  SpaRenderer.renderGlobalReact(<SsmCounter count={count} />, document.getElementById(id));
