import * as React from "react";
import { useEffect, useState } from "react";
import { usePrevious } from "utils/hooks";
import { Select, FormContext } from "components/input";
import { Props as FilterFormProps } from "../filter-form";

import { Template } from "./index";
import Network, { JsonResult } from "utils/network";

type Channel = {
  id: number;
  label: string;
  name: string;
};

function getChannels() {
  return Network.get<JsonResult<Channel[]>>("/rhn/manager/api/channels/modular").then(Network.unwrap);
}

export default (props: FilterFormProps & { template: Template }) => {
  const template = props.template;
  const prevTemplate = usePrevious(template);

  const formContext = React.useContext(FormContext);
  const setModelValue = formContext.setModelValue;
  const [channels, setChannels] = useState<Channel[]>([]);

  useEffect(() => {
    getChannels().then(setChannels).catch(Network.showResponseErrorToastr);
  }, []);

  useEffect(() => {
    if (prevTemplate) {
      setModelValue?.("channelId", null);
    }
  }, [template]);

  return (
    <Select
      name="channelId"
      label={t("Channel")}
      labelClass="col-md-3"
      divClass="col-md-6"
      required
      options={channels}
      getOptionValue={(channel) => channel.id}
      getOptionLabel={(channel) => channel.name}
    />
  );
};
