// @flow
import React, {useEffect} from 'react';
import {Loading} from "components/loading/loading";
import type {ChannelsTreeType} from "core/channels/api/use-channels-tree-api";
import useChannelsTreeApi from "core/channels/api/use-channels-tree-api";
import styles from "./channels-selection.css";
import GroupChannels from "./group-channels";
import {useImmerReducer} from "use-immer";

import type {ActionChannelsSelectionType, FilterType, StateChannelsSelectionType} from "./channels-selection.state";
import {
  getChannelsFiltersAvailableValues,
  initialStateChannelsSelection,
  reducerChannelsSelection
} from "./channels-selection.state"
import type {UseChannelsType} from "core/channels/api/use-channels-tree-api.js"
import {getVisibleChannels, isGroupVisible, orderBaseChannels} from "./channels-selection.utils";
import useMandatoryChannelsApi from "core/channels/api/use-mandatory-channels-api";
import {getSelectedChannelsIdsInGroup} from "core/channels/utils/channels-state.utils";

type PropsType = {
  isSourcesApiLoading: boolean,
  initialSelectedIds: Array<number>,
  onChange: Function,
}

const ChannelsSelection = (props: PropsType) => {
  const {fetchChannelsTree, isChannelsTreeLoaded,channelsTree }: UseChannelsType = useChannelsTreeApi();
  const {fetchMandatoryChannelsByChannelIds, isDependencyDataLoaded, requiredChannelsResult}= useMandatoryChannelsApi();
  const [state, dispatchChannelsSelection] : [StateChannelsSelectionType, (ActionChannelsSelectionType) => void]
    = useImmerReducer(
    (draft, action) => reducerChannelsSelection(draft, action, channelsTree, requiredChannelsResult),
    initialStateChannelsSelection(props.initialSelectedIds),
  );

  const isAllApiDataLoaded = isChannelsTreeLoaded && isDependencyDataLoaded;

  useEffect(() => {
    fetchChannelsTree()
      .then((channelsTree: ChannelsTreeType) => {
        fetchMandatoryChannelsByChannelIds({channels: Object.values(channelsTree.channelsById)});
      })
  }, [])

  useEffect(() => {
    // set lead base channel as first and notify
    const sortedSelectedChannelsId = state.selectedChannelsIds
      .filter(cId => cId !== state.selectedBaseChannelId);
    sortedSelectedChannelsId.unshift(state.selectedBaseChannelId)
    isAllApiDataLoaded && props.onChange(
      sortedSelectedChannelsId
        .filter(cId => channelsTree.channelsById[cId])
        .map(cId => channelsTree.channelsById[cId])
    );
  }, [state.selectedChannelsIds])

  if (!isAllApiDataLoaded || props.isSourcesApiLoading) {
    return (
      <div className='form-group'>
        <Loading text={ props.isSourcesApiLoading ? "Adding sources..." : "Loading.."} />
      </div>
    )
  }

  const onSearch = (search: string) => dispatchChannelsSelection({
    type: "search",
    search
  });

  const visibleChannels = getVisibleChannels(channelsTree, state.activeFilters);
  // Order all base channels by id and set the lead base channel as first
  let orderedBaseChannels = orderBaseChannels(channelsTree, state.selectedBaseChannelId);

  return (
    <div>
      <div className='form-group'>
        <label className='col-lg-3 control-label'>
          {t('New Base Channel')}
        </label>
        <div className='col-lg-8'>
          <select
            name='selectedBaseChannel'
            className='form-control'
            value={state.selectedBaseChannelId}
            onChange={event => dispatchChannelsSelection({
              type: "lead_channel",
              newBaseId: parseInt(event.target.value, 10)
            })}>
            <option disabled selected value> -- select a base channel -- </option>
            {
              orderedBaseChannels.map(b => <option key={b.id} value={b.id}>{b.name}</option>)
            }
          </select>
          <span className='help-block'>
            {t("Choose the channel to be elected as the new base channel")}
          </span>
        </div>
      </div>
      {
        state.selectedBaseChannelId &&
        <div className='form-group'>
          <label className='col-lg-3 control-label'>
            <div className="row" style={{marginBottom: "30px"}}>
              {`${t('Child Channels')} (${state.selectedChannelsIds.length})`}
            </div>
            <div className="row panel panel-default panel-body text-left">
              <div style={{position: "relative"}}>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Search a channel"
                  value={state.search}
                  onChange={event => onSearch(event.target.value)} />
                <span className={`${styles.search_icon_container} clear`}>
                <i
                  onClick={() => onSearch("")}
                  className="fa fa-times-circle-o no-margin"
                  title={t('Clear Search')}
                />
              </span>
              </div>
              <hr/>
              {
                getChannelsFiltersAvailableValues().map((filter: FilterType)  =>
                  <div key={filter.id} className='checkbox'>
                    <input type='checkbox'
                           value={filter.id}
                           checked={state.activeFilters.includes(filter.id)}
                           id={`filter_${filter.id}`}
                           onChange={(event) => dispatchChannelsSelection({
                             type: "toggle_filter",
                             filter: event.target.value
                           })}
                    />
                    <label htmlFor={`filter_${filter.id}`}>
                      {filter.text}
                    </label>
                  </div>
                )
              }
            </div>
          </label>
          <div className='col-lg-8'>
            <div>
              {
                orderedBaseChannels.map(baseChannel => {
                  const selectedChannelsIdsInGroup = getSelectedChannelsIdsInGroup(state.selectedChannelsIds, baseChannel);

                  if(!isGroupVisible(
                    baseChannel,
                    channelsTree,
                    visibleChannels,
                    selectedChannelsIdsInGroup,
                    state.selectedBaseChannelId,
                    state.search)
                  ) {
                    return null;
                  }

                  const isOpen = state.openGroupsIds
                    .some(openId => openId === baseChannel.id || baseChannel.children.includes(openId));

                  return (
                    <GroupChannels
                      key={`group_${baseChannel.id}`}
                      base={baseChannel}
                      search={state.search}
                      childChannelsId={baseChannel.children}
                      selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
                      selectedBaseChannelId={state.selectedBaseChannelId}
                      isOpen={isOpen}
                      setAllRecommentedChannels={enable => {
                        dispatchChannelsSelection({
                          type: "set_recommended",
                          baseId: baseChannel.id,
                          enable
                        })
                      }}
                      onChannelToggle={channelId => dispatchChannelsSelection({
                        type: "toggle_channel",
                        baseId: baseChannel.id,
                        channelId,
                      })}
                      onOpenGroup={open => dispatchChannelsSelection({
                        type: "open_group",
                        baseId: baseChannel.id,
                        open
                      })}
                      channelsTree={channelsTree}
                      requiredChannelsResult={requiredChannelsResult}
                    />
                  )
                })
              }
            </div>
          </div>
        </div>
      }
    </div>
  );
};

export default ChannelsSelection;
