// @flow
import * as React from 'react';

import { Column } from 'components/table/Column';
import { Utils } from 'utils/functions';
import { LinkButton, AsyncButton } from 'components/buttons';
import * as Systems from 'components/systems';
import { Utils as ListUtils } from '../../list.utils';
import { ListTab } from '../../ListTab';
import { HypervisorCheck } from '../../HypervisorCheck';

type Props = {
  serverId: string,
  pageSize: number,
  saltEntitled: boolean,
  foreignEntitled: boolean,
  isAdmin: boolean,
  hypervisor: string,
};

export function GuestsList(props: Props) {
  const modalsData = [
    {
      type: 'start', name: t('Start / Resume'), icon: 'fa-play', bulkonly: true,
    },
    {
      type: 'shutdown', name: t('Stop'), icon: 'fa-stop', bulkonly: false, canForce: true, forceName: t('Force off'),
    },
    {
      type: 'restart', name: t('Restart'), icon: 'fa-refresh', bulkonly: false, canForce: true, forceName: t('Reset'),
    },
    {
      type: 'suspend', name: t('Suspend'), icon: 'fa-pause', bulkonly: false,
    },
    {
      type: 'delete', name: t('Delete'), icon: 'fa-trash', bulkonly: false,
    },
  ];

  return (
    <>
      <HypervisorCheck foreignEntitled={props.foreignEntitled} hypervisor={props.hypervisor}/>
      <ListTab
        serverId={props.serverId}
        saltEntitled={props.saltEntitled}
        pageSize={props.pageSize}
        type="guest"
        title={t('Hosted Virtual Systems')}
        description={t('This is a list of virtual guests which are configured to run on this host.')}
        modalsData={modalsData}
        isActionVisible={(action) => !props.foreignEntitled && (action.type !== 'delete' || props.saltEntitled)}
        getCreateActionsKeys={(actions) => {
          return Object.keys(actions).filter(key => key.startsWith("new-") && actions[key].type === "virt.create")
        }}
        idName="uuid"
      >
      {
        (createModalButton, onAction) => {
            const columns = [
              <Column
                columnKey="name"
                comparator={Utils.sortByText}
                header={t('Guest')}
                cell={row => row.name}
              />,
              <Column
                columnKey="serverName"
                comparator={Utils.sortByText}
                header={t('System')}
                cell={(row) => {
                  if (row.virtualSystemId == null) {
                    return t('Unregistered System');
                  }

                  if (row.accessible) {
                    return <a href={`/rhn/systems/details/Overview.do?sid=${row.virtualSystemId}`}>{row.serverName}</a>;
                  }
                  return row.serverName;
                }}
              />,
              <Column
                columnKey="statusType"
                comparator={ListUtils.sortByUpdate}
                header={t('Updates')}
                cell={(row) => {
                  if (row.statusType == null) {
                    return '-';
                  }
                  return Systems.statusDisplay(row, props.isAdmin);
                }}
              />,
              <Column
                columnKey="stateLabel"
                header={t('State')}
                comparator={ListUtils.sortByState}
                cell={row => row.stateName}
              />,
              <Column
                columnKey="memory"
                comparator={Utils.sortByNumber}
                header={t('Current Memory')}
                cell={row => `${row.memory / 1024} MiB`}
              />,
              <Column
                columnKey="vcpus"
                comparator={Utils.sortByNumber}
                header={t('vCPUs')}
                cell={row => row.vcpus}
              />,
              <Column
                columnKey="channelLabels"
                comparator={Utils.sortByText}
                header={t('Base Software Channel')}
                cell={(row) => {
                  if (row.channelId == null) {
                    return t('(none)');
                  }
                  if (row.subscribable) {
                    return <a href={`/rhn/channels/ChannelDetail.do?cid=${row.channelId}`}>{row.channelLabels}</a>;
                  }
                  return row.channelLabels;
                }}
              />
            ];

            const actionsProvider =
              (row) => {
                if (props.foreignEntitled) {
                  return [];
                }
                const state = row.stateLabel;
                return (
                  <div className="btn-group">
                    {state !== 'running' && row.name !== 'Domain-0'
                     && (
                       <AsyncButton
                         defaultType="btn-default btn-sm"
                         title={t(state === 'paused' ? 'Resume' : 'Start')}
                         icon="fa-play"
                         action={() => onAction('start', [row.uuid], {})}
                       />) }
                    {state === 'running' && row.name !== 'Domain-0' && createModalButton('suspend', modalsData, row)}
                    {state !== 'stopped' && row.name !== 'Domain-0' && createModalButton('shutdown', modalsData, row)}
                    {(state === 'paused' || state === 'running') && createModalButton('restart', modalsData, row)}
                    {props.saltEntitled && state === 'running' && (
                      <LinkButton
                        title={t('Graphical Console')}
                        className="btn-default btn-sm"
                        icon="fa-desktop"
                        href={`/rhn/manager/systems/details/virtualization/guests/${props.serverId}/console/${row.uuid}`}
                        target="_blank"
                      />
                    )}
                    <LinkButton
                      title={t('Edit')}
                      className="btn-default btn-sm"
                      icon="fa-edit"
                      href={`/rhn/manager/systems/details/virtualization/guests/${props.serverId}/edit/${row.uuid}`}
                    />
                    {props.saltEntitled && row.name !== 'Domain-0' && createModalButton('delete', modalsData, row)}
                  </div>
                );
              }

            return { columns, actionsProvider };
          }
      }
      </ListTab>
    </>
  );
}
