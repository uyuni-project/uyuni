import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import { showErrorToastr } from "components/toastr/toastr";

import Network from "utils/network";

import SelectInput from "./select-input";
import TextInput from "./text-input";

export default function AppStreams({ matcher }) {
  const [channels, setChannels] = useState<{ id: string; name: string }[]>([]);
  const [isBrowse, setBrowse] = useState(false);
  const [isLoading, setLoading] = useState(false);

  const enableBrowse = () => {
    setLoading(true);
    Network.get("/rhn/manager/api/channels/modular")
      .then((channels) => {
        setChannels(channels.data);
        setLoading(false);
        setBrowse(true);
      })
      .catch((xhr) => showErrorToastr(Network.responseErrorMessage(xhr).map((msg) => msg.text)));
  };

  if (isBrowse) {
    return <SelectInput channels={channels} />;
  } else if (matcher === "equals") {
    return (
      <>
        <div className="form-group">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <Button
              className="btn-tertiary"
              handler={enableBrowse}
              icon={isLoading ? "fa-refresh fa-spin fa-fw" : "fa-search fa-fw"}
              text="Browse available modules"
            />
          </div>
        </div>
        <TextInput />
      </>
    );
  } else {
    return null;
  }
}
