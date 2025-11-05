import SpaRenderer from "core/spa/spa-renderer";

import { HierarchicalRow } from "components/table/HierarchicalTable";

import { AppStreamsChannelSelection } from "./ssm-appstreams-channel-selection";

type RendererProps = { channels: HierarchicalRow[] };

export const renderer = (id: string, { channels }: RendererProps) =>
  SpaRenderer.renderNavigationReact(<AppStreamsChannelSelection channels={channels} />, document.getElementById(id));
