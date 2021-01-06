/* eslint-disable */
// @flow
'use strict';

import * as React from 'react';
import ReactDOM from 'react-dom';
import { TopPanel } from 'components/panels/TopPanel';
import { Text } from 'components/input/Text';
import { Select } from 'components/input/Select';
import { Form } from 'components/input/Form';
import { SubmitButton, Button } from 'components/buttons';
import Network from 'utils/network';
import { Messages } from 'components/messages';
import { Utils as MessagesUtils } from 'components/messages';
import { Utils } from 'utils/functions';
import SpaRenderer from 'core/spa/spa-renderer';

const msgMap = {
  "not_found":
    t("Image store not found"),
  "build_scheduled":
    t("The image import has been scheduled."),
  "taskomatic_error":
    t("There was an error while scheduling a task. Please make sure that the task scheduler is running.")
}

type ImportDto = {
  buildHostId: number,
  name: string,
  version: string,
  activationKey: string,
  storeId: number
}

type Model = {
  name: ?string,
  version: ?string,
  storeId: ?string,
  buildHostId: ?string,
  activationKey: string
}

function emptyModel(): Model {
  return {
    name: null,
    version: "latest",
    storeId: null,
    buildHostId: null,
    activationKey: ""
  }
}

class ImageImport extends React.Component {

  state: {
    imageStores: ?Object,
    messages: Array<Object>,
    model: Model,
    isInvalid: boolean,
    hosts: Array<Object>,
    activationkeys: Array<string>
  }

  constructor(props) {
    super(props);

    (this:any).getImageStores = this.getImageStores.bind(this);
    (this:any).getBounceUrl = this.getBounceUrl.bind(this);
    (this:any).onFormChange = this.onFormChange.bind(this);
    (this:any).onValidate = this.onValidate.bind(this);
    (this:any).getBuildHosts = this.getBuildHosts.bind(this);
    (this:any).getActivationKeys = this.getActivationKeys.bind(this);
    (this:any).onImport = this.onImport.bind(this);
    (this:any).handleActivationKeyChange = this.handleActivationKeyChange.bind(this);
    (this:any).clearFields = this.clearFields.bind(this);
    (this:any).handleResponseError = this.handleResponseError.bind(this);

    this.state = {
        imageStores: null,
        messages: [],
        model: emptyModel(),
        isInvalid: true,
        hosts: [],
        activationkeys: []
    }

  }

  componentDidMount() {
    this.getImageStores();
    this.getBuildHosts();
    this.getActivationKeys();
  }

  getImageStores() {
    const type = "registry";
    Network.get("/rhn/manager/api/cm/imagestores/type/" + type, "application/json").promise
      .then(data => {
          this.setState({
              imageStores: data
          });
      })
      .catch(this.handleResponseError);
  }

  getBuildHosts() {
    Network.get("/rhn/manager/api/cm/build/hosts/container_build_host").promise
      .then(data => {
        this.setState({
            hosts: data
        });
      })
      .catch(this.handleResponseError);
  }

  getActivationKeys() {
    Network.get("/rhn/manager/api/cm/activationkeys").promise
      .then(data => {
        this.setState({
            activationkeys: data
        });
      })
      .catch(this.handleResponseError);
  }

  getChannels(token) {
    if(!token) {
      this.setState({
        channels: undefined
      });
      return;
    }

    Network.get("/rhn/manager/api/cm/imageprofiles/channels/" + token).promise.then(res => {
      // Prevent out-of-order async results
      if(res.activationKey != this.state.model.activationKey)
        return false;

      this.setState({
        channels: res
      });
    });
  }

  handleResponseError(jqXHR) {
    this.setState({
         messages: Network.responseErrorMessage(jqXHR)
    });
  }

  getBounceUrl() {
      return encodeURIComponent("/rhn/manager/cm/import");
  }

  onFormChange(model) {
      this.setState({
          model: model
      });
  }

  onValidate(isValid) {
      this.setState({
          isInvalid: !isValid
      });
  }

  clearFields() {
    this.setState({
          model: emptyModel()
    });
  }

