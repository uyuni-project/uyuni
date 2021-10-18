import * as React from "react";
import { useState, useRef } from "react";
import { Panel } from "components/panels/Panel";
import { AsyncButton, Button } from "components/buttons";
import { withErrorMessages } from "../shared/api/use-clusters-api";
import { Label } from "components/input/Label";

import { MessageType } from "components/messages";
import { ErrorMessagesType } from "../shared/api/use-clusters-api";

type Props = {
  panel: React.ReactNode;
  onAdd: (name: string, label: string, description: string) => Promise<any>;
  onPrev: () => void;
  setMessages: (arg0: Array<MessageType>) => void;
};

const FinishAddCluster = (props: Props) => {
  const [name, setName] = useState<string>("");
  const [label, setLabel] = useState<string>("");
  const [description, setDescription] = useState<string>("");
  const [submitted, setSubmitted] = useState<boolean>(false);
  const theForm = useRef<HTMLFormElement>(null);

  const onAdd = () => {
    const isValid = theForm.current?.reportValidity() || false;
    if (isValid) {
      return props
        .onAdd(name, label, description)
        .then((data) => {
          setSubmitted(true);
          window.location.href = `/rhn/manager/cluster/${data}`;
        })
        .catch((err: ErrorMessagesType) => {
          console.log(err);
          props.setMessages(err.messages);
          throw err;
        });
    } else {
      console.log("form is not valid");
    }
  };

  return (
    <Panel
      headingLevel="h4"
      title={t("Add cluster")}
      footer={
        <div className="btn-group">
          <Button
            id="btn-prev"
            disabled={submitted}
            text={t("Back")}
            className="btn-default"
            icon="fa-arrow-left"
            handler={() => props.onPrev()}
          />
          <AsyncButton
            id="btn-next"
            disabled={submitted}
            text={t("Add")}
            defaultType="btn-success"
            icon="fa-plus"
            action={onAdd}
          />
        </div>
      }
    >
      <form
        id="formula-form"
        ref={theForm}
        className="form-horizontal"
        onSubmit={(event) => {
          event.preventDefault();
          return false;
        }}
      >
        <div className="form-horizontal">
          <div className="form-group">
            <Label required={true} className="col-md-3" name={t("Name:")} />
            <div className="col-md-3">
              <input
                className="form-control"
                type="text"
                value={name}
                onChange={(ev) => setName(ev.target.value)}
                required
              />
            </div>
          </div>
          <div className="form-group">
            <Label required={true} className="col-md-3" name={t("Label:")} />
            <div className="col-md-3">
              <input
                className="form-control"
                type="text"
                value={label}
                onChange={(ev) => setLabel(ev.target.value)}
                required
                pattern="^[a-zA-Z_][a-zA-Z0-9_-]+$"
                title="letters and numbers, optionally underscore or hyphen, must start with a letter or underscore"
              />
            </div>
          </div>
          <div className="form-group">
            <Label required={true} className="col-md-3" name={t("Description:")} />
            <div className="col-md-3">
              <input
                className="form-control"
                type="text"
                value={description}
                onChange={(ev) => setDescription(ev.target.value)}
                required
              />
            </div>
          </div>
        </div>
      </form>
      {props.panel}
    </Panel>
  );
};

export default withErrorMessages(FinishAddCluster);
