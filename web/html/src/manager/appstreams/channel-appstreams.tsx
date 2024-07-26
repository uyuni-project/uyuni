import { AppStreamModule, Channel } from "./appstreams.type";
import { getStreamName } from "./utils";

interface Props {
  channel: Channel;
  streams: AppStreamModule[];
  moduleName: string;
  toEnable: Map<number, string[]>;
  toDisable: Map<number, string[]>;
  showPackages?: (string) => void;
  onToggle: (AppStreamModule) => void;
}

export const ChannelAppStreams = ({
  channel,
  streams,
  moduleName,
  showPackages,
  toEnable,
  toDisable,
  onToggle,
}: Props) => {
  // Sort stream names alphanumerically in descending order
  streams.sort((m1, m2) => getStreamName(m2).localeCompare(getStreamName(m1), "en", { numeric: true }));
  return (
    <>
      {streams.map((moduleStream, streamIdx) => {
        const stream = getStreamName(moduleStream);

        const changedModules = streams.map((s) => {
          const isChanged = s.enabled
            ? toDisable.get(channel.id)?.includes(getStreamName(s))
            : toEnable.get(channel.id)?.includes(getStreamName(s));
          return isChanged ? s.name : null;
        });

        const changedStatus = changedModules.includes(moduleName);
        const enabled =
          toEnable.get(channel.id)?.includes(stream) ||
          (moduleStream.enabled && !toDisable.get(channel.id)?.includes(stream));
        return (
          <tr key={`${moduleStream.name}_${moduleStream.stream}`} className={changedStatus ? "changed" : ""}>
            {streamIdx === 0 && <td rowSpan={streams.length}>{moduleName}</td>}
            <td>
              <button className={"btn btn-link"} onClick={() => showPackages && showPackages(stream)}>
                {enabled ? <strong>{stream}</strong> : stream}
              </button>
            </td>
            <td>
              <div className="form-group">
                <input
                  id={stream + "-cbox"}
                  type="checkbox"
                  checked={enabled}
                  value={stream}
                  onChange={() => onToggle(moduleStream)}
                />
              </div>
            </td>
          </tr>
        );
      })}
    </>
  );
};
