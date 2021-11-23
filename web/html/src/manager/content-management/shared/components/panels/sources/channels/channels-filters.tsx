import * as React from "react";
import { memo } from "react";
import { ActionChannelsSelectionType, channelsFiltersAvailableValues, FilterType } from "./channels-selection.state";

type Props = {
  activeFilters: string[];
  dispatchChannelsSelection: (arg0: ActionChannelsSelectionType) => void;
};

const ChannelsFilters = (props: Props) => {
  return (
    <React.Fragment>
      {channelsFiltersAvailableValues.map((filter: FilterType) => (
        <div key={filter.id} className="checkbox">
          <input
            type="checkbox"
            value={filter.id}
            checked={props.activeFilters.includes(filter.id)}
            id={`filter_${filter.id}`}
            onChange={(event) =>
              props.dispatchChannelsSelection({
                type: "toggle_filter",
                filter: event.target.value,
              })
            }
          />
          <label htmlFor={`filter_${filter.id}`}>{filter.text}</label>
        </div>
      ))}
    </React.Fragment>
  );
};

export default memo(ChannelsFilters, (prevProps, nextProps) => {
  return prevProps.activeFilters.join() === nextProps.activeFilters.join();
});
