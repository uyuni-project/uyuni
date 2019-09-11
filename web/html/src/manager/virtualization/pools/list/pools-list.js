// @flow
import * as React from 'react';
import { Tree } from 'components/tree/tree';
import type { TreeItem, TreeData } from 'components/tree/tree';
import { CustomDiv } from 'components/custom-objects';
import { ProgressBar } from 'components/progressbar';
import { DataHandler } from 'components/data-handler';
import { SearchField } from 'components/data-handler';
import { VirtualizationPoolsListRefreshApi } from '../virtualization-pools-list-refresh-api';

type Props = {
  serverId: string,
  refreshInterval: number,
  pageSize: number,
};

function poolsInfoToTree(pools: Object) {
  if (pools == null) {
    return null;
  }

  const children = pools.reduce((result, pool) => {
    const volumes = pool.volumes.map((volume, idx) => ({
      id: `${pool.uuid}/${idx}`,
      data: Object.assign({}, volume, { itemType: 'volume' }),
      children: [],
    }));

    const poolItem = {
      id: pool.uuid,
      data: Object.assign({}, pool, { itemType: 'pool' }),
      children: volumes.map(volume => volume.id),
    };
    return result.concat([poolItem], volumes);
  }, []);

  const rootNode = {
    id: 'root',
    children: children.filter(child => child.data.itemType === 'pool')
                      .map(child => child.id),
  };

  return {
    rootId: 'root',
    items: [rootNode].concat(children),
  };
}

function getPoolsAndVolumes(tree: ?TreeData): Array<TreeItem> {
  const items = tree != null ? tree.items : [];
  return items.filter(item => item.data && (item.data.itemType === 'pool' || item.data.itemType === 'volume'));
}

function searchData (datum, criteria): boolean {
  if (criteria && datum.data.name) {
    return (datum.data.name).toLowerCase().includes(criteria.toLowerCase());
  }
  return true;
}

type FilteredTreeProps = {
  tree: TreeData,
  data?: Array<TreeItem>,
  children: React.Node,
};

function FilteredTree(props: FilteredTreeProps) {
  const ids = (props.data || []).map(item => item.id).concat(['root']);

  const filteredItems = props.tree.items.filter(item => ids.includes(item.id));

  // Extract the volumes of the filtered pools
  const filteredPools = filteredItems.filter(item => item.data && item.data.itemType === 'pool');
  const poolVolumesIds = filteredPools.reduce((acc, item) => acc.concat(item.children), []);

  // Extract the pools containing the filtered volumes
  const filteredVolumes = filteredItems.filter(item => item.data && item.data.itemType === 'volume');
  const volumePoolIds = filteredVolumes.map(vol => props.tree.items.filter(item => (item.children || []).includes(vol.id)))
                                       .reduce((acc, item) => acc.concat(item), [])
                                       .map(item => item.id);

  const allIds = ids.concat(poolVolumesIds, volumePoolIds);

  const filteredTreeData = {
    rootId: 'root',
    items: props.tree.items.filter(item => allIds.includes(item.id)),
  };

  return React.Children.toArray(props.children)
      .map(item => React.cloneElement(item, {data: filteredTreeData}));
}

