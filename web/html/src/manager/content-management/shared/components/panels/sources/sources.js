// @flow
import React from 'react';
import {Select} from "../../../../../../components/input/Select";
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import {addSource, modifySource, deleteSource} from "./sources.utils";
import {showSuccessToastr} from "components/toastr/toastr";

import type {projectSourceType} from '../../../type/project.type.js';

type SourcesProps = {
  sources: Array<projectSourceType>,
  onChange: Function,
};

// TODO: Implement STATE logic in backend
const panelsStateClassName = {
  "0": "panel-success",
  "1": "panel-default",
  "2": "panel-warning",
  "3": "panel-danger",
}

const generateNewSource = () => ({
  id: Math.floor((Math.random() * 10000)),
  name: ["SLES11 SP4 Pool x86_64", "SLES15 x86_64"][Math.floor(Math.random() * 2)],
  type: "Software Channel",
  level: "Child Channel",
})

const ModalSourceCreationContent = (props) => {
  return (
    <React.Fragment>
      <div className="row">
        <Select
          name="sourceType"
          label={t("Type")}
          labelClass="col-md-3"
          divClass="col-md-9">
          <option key="0" value="1">Channel</option>
        </Select>
      </div>
      <div className="row">
        <Select
          name="sourceBaseChannel"
          label={t("Base Channel")}
          labelClass="col-md-3"
          divClass="col-md-9">
          <option key="0" value="1">SLES11 SP4 Pool x86_64</option>
          <option key="1" value="2">SLES12 SP3 Pool x86_64</option>
        </Select>
      </div>
      <div className="row">
        <div className="form-group">
          <label className="col-lg-3 control-label">Child Channels:</label>
          <div className="col-lg-9">
            <input type="checkbox" />&nbsp;&nbsp;<label>SLES12 SP3 Updates x86_64</label>
            <br />
            <input type="checkbox" />&nbsp;&nbsp;<label>SLES12 SP3 Updates x86_64</label>
            <br />
            <input type="checkbox" />&nbsp;&nbsp;<label>SLES12 SP3 Updates x86_64</label>
          </div>
        </div>
      </div>
    </React.Fragment>
  )
}

const Sources = (props: SourcesProps) => {

  return (
    <CreatorPanel
      id="sources"
      title="Sources"
      creatingText="Add new Source"
      panelLevel="2"
      collapsible
      customIconClass="fa-small"
      onSave={({closeDialog}) => {
        const randomSource = generateNewSource();
        props.onChange(addSource(props.sources, {...randomSource, state:  "0"}))
        closeDialog();
        showSuccessToastr(`${randomSource.name} added successfully`)
      }}
      renderCreationContent={() => <ModalSourceCreationContent source={{}} />}
      renderContent={() =>
        <div className="min-height-panel">
          {
            props.sources.map(source =>
              <div className="col-xs-3">
                <CreatorPanel
                  id={`source${source.id}`}
                  title={source.name}
                  creatingText="Edit"
                  panelLevel="4"
                  className={panelsStateClassName[source.state]}
                  onSave={({closeDialog}) => {
                    const nextState = source.state === "0" ? "0" : "2";
                    props.onChange(modifySource(props.sources, {...source, state:  nextState}))
                    closeDialog();
                    showSuccessToastr(`${source.name} edited successfully`)
                  }}
                  onDelete={({closeDialog}) => {
                    if(source.state === "0") {
                      props.onChange(deleteSource(props.sources, source))
                    } else {
                      props.onChange(modifySource(props.sources, {...source, state:  "3"}))
                    }
                    closeDialog();
                    showSuccessToastr(`${source.name} deleted successfully`)
                  }}
                  renderCreationContent={() => <ModalSourceCreationContent source={{}} />}
                  renderContent={() => (
                    <React.Fragment>
                      <dl className="row">
                        <dt className="col-xs-3">Type:</dt>
                        <dd className="col-xs-9">{source.type}</dd>
                      </dl>
                      <dl className="row">
                        <dt className="col-xs-3">Level:</dt>
                        <dd className="col-xs-9">{source.level}</dd>
                      </dl>
                    </React.Fragment>
                  )}/>
              </div>
            )
          }
        </div>
      }
    />
  )
}

export default Sources;
