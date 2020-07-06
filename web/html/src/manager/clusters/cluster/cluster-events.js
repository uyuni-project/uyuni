// @flow
import * as React from 'react';
import {useEffect, useState} from 'react';
import useClustersApi from '../shared/api/use-clusters-api';
import {Table} from 'components/table/Table';
import {Column} from 'components/table/Column';
import {SearchField} from 'components/table/SearchField';
import Functions from 'utils/functions';

import type {ClusterType, ErrorMessagesType} from '../shared/api/use-clusters-api'
import type {MessageType} from 'components/messages';

type Props = {
  cluster: ClusterType,
  setMessages: (Array<MessageType>) => void,
  eventsType: "pending"|"history"
}

const ClusterEvents = (props: Props) => {
    const [events, setEvents] = useState<any>(null);
    // const {fetchClusterEvents} = useClustersApi();

    // useEffect(() => {
    //   fetchClusterEvents(props.cluster.id, props.eventsType)
    //   .then(data => {
    //     setEvents(data.form);
    //   })
    //   .catch((error : ErrorMessagesType) => {
    //     props.setMessages(error.messages);
    //   });
    // });


    return <>
        <h2><i className="fa fa-suitcase"></i>
        Pending Events
        </h2>
        <div className="page-summary">
            The following events have been scheduled for this system.
            <br/>
            You may cancel events for this system by selecting them and clicking the <strong>Cancel Selected Events</strong> button.
        </div>

        <Table
            data={[]}
            identifier={row => row.id}
            initialSortColumnKey="type"
            >
            <Column
                columnKey="type"
                comparator={Functions.Utils.sortByText}
                header={t('Type')}
                cell={row => row}
            />
            <Column
                columnKey="summary"
                comparator={Functions.Utils.sortByText}
                header={t('Summary')}
                cell={row => row}
            />            
        </Table>                    

    </>;
}

export default ClusterEvents;