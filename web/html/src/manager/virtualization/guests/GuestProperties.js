// @flow

import type { ActionChain } from 'components/action-schedule';
import type { MessageType } from 'components/messages';

import * as React from 'react';
import { Panel } from 'components/panels/Panel';
import { Text } from 'components/input/Text';
import { Select } from 'components/input/Select';
import Validation from 'components/validation';
import { Messages } from 'components/messages';
import { Loading } from 'components/utils/Loading';
import { guestNicsPanel } from './properties/guest-nics-panel';
import { GuestDisksPanel } from './properties/GuestDisksPanel';
import { GuestPropertiesForm } from './properties/guest-properties-form';
import { GuestPropertiesTraditional } from './properties/guest-properties-traditional';
import { VirtualizationDomainsCapsApi } from './virtualization-domains-caps-api';
import { VirtualizationNetsListRefreshApi } from '../nets/virtualization-nets-list-refresh-api';
import { VirtualizationPoolsListRefreshApi } from '../pools/virtualization-pools-list-refresh-api';

type Props = {
  host: Object,
  submitText: string,
  submit: Function,
  initialModel: ?Object,
  messages: Array<MessageType>,
  localTime: string,
  timezone: string,
  actionChains: Array<ActionChain>,
};

/**
 * Component editing a virtual machine properties
 */
export function GuestProperties(props: Props) : React.Node {
  if (!props.host.saltEntitled) {
    return (
      <GuestPropertiesTraditional
        host={props.host}
        submitText={props.submitText}
        submit={props.submit}
        initialModel={props.initialModel}
        messages={props.messages}
        localTime={props.localTime}
        timezone={props.timezone}
        actionChains={props.actionChains}
      />
    );
  }

  const validationChecks = [{
    check: (model: Object) => !Number.isNaN(Number.parseInt(model.vcpu, 10))
      && (model.vcpu > props.host.cpu.count),
    message: Messages.warning('Overcommitting CPU can harm performances.'),
  }]

  const osTypesLabels = {
    hvm: 'Fully Virtualized',
    xen: 'Para Virtualized',
  };

  return (
    <VirtualizationNetsListRefreshApi serverId={props.host.id}>
      {
        ({
          networks,
          error: netListError,
        }) => (
          <VirtualizationPoolsListRefreshApi serverId={props.host.id}>
            {
              ({
                pools,
                error: poolListError,
              }) => (
                <VirtualizationDomainsCapsApi hostId={props.host.id}>
                  {
                    ({
                      osTypes,
                      domainsCaps,
                      messages,
                    }) => {
                      const allMessages = [].concat(props.messages, messages, netListError, poolListError)
                        .filter(item => item);
                      const { initialModel } = props;

                      if (initialModel != null && networks != null && pools != null
                          && osTypes.length > 0 && domainsCaps.length > 0) {
                        return (
                          <GuestPropertiesForm
                            submitText={props.submitText}
                            submit={props.submit}
                            initialModel={initialModel}
                            validationChecks={validationChecks}
                            messages={allMessages}
                            localTime={props.localTime}
                            timezone={props.timezone}
                            actionChains={props.actionChains}
                          >
                            {
                              ({ model, changeModel }) => {
                                const vmTypes = domainsCaps.map(cap => cap.domain)
                                  .filter((vmType, idx, array) => array.indexOf(vmType) === idx);
                                const vmType = model.vmType
                                  || initialModel.vmType
                                  || (vmTypes.includes('kvm') ? 'kvm' : vmTypes[0]);
                                const arch = initialModel.arch || props.host.cpu.arch;
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
                                        defaultValue={props.host.cpu.arch}
                                      >
                                        {
                                          domainsCaps.map(cap => cap.arch)
                                            .filter((item, index, array) => array.indexOf(item) === index)
                                            .map(k => <option key={k} value={k}>{k}</option>)
                                        }
                                      </Select>)
                                    }
                                  </Panel>,
                                  <GuestDisksPanel
                                    changeModel={changeModel}
                                    pools={pools}
                                    caps={caps}
                                  />,
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
