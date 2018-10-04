// @flow
import ChildChannels from './child-channels';
import ActivationKeyChannelsApi from "./activation-key-channels-api";

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../../utils/network');
const Loading = require('../../components/loading/loading').Loading;

type ActivationKeyChannelsProps = {
  activationKeyId: number
}

type ActivationKeyChannelsState = {
  currentSelectedBaseId: number,
  currentChildSelectedIds: Array<number>,
}

class ActivationKeyChannels extends React.Component<ActivationKeyChannelsProps, ActivationKeyChannelsState> {
  constructor(props: ActivationKeyChannelsProps) {
    super(props);
    this.state = {
      currentSelectedBaseId: -1,
      currentChildSelectedIds: [],
    }
  }

  getDefaultBase() {
    return { id: -1, name: t('SUSE Manager Default'), custom: false, subscribable: true};
  }

  handleBaseChange = (event: SyntheticInputEvent<*>) => {
    const newBaseId : number = parseInt(event.target.value);
    return new Promise((resolve) =>
      this.setState(
        {currentSelectedBaseId: newBaseId},
        () => resolve(newBaseId)
      )
    );
  };

  handleChildChange = (event: SyntheticInputEvent<*>) => {
    this.selectChildChannels([parseInt(event.target.value)], event.target.checked);
  };

  selectChildChannels = (channelIds: Array<number>, selectedFlag: boolean) => {
    var selectedIds = [...this.state.currentChildSelectedIds];
    if (selectedFlag) {
      selectedIds = [...channelIds.filter(c => !selectedIds.includes(c)), ...selectedIds];
    }
    else {
      selectedIds = [...selectedIds.filter(c => !channelIds.includes(c))];
    }
    this.setState({currentChildSelectedIds: selectedIds});
  }

  onNewBaseChannel = ({currentSelectedBaseId, currentChildSelectedIds}: ActivationKeyChannelsState) => {
    this.setState({currentSelectedBaseId, currentChildSelectedIds});
  }

  renderChildChannels = ({loadingChildren, availableChannels}) => {
    return loadingChildren ?
      <Loading text='Loading child channels..' />
      : availableChannels.map(g =>
        <ChildChannels
          key={g.base.id}
          channels={g.children.sort((c1, c2) => c1.name > c2.name)}
          base={g.base}
          showBase={availableChannels.length > 1}
          selectedChannelsIds={this.state.currentChildSelectedIds}
          selectChannels={this.selectChildChannels}
          // Todo: [LN->Dario] this code here is weeeird -> why not use setState and an object  ChildChannelsForBase[childchannelsforbase] ->
          //   if we don't need to force a reUpdate, we can save this on this.things and not state
          saveState={(state) => {this.state["ChildChannelsForBase" + g.base.id] = state;}}
          loadState={() => this.state["ChildChannelsForBase" + g.base.id]}
          collapsed={Array.from(availableChannels.keys()).length > 1}
        />
      );
  };

  render() {
    return (
      <ActivationKeyChannelsApi
        onNewBaseChannel={this.onNewBaseChannel}
        activationKeyId={this.props.activationKeyId}
        currentSelectedBaseId={this.state.currentSelectedBaseId}>
        {
          ({
             messages,
             loading,
             loadingChildren,
             availableBaseChannels,
             availableChannels,
             fetchChildChannels
           }) => {

            if (loading) {
              return (
                <div className='form-group'>
                  <Loading text='Loading..' />
                </div>
              )
            }

            return (
              <div>
                <div className='form-group'>
                  <label className='col-lg-3 control-label'>{t('Base Channel:')}</label>
                  <div className='col-lg-6'>
                    <select name='selectedBaseChannel' className='form-control'
                            value={this.state.currentSelectedBaseId}
                            onChange={
                              (event) => this.handleBaseChange(event).then((newBaseId) => fetchChildChannels(newBaseId))
                            }>
                      <option value={this.getDefaultBase().id}>{this.getDefaultBase().name}</option>
                      {
                        availableBaseChannels
                          .sort((b1, b2) => b1.name > b2.name)
                          .map(b => <option key={b.id} value={b.id}>{b.name}</option>)
                      }
                    </select>
                    <span className='help-block'>
                      {t('Choose "SUSE Manager Default" to allow systems to register to the default SUSE Manager ' +
                        'provided channel that corresponds to the installed SUSE Linux version. Instead of the default, ' +
                        'you may choose a particular SUSE provided channel or a custom base channel, but if a system using ' +
                        'this key is not compatible with the selected channel, it will fall back to its SUSE Manager Default channel.')}
                    </span>
                  </div>
                </div>
                <div className='form-group'>
                  <label className='col-lg-3 control-label'>{t('Child Channel:')}</label>
                  <div className='col-lg-6'>
                    {this.renderChildChannels({
                      loadingChildren,
                      availableChannels
                    })}
                    <span className='help-block'>
                      {t('Any system registered using this activation key will be subscribed to the selected child channels.')}
                    </span>
                  </div>
                </div>
              </div>
            )
          }
        }
      </ActivationKeyChannelsApi>
    )
  }
}

export default ActivationKeyChannels;
