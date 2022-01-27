import * as React from "react";
import { memo, useEffect, useState, useCallback } from "react";
import debounce from "lodash/debounce";
import xor from "lodash/xor";

import { Loading } from "components/utils/Loading";
import { Select } from "components/input/Select";
import { VirtualTree } from "components/virtual-list";
import { ProjectSoftwareSourceType } from "manager/content-management/shared/type";

import styles from "./channels-selection.css";
import { getInitialFiltersState } from "./channels-filters-types";
import BaseChannel from "./base-channel";
import ChildChannel from "./child-channel";
import RecommendedToggle from "./recommended-toggle";
import ChannelsFilters from "./channels-filters";
import { useChannelsWithMandatoryApi, useLoadSelectOptions } from "./channels-api";
import { RowType, RowDefinition } from "./channels-selection-rows";
import EmptyChild from "./empty-child";

import Worker from "./channels-selection.worker.ts";
import WorkerMessages from "./channels-selection-messages";

type PropsType = {
  isSourcesApiLoading: boolean;
  initialSelectedSources: ProjectSoftwareSourceType[];
  // For some reason, the wrapper expects labels, not channels, but that's fine by us
  onChange: (channelLabels: string[]) => void;
};

const getTreeWalker = (nodes: RowDefinition[], getNodeData: (...args: any[]) => any) => {
  return function* treeWalker() {
    // Get all root nodes
    for (let i = 0; i < nodes.length; i++) {
      yield getNodeData(nodes[i], 0, i === 0);
    }

    // Get all children
    while (true) {
      const parent = yield;

      for (let i = 0; i < parent.node.children?.length; i++) {
        yield getNodeData(parent.node.children?.[i], parent.nestingLevel + 1);
      }
    }
  };
};

