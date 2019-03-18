// @flow
import React, {useState} from 'react';
import {Toggler} from "../../../../../../../components/toggler";
import {Highlight} from "components/data-handler";
import {Loading} from "../../../../../../../components/loading/loading";
import {ChannelAnchorLink} from "../../../../../../../components/links";

type PropsType = {
  base: Object,
  search: String,
  childChannelsId: Array<number>,
  selectedChannelsIdsInGroup: Array<number>,
  onChannelsToggle: Function,
  channelsById: Object,
}

const ChildChannels = (props: PropsType) => {

  if(props.dependencyDataAvailable) { // TODO: Adapt prop after implementing mandatory
    return <Loading text='Loading dependencies..' />;
  }

  if(props.childChannelsId.length == 0) {
    return <span>&nbsp;{t('no child channels')}</span>;
  }

  const childChannels = props.childChannelsId.map(cId => props.channelsById[cId]);

  return (
    childChannels.map(c => {
      // const toolTip = props.dependenciesTooltip(c.id);
      // const mandatoryChannelsForBaseId: ?Set<number> = props.base && props.requiredChannels.get(props.base.id);

      // const isMandatory = mandatoryChannelsForBaseId && mandatoryChannelsForBaseId.has(c.id);
      // const isDisabled = isMandatory && props.selectedChannelsIdsInGroup.includes(c.id);
      return (
        <div key={c.id} className='checkbox'>
          <input type='checkbox'
                 value={c.id}
                 id={'child_' + c.id}
                 name='childChannels'
                 checked={props.selectedChannelsIdsInGroup.includes(c.id)}
               // disabled={isDisabled}
                 onChange={(event) => props.onChannelsToggle([event.target.value])}
          />
          {
            // // add an hidden carbon-copy of the disabled input since the disabled one will not be included in the form submit
            // isDisabled ?
            //   <input type='checkbox' value={c.id} name='childChannels'
            //          hidden='hidden' checked={props.selectedChannelsIdsInGroup.includes(c.id)} readOnly={true}/>
            //   : null
          }
          {/*title={toolTip}*/}
          <label htmlFor={"child_" + c.id}>
            <Highlight
              enabled={props.search}
              text={c.name}
              highlight={props.search}
            >
            </Highlight>
          </label>
          &nbsp;
          {/*{*/}
          {/*toolTip ?*/}
          {/*<a href="#"><i className="fa fa-info-circle spacewalk-help-link" title={toolTip}></i></a>*/}
          {/*: null*/}
          {/*}*/}
          &nbsp;
          {
            c.recommended ?
              <span className='recommended-tag-base' title={'This channel is recommended'}>{t('recommended')}</span>
              : null
          }
          {/*{*/}
          {/*isMandatory ?*/}
          {/*<span className='mandatory-tag-base' title={'This channel is mandatory'}>{t('mandatory')}</span>*/}
          {/*: null*/}
          {/*}*/}
          <ChannelAnchorLink id={c.id} newWindow={true}/>
        </div>
      )
    })
  );
};


export default ChildChannels;
