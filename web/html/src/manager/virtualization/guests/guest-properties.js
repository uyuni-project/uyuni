// @flow

import type { ActionChain } from 'components/action-schedule';

const React = require('react');
const { Panel } = require('components/panels/Panel');
const { Text } = require('components/input/Text');
const { Select } = require('components/input/Select');
const Validation = require('components/validation');
const MessagesUtils = require('components/messages').Utils;
const { Loading } = require('components/loading/loading');
const { guestNicsPanel } = require('./properties/guest-nics-panel');
const { guestDisksPanel } = require('./properties/guest-disks-panel');
const { GuestPropertiesForm } = require('./properties/guest-properties-form');
const { GuestPropertiesTraditional } = require('./properties/guest-properties-traditional');
const { VirtualizationDomainsCapsApi } = require('./virtualization-domains-caps-api');
const { VirtualizationNetsListRefreshApi } = require('../nets/virtualization-nets-list-refresh-api');
const { VirtualizationPoolsListRefreshApi } = require('../pools/virtualization-pools-list-refresh-api');

type Props = {
  host: Object,
  submitText: string,
  submit: Function,
  initialModel: ?Object,
  messages: Array<String>,
  localTime: string,
  timezone: string,
  actionChains: Array<ActionChain>,
};

/*
 * Component editing a virtual machine properties
 */
class GuestProperties extends React.Component<Props> {
  validationChecks = [{
    check: (model: Object) => !Number.isNaN(Number.parseInt(model.vcpu, 10))
      && (model.vcpu > this.props.host.cpu.count),
    message: MessagesUtils.warning('Overcommitting CPU can harm performances.'),
  }]

  render() {
    if (!this.props.host.saltEntitled) {
      return (
        <GuestPropertiesTraditional
          host={this.props.host}
          submitText={this.props.submitText}
          submit={this.props.submit}
          initialModel={this.props.initialModel}
          messages={this.props.messages}
          localTime={this.props.localTime}
          timezone={this.props.timezone}
          actionChains={this.props.actionChains}
        />
      );
    }

    const osTypesLabels = {
      hvm: 'Fully Virtualized',
      xen: 'Para Virtualized',
    };

    return (
      <VirtualizationNetsListRefreshApi serverId={this.props.host.id}>
        {
          ({
            networks,
            error: netListError,
          }) => (
            <VirtualizationPoolsListRefreshApi serverId={this.props.host.id}>
              {
                ({
                  pools,
                  error: poolListError,
                }) => (
                  <VirtualizationDomainsCapsApi hostId={this.props.host.id}>
                    {
                      ({
                        osTypes,
                        domainsCaps,
                        messages,
                      }) => {
                        const allMessages = [].concat(this.props.messages, messages, netListError, poolListError)
                          .filter(item => item);
                        const { initialModel } = this.props;

                        if (initialModel != null && networks != null && pools != null
                            && osTypes.length > 0 && domainsCaps.length > 0) {
                          return (
                            <GuestPropertiesForm
                              submitText={this.props.submitText}
                              submit={this.props.submit}
                              initialModel={initialModel}
                              validationChecks={this.validationChecks}
                              messages={allMessages}
                              localTime={this.props.localTime}
                              timezone={this.props.timezone}
                              actionChains={this.props.actionChains}
                            >
                              {
                                ({ model, changeModel }) => {
                                  const vmTypes = domainsCaps.map(cap => cap.domain)
                                    .filter((vmType, idx, array) => array.indexOf(vmType) === idx);
                                  const vmType = model.vmType
                                    || initialModel.vmType
                                    || (vmTypes.includes('kvm') ? 'kvm' : vmTypes[0]);
                                  const arch = initialModel.arch || this.props.host.cpu.arch;
                                  const caps = domainsCaps.find(cap => cap.arch === arch && cap.domain === vmType);

                                  return [
                                    <Panel key="general" title={t('General')} headingLevel="h2">
                                      { initialModel.name === undefined
                                        && (
                                        <Text
                                          name="name"
                                          label={t('Name')}
                                          required
                                          invalidHint={t('Can not contain the following characters: /\\')}
                                          labelClass="col-md-3"
                                          divClass="col-md-6"
                                          validators={[Validation.matches(/^[^/\\]+$/)]}
                                        />)
                                      }
                                      { initialModel.vmType === undefined
                                        && (
                                        <Select
                                          labelClass="col-md-3"
                                          divClass="col-md-6"
                                          label={t('Hypervisor')}
                                          name="vmType"
                                          required
                                          defaultValue={vmTypes.includes('kvm') ? 'kvm' : vmTypes[0]}
                                        >
                                          {
                                            vmTypes.map(k => <option key={k} value={k}>{k}</option>)
                                          }
                                        </Select>)
                                      }
                                      { initialModel.osType === undefined
                                        && (
                                        <Select
                                          labelClass="col-md-3"
                                          divClass="col-md-6"
                                          label={t('Virtual Machine Type')}
                                          name="osType"
                                          required
                                          defaultValue={osTypes[0]}
                                        >
                                          {
                                            osTypes.map(k => <option key={k} value={k}>{osTypesLabels[k]}</option>)
                                          }
                                        </Select>)
                                      }
                                      <Text
                                        name="memory"
                                        label={t('Maximum Memory (MiB)')}
                                        required
                                        invalidHint={t('A positive integer is required')}
                                        labelClass="col-md-3"
                                        divClass="col-md-6"
                                        validators={[Validation.isInt({ gt: 0 })]}
                                      />
                                      <Text
                                        name="vcpu"
                                        label={t('Virtual CPU Count')}
                                        required
                                        invalidHint={t('A positive integer is required')}
                                        labelClass="col-md-3"
                                        divClass="col-md-6"
                                        validators={[Validation.isInt({ gt: 0 })]}
                                      />
                                      { initialModel.arch === undefined
                                        && (
                                        <Select
                                          labelClass="col-md-3"
                                          divClass="col-md-6"
                                          label={t('Architecture')}
                                          name="arch"
                                          required
                                          defaultValue={this.props.host.cpu.arch}
                                        >
                                          {
                                            domainsCaps.map(cap => cap.arch)
                                              .filter((item, index, array) => array.indexOf(item) === index)
                                              .map(k => <option key={k} value={k}>{k}</option>)
                                          }
                                        </Select>)
                                      }
                                    </Panel>,
                                    guestDisksPanel(model, changeModel, pools, caps),
                                    guestNicsPanel(model, changeModel, networks),
                                    <Panel key="graphics" title={t('Graphics')} headingLevel="h2">
                                      <Select
                                        labelClass="col-md-3"
                                        divClass="col-md-6"
                                        label={t('Type')}
                                        name="graphicsType"
                                      >
                                        {
                                          [{ key: 'vnc', display: 'VNC' }, { key: 'spice', display: 'Spice' }]
                                            .filter(entry => caps !== undefined
                                              && caps.devices.graphics.type.includes(entry.key))
                                            .map(entry => (
                                              <option key={entry.key} value={entry.key}>
                                                {entry.display}
                                              </option>))
                                        }
                                        <option key="" value="">{t('None')}</option>
                                      </Select>
                                    </Panel>,
                                  ];
                                }
                              }
                            </GuestPropertiesForm>
                          );
                        }
                        return <Loading text={t('Loading...')} withBorders={false} />;
                      }
                    }
                  </VirtualizationDomainsCapsApi>
                )
              }
            </VirtualizationPoolsListRefreshApi>
          )
        }
      </VirtualizationNetsListRefreshApi>
    );
  }
}

module.exports = {
  GuestProperties,
};
