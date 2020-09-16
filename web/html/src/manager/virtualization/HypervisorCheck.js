// @flow
import * as React from 'react';

import { Messages } from 'components/messages';
import { Utils as MessagesUtils } from 'components/messages';

type Props = {
    hypervisor: string,
    foreignEntitled: boolean,
};

export function HypervisorCheck(props: Props) {
    if (props.foreignEntitled) {
        return null;
    }

    const virtMissing = props.hypervisor.includes("'virt' __virtual__ returned False");
    if (virtMissing) {
        return <Messages items={MessagesUtils.error(t("Please install libvirt python module."))}/>;
    }

    const error = props.hypervisor === "" ?
        MessagesUtils.error(t("Neither KVM nor Xen is running. Ensure they are installed and the kernel modules are loaded.")) :
        [];
    return (
        <Messages items={error}/>
    );
};

HypervisorCheck.defaultProps = {
    foreignEntitled: false,
};