  onImport() {
    const storeId: number = parseInt(this.state.model.storeId);
    const buildHostId: number = parseInt(this.state.model.buildHostId)
    if (this.state.model.name &&
        this.state.model.version &&
        this.state.model.storeId &&
        this.state.model.buildHostId &&
        !isNaN(storeId) &&
        !isNaN(buildHostId)
      ) {
      const importObj: ImportDto = {
          buildHostId: buildHostId,
          activationKey: this.state.model.activationKey,
          storeId: storeId,
          name: this.state.model.name,
          version: this.state.model.version
      }
      return Network.post(
          "/rhn/manager/api/cm/images/import",
          JSON.stringify(importObj),
          "application/json"
          ).promise.then(data => {
              if(data.success) {
                  Utils.urlBounce("/rhn/manager/cm/images");
              } else {
                  this.setState({
                      messages: MessagesUtils.error(msgMap[data.messages[0]] ?
                        msgMap[data.messages[0]] : data.messages[0]
                      )
                  });
              }
          })
          .catch(this.handleResponseError);
    } else {
      // should not happen
      console.log("Not all required values present in model")
      this.setState({
          messages: MessagesUtils.error("Not all required values present.")
      });
    }
  }

  handleActivationKeyChange(name, value) {
    this.getChannels(value);
  }

  renderActivationKeySelect() {
    const hint = this.state.channels && (
      this.state.channels.base ?
        <ul className="list-unstyled">
          <li>{this.state.channels.base.name}</li>
          <ul>
            {
              this.state.channels.children.map(c => <li key={c.id}>{c.name}</li>)
            }
          </ul>
        </ul>
        :
        <span><em>{t("There are no channels assigned to this key.")}</em></span>
    );

    return (
      <Select name="activationKey" label={t("Activation Key")} hint={hint}
        labelClass="col-md-3" divClass="col-md-6" onChange={this.handleActivationKeyChange}
        isClearable options={this.state.activationkeys ? this.state.activationkeys : []}
      />
    );
  }

  render() {
    return (
        <TopPanel title={t("Import Image")} icon="fa fa-download">
        { this.state.messages ?
             <Messages items={this.state.messages}/> :
             null
        }
        <p>{t('You can import Container images only using this feature.')}</p>
            <Form model={this.state.model} className="image-build-form"
                    onChange={this.onFormChange} onSubmit={this.onImport}
                    onValidate={this.onValidate}>

                    <Select name="storeId" label={t("Image Store")} required
                      labelClass="col-md-3" divClass="col-md-6" invalidHint={
                        <span>Target Image Store is required.&nbsp;<a href={"/rhn/manager/cm/imagestores/create" + "?url_bounce=" + this.getBounceUrl()}>Create a new one</a>.</span>
                      }
                      options={this.state.imageStores}
                      getOptionValue={option => option.id}
                    />

                    <Text name="name" label={t("Image name")} required invalidHint={t("Image name is required.")} labelClass="col-md-3" divClass="col-md-6"/>

                    <Text name="version" label={t("Image version")} required invalidHint={t("Image version is required.")} labelClass="col-md-3" divClass="col-md-6"/>

                    <Select name="buildHostId" required label={t("Build Host")} labelClass="col-md-3" divClass="col-md-6"
                      isClearable options={this.state.hosts}
                      getOptionValue={option => option.id} getOptionLabel={option => option.name}
                    />

                    { this.renderActivationKeySelect() }
                    <div className="form-group">
                        <div className="col-md-offset-3 col-md-6">
                          <SubmitButton id="update-btn" className="btn-success" icon="fa-download" text={t("Import")} disabled={this.state.isInvalid}/>
                          <Button id="clear-btn" className="btn-default pull-right" icon="fa-eraser" text={t("Clear fields")} handler={this.clearFields}/>
                        </div>
                    </div>

            </Form>
        </TopPanel>
        );
  }

}

export const renderer = () => SpaRenderer.renderNavigationReact(
  <ImageImport />,
  document.getElementById('image-import')
)
