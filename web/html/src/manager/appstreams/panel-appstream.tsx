import { Panel } from "components/panels/Panel";

import { AppStreamModule, Channel } from "./appstreams.type";
import { ChannelAppStreams } from "./channel-appstreams";

interface Props {
  channel: Channel;
  appStreams: Map<string, AppStreamModule[]>;
  toEnable: Map<number, string[]>;
  toDisable: Map<number, string[]>;
  onToggle: (arg: AppStreamModule) => void;
  showPackages?: (arg: string) => void;
}

export const AppStreamPanel = ({ channel, appStreams, toEnable, toDisable, onToggle, showPackages }: Props) => (
  <Panel headingLevel="h4" icon="spacewalk-icon-software-channels" title={channel.name} key={`panel-${channel.label}`}>
    <table className="table table-striped">
      <thead>
        <tr>
          <th>{t("Modules")}</th>
          <th>{t("Streams")}</th>
          <th>{t("Enabled")}</th>
        </tr>
      </thead>
      <tbody>
        {Object.keys(appStreams).map((moduleName) => (
          <ChannelAppStreams
            channel={channel}
            showPackages={showPackages}
            key={moduleName}
            streams={appStreams[moduleName]}
            moduleName={moduleName}
            toEnable={toEnable}
            toDisable={toDisable}
            onToggle={onToggle}
          />
        ))}
      </tbody>
    </table>
  </Panel>
);