const ChannelsSelection = (props: PropsType) => {
  const [isLoading, setIsLoading] = useState(true);
  const [loadSelectOptions] = useLoadSelectOptions();
  const [channelsWithMandatoryPromise] = useChannelsWithMandatoryApi();

  const [worker] = useState(new Worker());
  const [tree, setTree] = useState<RowDefinition[] | undefined>(undefined);
  const [treeWalker, setTreeWalker] = useState<(() => Generator<any>) | undefined>(undefined);
  const [selectedNodes, setSelectedNodes] = useState<Set<number>>(new Set());
  // TODO: This is obsolete
  const [selectedChannelsCount, setSelectedChannelsCount] = useState<number>(0);
  const [activeFilters, setActiveFilters] = useState<string[]>(getInitialFiltersState());
  const [search, setSearch] = useState("");

  const onSelectedBaseChannelIdChange = (channelId: number) => {
    worker.postMessage({ type: WorkerMessages.SET_SELECTED_BASE_CHANNEL_ID, selectedBaseChannelId: channelId });
    onToggleChannelSelect(channelId);
  };

  // Debounce searching so the worker is not overloaded during typing when working with large data sets
  const onSearch = useCallback(
    debounce((newSearch: string) => {
      worker.postMessage({ type: WorkerMessages.SET_SEARCH, search: newSearch });
    }, 50),
    []
  );

  const onToggleChannelSelect = (channelId: number) => {
    console.log(tree);
    if (selectedNodes.has(channelId)) {
      // TODO
    }
    // worker.postMessage({ type: WorkerMessages.TOGGLE_IS_CHANNEL_SELECTED, channelId });
  };

  const onSetRecommendedChildrenSelected = (channelId: number, selected: boolean) => {
    // worker.postMessage({ type: WorkerMessages.SET_RECOMMENDED_CHILDREN_ARE_SELECTED, channelId, selected });
  };

  const onToggleChannelOpen = (channelId: number) => {
    // worker.postMessage({ type: WorkerMessages.TOGGLE_IS_CHANNEL_OPEN, channelId });
  };

  useEffect(() => {
    // Ensure the worker knows about our initial configuration
    worker.postMessage({ type: WorkerMessages.SET_ACTIVE_FILTERS, activeFilters });

    channelsWithMandatoryPromise.then(({ channels, mandatoryChannelsMap }) => {
      if (isLoading) {
        setIsLoading(false);
      }

      worker.postMessage({
        type: WorkerMessages.SET_CHANNELS,
        channels,
        mandatoryChannelsMap,
        // These will hold no values if the initial selection is empty
        initialSelectedBaseChannelId: props.initialSelectedSources[0]?.channelId,
        initialSelectedChannelIds: props.initialSelectedSources.map((channel) => channel.channelId),
      });
    });

    worker.addEventListener("message", async ({ data }) => {
      switch (data.type) {
        case WorkerMessages.STATE_CHANGED: {
          if (!Array.isArray(data.rows)) {
            throw new RangeError("Received no valid rows");
          }
          setTree(data.rows);
          setTreeWalker(() => getTreeWalker(data.rows, getNodeData));
          // setRows(data.rows);
          // TODO: Implement
          // setSelectedChannelsCount(data.selectedChannelsCount);
          // TODO: Implement
          // props.onChange(data.selectedChannelLabels);
          return;
        }
        default:
          throw new RangeError(`Unknown message type, got ${data.type}`);
      }
    });

    // When the component unmounts, SIGKILL the worker
    return () => {
      worker.terminate();
    };
  }, []);

  // TODO: Types
  const getNodeData = (node: RowDefinition & {}, nestingLevel: number, isFirstNode?: boolean) => {
    // The tree component requires all ids to be strings
    const id = node.id.toString();
    return {
      data: {
        ...node,
        id,
        // TODO: Types
        defaultHeight: rowHeight(node.type),
        // TODO: Implement
        // true if we have a search string, or if id matches the current selected base
        isOpenByDefault: Boolean(isFirstNode || search), // This is a mandatory field
      },
      node,
      nestingLevel,
    };
  };

  const Row = ({ data, isOpen, setOpen }) => {
    const definition: RowDefinition = data;
    switch (definition.type) {
      case RowType.Parent:
        return (
          <BaseChannel
            rowDefinition={definition}
            search={search}
            isOpen={isOpen}
            isSelected={selectedNodes.has(definition.id)}
            onToggleChannelSelect={(channelId) => onToggleChannelSelect(channelId)}
            onToggleChannelOpen={() => setOpen(!isOpen)}
          />
        );
      case RowType.Child:
        return (
          <ChildChannel
            definition={definition}
            search={search}
            onToggleChannelSelect={(channelId) => onToggleChannelSelect(channelId)}
          />
        );
      case RowType.EmptyChild:
        return <EmptyChild />;
      case RowType.RecommendedToggle:
        return (
          <RecommendedToggle
            definition={definition}
            onSetRecommendedChildrenSelected={(channelId, selected) =>
              onSetRecommendedChildrenSelected(channelId, selected)
            }
          />
        );
      default:
        throw new RangeError("Incorrect channel render type in renderer");
    }
  };

  const rowHeight = (type: RowType) => {
    switch (type) {
      case RowType.Parent:
        return 30;
      case RowType.Child:
        return 25;
      case RowType.EmptyChild:
        return 25;
      case RowType.RecommendedToggle:
        return 20;
      default:
        throw new RangeError("Incorrect channel render type in height");
    }
  };

  if (isLoading || props.isSourcesApiLoading) {
    return (
      <div className="form-group">
        <Loading text={props.isSourcesApiLoading ? "Adding sources..." : "Loading.."} />
      </div>
    );
  }

  const defaultValueOption = props.initialSelectedSources[0]
    ? {
        base: props.initialSelectedSources[0],
      }
    : undefined;

  return (
    <React.Fragment>
      <div className="row">
        <Select
          loadOptions={loadSelectOptions}
          defaultValueOption={defaultValueOption}
          paginate={true}
          label={t("New Base Channel")}
          labelClass="col-md-3"
          divClass="col-md-8"
          hint={t("Choose the channel to be elected as the new base channel")}
          getOptionLabel={(option) => option.base.name}
          getOptionValue={(option) => option.base.id}
          onChange={(name, rawValue) => {
            const value = parseInt(rawValue, 10);
            if (isNaN(value)) {
              return;
            }
            onSelectedBaseChannelIdChange(value);
          }}
        />
      </div>
      {treeWalker && (
        <div className="row" style={{ display: "flex" }}>
          <label className="col-lg-3 control-label">
            <div className="row" style={{ marginBottom: "30px" }}>
              {`${t("Child Channels")} (${selectedChannelsCount})`}
            </div>
            <div className="row panel panel-default panel-body text-left">
              <div style={{ position: "relative" }}>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Search a channel"
                  value={search}
                  onChange={(event) => {
                    const newSearch = event.target.value;
                    setSearch(newSearch);
                    onSearch(newSearch);
                  }}
                />
                <span className={`${styles.search_icon_container} clear`}>
                  <i
                    onClick={() => {
                      setSearch("");
                      onSearch("");
                    }}
                    className="fa fa-times-circle-o no-margin"
                    title={t("Clear Search")}
                  />
                </span>
              </div>
              <hr />
              <ChannelsFilters
                activeFilters={activeFilters}
                onChange={(value) => {
                  const newActiveFilters = xor(activeFilters, [value]);
                  setActiveFilters(newActiveFilters);
                  worker.postMessage({ type: WorkerMessages.SET_ACTIVE_FILTERS, activeFilters: newActiveFilters });
                }}
              />
            </div>
          </label>
          <VirtualTree
            treeWalker={treeWalker}
            renderRow={Row}
            // By default, assume we have a base channel row
            estimatedRowHeight={30}
          />
          {/*
          <VirtualList
            items={rows}
            renderRow={Row}
            rowHeight={rowHeight}
            // By default, assume we have a base channel row
            estimatedRowHeight={30}
          />
          */}
        </div>
      )}
    </React.Fragment>
  );
};

// This whole view is expensive with large lists, so rerender only when we really need to
export default memo(ChannelsSelection, (prevProps, nextProps) => {
  return (
    prevProps.isSourcesApiLoading === nextProps.isSourcesApiLoading &&
    // This prop is a filtet result but memoed so this works fine
    prevProps.initialSelectedSources === nextProps.initialSelectedSources
  );
});
