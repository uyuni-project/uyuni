// @flow
import React, {useState, useEffect} from 'react';
import {Toggler} from "../../../../../../../components/toggler";
import {Highlight} from "components/data-handler";
import ChildChannels from "./child-channels";
import _intersection from "lodash/intersection";
import _difference from "lodash/difference";

type PropsType = {
  base: Object,
  search: String,
  childChannelsId: Array<number>,
  selectedChannelsIdsInGroup: Array<number>,
  selectedBaseChannelId: number,
  onChannelsToggle: Function,
  channelsById: Object,
}

const GroupChannels = (props: PropsType) => {
  const [collapsed, setCollapsed] = useState(true);

  const nrOfSelectedChilds = props.selectedChannelsIdsInGroup.length;
  const childChannels = props.childChannelsId.map(cId => props.channelsById[cId]);

  //If the search term is present in the group it will open it
  useEffect(() => {
    const searchTermIsPresent =
      childChannels.some(c => c.name.toLowerCase().includes(props.search.toLowerCase())) ||
      props.base.name.toLowerCase().includes(props.search.toLowerCase());
    if(props.search && searchTermIsPresent) {
      setCollapsed(false);
    } else {
      setCollapsed(true);
    }
  }, [props.search])

  //If top channel (base) is selected it will open and select all the recommended child channels
  useEffect(() => {
    if(props.selectedChannelsIdsInGroup.includes(props.base.id)) {
      setRecommentedChannels(true);
      setCollapsed(false);
    }
  }, [props.selectedChannelsIdsInGroup.includes(props.base.id)])

  //If a new base channel is selected it will automatically open and select all the recommended child channels
  useEffect(() => {
    if(props.base.id === props.selectedBaseChannelId) {
      setRecommentedChannels(true);
      setCollapsed(false);
    }
  }, [props.selectedBaseChannelId])

  const getAllRecommentedIds = (channelIds) => {
    return channelIds
      .map(cId => props.channelsById[cId])
      .filter(c => c.recommended)
      .map(c => c.id);
  }

  const recommendedChildrenIds = getAllRecommentedIds(props.childChannelsId);
  const recommendedIds = [props.base.id, ...recommendedChildrenIds];
  const recommendedIdsSelected = _intersection(recommendedIds, props.selectedChannelsIdsInGroup);

  const areRecommendedChildrenSelected = () : boolean =>
    recommendedIds.length === recommendedIdsSelected.length;

  const setRecommentedChannels = (enable) => {
    enable
      ? props.onChannelsToggle(_difference(recommendedIds, recommendedIdsSelected))
      : props.onChannelsToggle(recommendedIds.filter(cId => cId !== props.selectedBaseChannelId));
  }

  const toggleRecommended = () =>
    areRecommendedChildrenSelected()
      ? setRecommentedChannels(false)
      : setRecommentedChannels(true)

  return (
    <div className='row'>
      <h4 style={{
        backgroundColor: props.base.id === props.selectedBaseChannelId ? "lightBlue" : "",
        marginBottom: "0px"
      }} className='pointer'>
        <input type='checkbox'
               id={'base_' + props.base.id}
               name='childChannels'
               checked={props.selectedChannelsIdsInGroup.includes(props.base.id)}
               value={props.base.id}
               onChange={(event) => {
                 props.onChannelsToggle([event.target.value])
               }}
               disabled={props.base.id === props.selectedBaseChannelId}
        />
        &nbsp;
        &nbsp;
        <div
          style={{display: "inline"}}
          onClick={() => setCollapsed(!collapsed)}>
          <i className={'fa ' + (collapsed ? 'fa-angle-right' : 'fa-angle-down')} />
          &nbsp;
          <Highlight
            enabled={props.search}
            text={props.base.name}
            highlight={props.search}
          >
          </Highlight>
          {
            nrOfSelectedChilds > 0 &&
            <b>{` (${nrOfSelectedChilds})`}</b>
          }
        </div>
      </h4>
      {
        <div className={collapsed ? 'hide' : 'col-lg-12'}>
          {
            recommendedChildrenIds.length > 0 &&
            <Toggler
              handler={() => toggleRecommended()}
              value={areRecommendedChildrenSelected()}
              text={t("include recommended")}
            />
          }
          <ChildChannels
            {...props}
          />
        </div>
      }
    </div>
  );
};


export default GroupChannels;
