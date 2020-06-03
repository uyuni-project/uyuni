// @flow
import { hot } from 'react-hot-loader';
import React, {useEffect} from 'react';
import withPageWrapper from 'components/general/with-page-wrapper';
import {TopPanel} from 'components/panels/TopPanel';
import {LinkButton} from 'components/buttons';
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";
import {Table} from 'components/table/Table';
import {Column} from 'components/table/Column';
import {SearchField} from 'components/table/SearchField';
import Functions from 'utils/functions';
import {SystemLink} from 'components/links';
import {showInfoToastr} from 'components/toastr/toastr';

import type {ClusterType} from '../shared/api/use-clusters-api';

type Props = {
  clusters: Array<ClusterType>,
  flashMessage: String,
};

const ListClusters = (props) => {
    const roles = useRoles();
    const hasEditingPermissions = isOrgAdmin(roles);
    const panelButtons = (
        <div className="pull-right btn-group">
        {
            hasEditingPermissions &&
            <LinkButton
                id="addCluster"
                icon="fa-plus"
                className="btn-link js-spa"
                title={t('Add an existing cluster')}
                text={t('Add Cluster')}
                href="/rhn/manager/clusters/add"
            />
        }
        </div>
    );

    const filterFunc = (row, criteria) => {
        const keysToSearch = ['name', 'type'];
        if (criteria) {
            return keysToSearch.map(key => row[key]).join().toLowerCase().includes(criteria.toLowerCase());
        }
        return true;
    };

    useEffect(()=> {
        if(props.flashMessage) {
            showInfoToastr(props.flashMessage);
        }
    }, []);

    return (
        <React.Fragment>
            <TopPanel title={t('Clusters')}
                icon="spacewalk-icon-clusters" button={panelButtons}
                helpUrl="/docs/reference/clusters/clusters-menu.html">
                <Table
                    data={props.clusters}
                    identifier={row => row.id}
                    initialSortColumnKey="name"
                    searchField={(
                        <SearchField
                        filter={filterFunc}
                        placeholder={t('Filter by any value')}
                        />
                    )}
                    >
                    <Column
                        columnKey="name"
                        comparator={Functions.Utils.sortByText}
                        header={t('Name')}
                        cell={(row: ClusterType) =>
                        <a
                            className="js-spa"
                            href={`/rhn/manager/cluster/${row.id}`}>
                            {row.name}
                        </a>
                        }
                    />
                    <Column
                        columnKey="type"
                        comparator={Functions.Utils.sortByText}
                        header={t('Type')}
                        cell={(row: ClusterType) => row.provider.name}
                    />
                    <Column
                        columnKey="type"
                        comparator={Functions.Utils.sortByText}
                        header={t('Management node')}
                        cell={(row: ClusterType) => <SystemLink id={row.managementNode.id}>{row.managementNode.name}</SystemLink> }
                    />                
                </Table>
            </TopPanel>
        </React.Fragment>);

}

export default hot(module)(withPageWrapper<Props>(ListClusters));
