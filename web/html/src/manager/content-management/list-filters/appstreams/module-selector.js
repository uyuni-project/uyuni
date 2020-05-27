// @flow

import React, {useContext} from 'react';
import Combo from 'components/input/Combo';
import {FormContext} from 'components/input/Form';

type ModuleSelectorProps = {
  modules: {
    [name: string]: {
      default: string,
      streams: Array<string>
    }
  },
  isLoading: boolean
}

export default function ModuleSelector(props: ModuleSelectorProps) {
  const formContext = useContext(FormContext);
  const createOption = value => ({value: value, label: value});
  const moduleOptions = Object.keys(props.modules).map(createOption);
  const getModuleName = ctx => ctx.model['moduleName'];
  const getStreamOptions = ctx =>
    ((props.modules[getModuleName(ctx)] || {}).streams || []).map(createOption);
  const getDefaultStream = module => (props.modules[module] || {}).default;

  return (
    <>
      <Combo
        name="moduleName"
        label={t("Module")}
        isLoading={props.isLoading}
        options={moduleOptions}
        labelClass="col-md-3"
        divClass="col-md-6"
        placeholder={t("Select a module...")}
        emptyText={t("No modules available")}
        onChange={(name, module) => {formContext.model['moduleStream'] = getDefaultStream(module)}}
        required
      />
      <Combo
        name="moduleStream"
        label={t("Stream")}
        options={getStreamOptions(formContext)}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!getModuleName(formContext)}
        placeholder={t("Select a stream...")}
        emptyText={t("No streams available")}
      />
    </>
  );
}
