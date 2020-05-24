// @flow

import React, {useState} from 'react';
import {Select} from 'components/input/Select';
import {showErrorToastr} from 'components/toastr/toastr';
import useLifecycleActionsApi from '../../shared/api/use-lifecycle-actions-api';
import ModuleSelector from './module-selector';

type SelectInputProps = {
  channels: Array<{id: string, name: string}>
}

export default function SelectInput(props: SelectInputProps) {
  const [modules, setModules] = useState({});
  const [isShowInputs, setShowInputs] = useState(false);
  const {onAction, isLoading} = useLifecycleActionsApi({resource: "appstreams"});

  const onChannelChange = (name, value) => {
    if (value) {
      setShowInputs(true);
      onAction(null, "get", value)
        .then(setModules)
        .catch(showErrorToastr);
    } else {
      setModules({});
    }
  }

  return (
    <>
      <Select
        name="moduleChannel"
        label={t("Channel")}
        labelClass="col-md-3"
        divClass="col-md-6"
        onChange={onChannelChange}
      >
        <option disabled value="">{t("Select a channel to browse available modules")}</option>
        {props.channels.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
      </Select>
      { isShowInputs &&
          <ModuleSelector modules={modules} isLoading={isLoading}/>
      }
    </>
  );
}
