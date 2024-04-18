import { AppStreamModule } from "./appstreams.type";

interface Props {
  streams: AppStreamModule[];
  moduleName: string;
  toEnable: string[];
  toDisable: string[];
  showPackages: (string) => void;
  onToggle: (AppStreamModule) => void;
}

const getStreamName = (module: AppStreamModule) => `${module.name}:${module.stream}`;

export const ChannelAppStreams = ({ streams, moduleName, showPackages, toEnable, toDisable, onToggle }: Props) => {
  // Sort stream names alphanumerically in descending order
  streams.sort((m1, m2) => getStreamName(m2).localeCompare(getStreamName(m1), "en", { numeric: true }));
  return (
    <>
      {streams.map((moduleStream, streamIdx) => {
        const stream = getStreamName(moduleStream);

        const changedModules = streams.map((s) => {
          const isChanged = s.enabled ? toDisable.includes(getStreamName(s)) : toEnable.includes(getStreamName(s));
          return isChanged ? s.name : null;
        });

        const changedStatus = changedModules.includes(moduleName);
        const enabled = toEnable.includes(stream) || (moduleStream.enabled && !toDisable.includes(stream));
        return (
          <tr key={`${moduleStream.name}_${moduleStream.stream}`} className={changedStatus ? "changed" : ""}>
            {streamIdx === 0 && <td rowSpan={streams.length}>{moduleName}</td>}
            <td>
              <button className={"btn btn-link"} onClick={() => showPackages(stream)}>
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
