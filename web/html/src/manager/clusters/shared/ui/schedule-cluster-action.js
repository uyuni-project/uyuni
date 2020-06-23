// @flow
import * as React from 'react';
import {useState} from 'react';
import {Panel} from 'components/panels/Panel';
import {AsyncButton, Button} from 'components/buttons';
import {ActionLink, ActionChainLink} from 'components/links';
import {ActionSchedule} from 'components/action-schedule';
import Functions from 'utils/functions';
import {withErrorMessages}  from '../api/use-clusters-api';
import useUserLocalization from 'core/user-localization/use-user-localization';
import {Messages} from 'components/messages';

import type {ActionChain} from "components/action-schedule";
import type {ErrorMessagesType} from '../api/use-clusters-api';
import type {MessageType} from 'components/messages';

declare var actionChains: Array<ActionChain>;

type Props = {
    title: string,
    panel: React.Node,
    schedule: (earliest: Date, actionChain: ?string) => Promise<any>,
    onPrev?: () =>  void,
    setMessages: (Array<MessageType>) => void,
    scheduleButtonLabel: string,
    actionType?: string
};

const ScheduleClusterAction = (props: Props) => {
    const {timezone, localTime} = useUserLocalization();

    const [actionChain, setActionChain] = useState<?ActionChain>(null);
    const [earliest, setEarliest] = useState(Functions.Utils.dateWithTimezone(localTime));
    const [disableSchedule, setDisableSchedule] = useState(false);

    const onSchedule = (): Promise<any> => {
        return props.schedule(earliest, actionChain ? actionChain.text: null).then(
            (actionId) => {
                setDisableSchedule(true);
                const actionChainMsg = Messages.success(<span>{t("Action has been successfully added to the Action Chain ")}
                        <ActionChainLink id={actionId}>{actionChain ? actionChain.text : ""}</ActionChainLink>.</span>);
                const actionMsg = Messages.success(<span>{t("Action has been ")}
                          <ActionLink id={actionId}>{t("scheduled")}</ActionLink>{t(" successfully.")}</span>);
                props.setMessages([actionChain ? actionChainMsg : actionMsg]);
            },
            (error: ErrorMessagesType) => {
                props.setMessages(error.messages);
            });
    }

    return (<Panel
                headingLevel="h4"
                title={props.title}
                footer={
                    <div className="btn-group">
                        {
                            props.onPrev ? <Button
                                id="btn-prev"
                                disabled={disableSchedule}
                                text={t("Back")}
                                className="btn-default"
                                icon="fa-arrow-left"
                                handler={props.onPrev}
                            /> : null
                        }
                        <AsyncButton
                            id="btn-next"
                            disabled={disableSchedule}
                            text={props.scheduleButtonLabel}
                            defaultType="btn-success"
                            action={onSchedule}
                        />
                    </div>
                }>

                {props.panel}

                <ActionSchedule
                    timezone={timezone}
                    localTime={localTime}
                    earliest={earliest}
                    actionType={props.actionType}
                    onDateTimeChanged={(date) => {setEarliest(date); setActionChain(null);}}
                />
            </Panel>);
}

export default withErrorMessages(ScheduleClusterAction);