export function PoolsList(props: Props) {
  return (
    <VirtualizationPoolsListRefreshApi
      serverId={props.serverId}
      refreshInterval={props.refreshInterval}
    >
      {
        ({
          pools,
          refreshError,
        }) => {
          function renderPool(pool: Object, renderNameColumn: Function): React.Node {
            return [
              <CustomDiv key="name" className="col" width="30" um="%">
                {renderNameColumn(pool.name)}
              </CustomDiv>,
              <CustomDiv key="state" className="col text-center" width="5" um="%">{pool.state}</CustomDiv>,
              <CustomDiv key="autostart" className="col text-center" width="5" um="%">
                {
                  pool.autostart &&
                  <i className="fa fa-check-square fa-1-5x" title={t(`${pool.name} is started automatically`)}/>
                }
              </CustomDiv>,
              <CustomDiv key="persistent" className="col text-center" width="5" um="%">
                {
                  pool.persistent &&
                  <i className="fa fa-check-square fa-1-5x" title={t(`${pool.name} is persistent`)}/>
                }
              </CustomDiv>,
              <CustomDiv key="target" className="col" width="30" um="%">{pool.targetPath}</CustomDiv>,
              <CustomDiv key="usage" className="col" width="10" um="%">
                <ProgressBar
                  progress={Math.round(100 * pool.allocation / pool.capacity)}
                  title={t(`${pool.allocation / (1024 * 1024)} GiB / ${pool.capacity / (1024 * 1024)} GiB in use`)}
                />
              </CustomDiv>,
              <CustomDiv key="actions" className="col text-right" width="10" um="%">
              </CustomDiv>,
            ];
          }

          function renderVolume (volume: Object, renderNameColumn: Function): React.Node {
            return [
              <CustomDiv key="name" className="col" width="calc((100% + 3em) * 0.3 - 3em)" um="">
                {renderNameColumn(volume.name)}
              </CustomDiv>,
              <CustomDiv key="usedBy" className="col" width="calc((100% + 3em) * 0.45)" um="">{volume.usedBy != null ? volume.usedBy.join(", ") : ''}</CustomDiv>,
              <CustomDiv key="usage" className="col" width="calc((100% + 3em) * 0.1)" um="">
                <ProgressBar
                  progress={Math.min(100, Math.round(100 * volume.allocation / volume.capacity))}
                  title={t(`${volume.allocation / (1024 * 1024)} GiB / ${volume.capacity / (1024 * 1024)} GiB in use`)}
                />
              </CustomDiv>,
              <CustomDiv key="actions" className="col text-right" width="calc((100% + 3em) * 0.1)" um="">
                <div className="btn-group">
                </div>
              </CustomDiv>,
            ];
          }

          function renderItem (item: TreeItem, renderNameColumn: Function): React.Node {
            const itemRenderers = {
              pool: renderPool,
              volume: renderVolume,
            };
            if (item.data != null) {
              return itemRenderers[item.data.itemType](item.data, renderNameColumn);
            } else {
              return null;
            }
          }

          const tree = poolsInfoToTree(pools);
          const header = [
            <CustomDiv key="name" className="col col-class-calc-width" width="30" um="%">{t('Name')}</CustomDiv>,
            <CustomDiv key="state" className="col text-center" width="5" um="%">{t('State')}</CustomDiv>,
            <CustomDiv key="autostart" className="col text-center" width="5" um="%">{t('Autostart')}</CustomDiv>,
            <CustomDiv key="persistent" className="col text-center" width="5" um="%">{t('Persistent')}</CustomDiv>,
            <CustomDiv key="target" className="col" width="30" um="%">{t('Location')}</CustomDiv>,
            <CustomDiv key="usage" className="col" width="10" um="%">{t('Usage')}</CustomDiv>,
            <CustomDiv key="actions" className="col text-right" width="10" um="%">{t('Actions')}</CustomDiv>,
          ];

          return (
            <DataHandler
              data={getPoolsAndVolumes(tree)}
              identifier={(raw) => raw.id}
              initialItemsPerPage={props.pageSize}
              loading={tree == null}
              additionalFilters={[]}
              searchField={
                  <SearchField filter={searchData}
                      criteria={''}
                      placeholder={t('Filter by pool or volume name')}
                      name='pool-name-filter'
                  />
              }
            >
            {
              tree != null &&
              <FilteredTree tree={tree}>
                <Tree
                  header={header}
                  renderItem={renderItem}
                />
              </FilteredTree>
            }
            </DataHandler>
          );
        }
      }
    </VirtualizationPoolsListRefreshApi>
  );
}
