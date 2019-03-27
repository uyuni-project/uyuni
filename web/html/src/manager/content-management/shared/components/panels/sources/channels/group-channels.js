// @flow
import React from 'react';
import {Toggler} from "components/toggler";
import {Highlight} from "components/data-handler";
import ChildChannels from "./child-channels";
import type {ChannelsTreeType} from "core/channels/api/use-channels-tree-api";
import type {RequiredChannelsResultType} from "core/channels/api/use-mandatory-channels-api";
import {getAllRecommentedIdsByBaseId} from "core/channels/utils/channels-state.utils";

type PropsType = {
  base: Object,
  search: string,
  childChannelsId: Array<number>,
  selectedChannelsIdsInGroup: Array<number>,
  selectedBaseChannelId: number,
  isOpen: boolean,
  setAllRecommentedChannels: Function,
  onChannelToggle: Function,
  onOpenGroup: Function,
  channelsTree: ChannelsTreeType,
  requiredChannelsResult: RequiredChannelsResultType,
}

const GroupChannels = (props: PropsType) => {
  const nrOfSelectedChilds = props.selectedChannelsIdsInGroup.length;

  const {recommendedIds, areRecommendedChildrenSelected} = getAllRecommentedIdsByBaseId(
    props.base.id,
    props.channelsTree,
    props.selectedChannelsIdsInGroup
  );
  const toggleRecommended = () =>
    areRecommendedChildrenSelected
      ? props.setAllRecommentedChannels(false)
      : props.setAllRecommentedChannels(true)

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
               onChange={() => {
                 props.onChannelToggle(props.base.id)
               }}
               disabled={props.base.id === props.selectedBaseChannelId}
        />
        &nbsp;
        &nbsp;
        <div
          style={{display: "inline"}}
          onClick={() => props.onOpenGroup(!props.isOpen)}
        >
          <i className={'fa ' + (props.isOpen ? 'fa-angle-down': 'fa-angle-right')} />
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
        <div className={props.isOpen ? 'col-lg-12' : 'hide'}>
          {
            recommendedIds.filter(id => id !== props.base.id).length > 0 &&
            <Toggler
              handler={() => toggleRecommended()}
              value={areRecommendedChildrenSelected}
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
