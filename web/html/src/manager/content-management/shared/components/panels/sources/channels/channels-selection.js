// @flow
import React, {useState, useEffect} from 'react';
import {Loading} from "../../../../../../../components/loading/loading";
import useChannels from "./api/use-channels";
import styles from "./channels-selection.css";
import GroupChannels from "./group-channels";
import _intersection from "lodash/intersection";
import _xor from "lodash/xor";

import type {useChannelsType} from "./api/use-channels.js"

type PropsType = {
  initialSelectedIds: Array<String>,
  onChange: Function,
}

const channelsFiltersAvailable = {
  vendors: {
    id: 'vendors',
    text: 'Vendors',
    isVisible: (c) => !c.custom,
  },
  custom: {
    id: 'custom',
    text: 'Custom',
    isVisible: (c) => c.custom && !c.isCloned,
  },
  clones: {
    id: 'clones',
    text: 'Clones',
    isVisible: (c) => c.isCloned,
  }
}

const getInitialFiltersState = () => Object.keys(channelsFiltersAvailable);

const ChannelsSelection = (props: PropsType) => {
  const [activeFilters, setActiveFilters] = useState(getInitialFiltersState());
  const [selectedBaseChannelId, setSelectedBaseChannelId] = useState(props.initialSelectedIds[0]);
  const [selectedChannelsIds, setSelectedChannelsIds] = useState(props.initialSelectedIds);
  const [search, setSearch] = useState("");
  const {isLoading, channels}: useChannelsType = useChannels();

  useEffect(() => {
    // set lead base channel as first
    const getSortedSelectedChannelsId = () => [
      selectedBaseChannelId,
      ...selectedChannelsIds.filter(cId => cId !== selectedBaseChannelId)
    ]
    !isLoading && props.onChange(getSortedSelectedChannelsId().map(cId => channels.channelsById[cId]));
  }, [selectedChannelsIds])

  if (isLoading) {
    return (
      <div className='form-group'>
        <Loading text='Loading..' />
      </div>
    )
  }

  // TODO: not filter selected base channel
  const visibleChannels = Object.values(channels.channelsById)
    .filter( c =>
      activeFilters
        .map(filterId => channelsFiltersAvailable[filterId])
        .some(filter => filter.isVisible(c))
    )
    .map(c => c.id);

  // Order all base channels by id and set the lead base channel as first
  const orderedBaseChannels = [
    ...(selectedBaseChannelId ? [channels.channelsById[selectedBaseChannelId]] : []),
    ...(
      channels.baseIds
        .map(cId => channels.channelsById[cId])
        .sort((b1, b2) => b1.id - b2.id)
        .filter(b => b.id !== selectedBaseChannelId)
    )
  ]

  const onLeadBaseChannelChange = (newBaseId) => {
    setSelectedBaseChannelId(newBaseId);
    setSearch("");
    setActiveFilters(getInitialFiltersState);
  }

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
            value={selectedBaseChannelId}
            onChange={event => onLeadBaseChannelChange(event.target.value)}
          >
            <option></option>
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
        selectedBaseChannelId &&
        <div className='form-group'>
          <label className='col-lg-3 control-label'>
            <div className="row" style={{marginBottom: "30px"}}>
              {`${t('Child Channels')} (${selectedChannelsIds.length})`}
            </div>
            <div className="row panel panel-default panel-body text-left">
              <div style={{position: "relative"}}>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Search a channel"
                  value={search}
                  onChange={event => setSearch(event.target.value)} />
                <span className={`${styles.search_icon_container} clear`}>
                <i className="fa fa-times-circle-o no-margin" title={t('Clear Menu')} />
              </span>
              </div>
              <hr/>
              {
                Object.values(channelsFiltersAvailable).map(filter =>
                  <div key={filter.id} className='checkbox'>
                    <input type='checkbox'
                           value={filter.id}
                           checked={activeFilters.includes(filter.id)}
                           id={`filter_${filter.id}`}
                           onChange={(event) => setActiveFilters(_xor(activeFilters, [event.target.value]))}
                      // disabled={isDisabled}
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
                  const selectedChannelsIdsInGroup = _intersection(
                    selectedChannelsIds,
                    [baseChannel.id, ...baseChannel.children]
                  );

                  const isSearchPresentInGroup = baseChannel.name.toLowerCase().includes(search.toLowerCase()) ||
                    baseChannel.children.some(cId => channels.channelsById[cId].name.toLowerCase().includes(search.toLowerCase()));
                  const isSameArchAsSelectedBase = baseChannel.archLabel === channels.channelsById[selectedBaseChannelId].archLabel;
                  const hasAtLeastOneSelection = selectedChannelsIdsInGroup.length > 0;

                  // We want to show the group either it's compatible with the selected channel or has at least one selection
                  const isGroupVisible = visibleChannels.includes(baseChannel.id) &&
                    (
                      (
                        isSearchPresentInGroup &&
                        isSameArchAsSelectedBase
                      ) ||
                      hasAtLeastOneSelection
                    )

                  if(!isGroupVisible) {
                    return null;
                  }

                  return (
                    <GroupChannels
                      key={`group_${baseChannel.id}`}
                      base={baseChannel}
                      search={search}
                      childChannelsId={baseChannel.children}
                      selectedChannelsIdsInGroup={selectedChannelsIdsInGroup}
                      selectedBaseChannelId={selectedBaseChannelId}
                      onChannelsToggle={channelsIds => setSelectedChannelsIds(_xor(selectedChannelsIds, channelsIds))}
                      channelsById={channels.channelsById}
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
