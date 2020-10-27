// @flow
import * as React from 'react';

import { Column } from 'components/table/Column';
import { Utils } from 'utils/functions';
import { AsyncButton } from 'components/buttons';
import { Utils as ListUtils } from '../../list.utils';
import { ListTab } from '../../ListTab';
import { HypervisorCheck } from '../../HypervisorCheck';


type Props = {
  serverId: string,
  pageSize: number,
  hypervisor: string,
};

export function NetsList(props: Props) {
  const modalsData = [
    {
      type: 'start', name: t('Start'), icon: 'fa-play', bulkonly: true,
    },
    {
      type: 'stop', name: t('Stop'), icon: 'fa-stop', bulkonly:false,
    },
    {
      type: 'delete', name: t('Delete'), icon: 'fa-trash', bulkonly: false,
    },
  ];
  return (
    <>
      <HypervisorCheck hypervisor={props.hypervisor} />
      <ListTab
        serverId={props.serverId}
        pageSize={props.pageSize}
        type="network"
        urlType="nets"
        title={t('Virtual Networks')}
        description={t('This is a list of virtual networks which are configured to run on this host.')}
        modalsData={modalsData}
        idName="name"
        canCreate={false}
      >
        {
          (createModalButton, onAction) => {
            const columns = [
              <Column
                columnKey="name"
                comparator={Utils.sortByText}
                header={t('Name')}
                cell={row => row.name}
              />,
              <Column
                columnKey="state"
                header={t('State')}
                comparator={ListUtils.sortByState}
                cell={row => row.active ? 'running' : 'stopped'}
              />,
              <Column
                columnKey="autostart"
                header={t('Autostart')}
                cell={
                  row => row.autostart &&
                    <i className="fa fa-check-square fa-1-5x" title={t(`${row.name} is started automatically`)}/>
                }
              />,
              <Column
                columnKey="persistent"
                header={t('Persistent')}
                cell={
                  row => row.persistent &&
                    <i className="fa fa-check-square fa-1-5x" title={t(`${row.name} is persistent`)}/>
                }
              />,
              <Column
                columnKey="bridge"
                comparator={Utils.sortByText}
                header={t('Bridge')}
                cell={row => row.bridge}
              />,
            ];
            const actionsProvider = (row) => {
              return (
                <div className="btn-group">
                {
                  !row.active && (
                    <AsyncButton
                      defaultType="btn-default btn-sm"
                      title={t('Start')}
                      icon="fa-play"
                      action={() => onAction('start', [row.name], {})}
                    />
                  )
                }
                { row.active && createModalButton('stop', modalsData, row) }
                { createModalButton('delete', modalsData, row) }
                </div>
              );
            };
            return {columns, actionsProvider};
          }
        }
      </ListTab>
    </>
  );
}
