// @flow

import React, {useState} from 'react';
import {showErrorToastr} from 'components/toastr/toastr';
import TextInput from './text-input';
import SelectInput from './select-input';
import Network from 'utils/network';

export default function AppStreams() {
  const [channels, setChannels] = useState([]);
  const [isBrowse, setBrowse] = useState(false);
  const [isLoading, setLoading] = useState(false);

  const enableBrowse = () => {
    setLoading(true);
    Network.get("/rhn/manager/api/channels/modular", "application/json").promise
      .then((channels) => {
        setChannels(channels.data);
        setLoading(false);
        setBrowse(true);
      }).catch(xhr => showErrorToastr(Network.responseErrorMessage(xhr)));
  }

  return (
    isBrowse ?
      <SelectInput channels={channels}/>
    :
      <>
        <div className="form-group">
          <div className="col-md-offset-3 col-md-6">
            <a href="#" onClick={enableBrowse}>
              {isLoading ? <i className="fa fa-refresh fa-spin fa-fw"/> : <i className="fa fa-search fa-fw"/>}
              Browse available modules</a>
          </div>
        </div>
        <TextInput/>
      </>
  );
}
