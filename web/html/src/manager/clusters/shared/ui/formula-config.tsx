import * as React from "react";
import { useEffect, useState } from "react";
import useClustersApi, { withErrorMessages } from "../api/use-clusters-api";
import { Panel } from "components/panels/Panel";
import { Button } from "components/buttons";
import { Messages } from "components/messages";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import {
  FormulaFormContext,
  FormulaFormContextProvider,
  FormulaFormRenderer,
} from "components/formulas/FormulaComponentGenerator";
import { Loading } from "components/utils/Loading";

import { FormulaValuesType, FormulaContextType, ErrorMessagesType } from "../api/use-clusters-api";
import { MessageType } from "components/messages";

// TODO move this to FormulaComponentGenerator once its flow-ified
// type ValidatedFormulaType = {
//   errors: {
//     required: Array<string>,
//     invalid: Array<string>
//   },
//   values: {[string]: any}
// }

type Props = {
  provider: string;
  title: string;
  values?: FormulaValuesType | null;
  formula: string;
  context?: FormulaContextType | null;
  onNext: (arg0: FormulaValuesType) => void;
  onPrev?: () => void;
  setMessages: (arg0: Array<MessageType>) => void;
};

const FormulaConfig = (props: Props) => {
  const [form, setForm] = useState<any>(null);
  const [formulaValues, setFormulaValues] = useState<FormulaValuesType | null | undefined>(null);
  const { fetchProviderFormulaForm } = useClustersApi();

  useEffect(() => {
    fetchProviderFormulaForm(props.provider, props.formula, props.context ? props.context : null)
      .then((data) => {
        setForm(data.form);
        if (data.params) {
          // merge params with props.values
          setFormulaValues(Object.assign({}, data.params, props.values)); // order is important
        } else if (props.values) {
          setFormulaValues(props.values);
        } else {
          setFormulaValues({});
        }
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      });
  }, []);

  const clickNext = ({ errors, values }) => {
    if (errors) {
      const messages: MessageType[] = [];
      if (errors.required && errors.required.length > 0) {
        messages.push(Messages.error(t("Please input required fields: {0}", errors.required.join(", "))));
      }
      if (errors.invalid && errors.invalid.length > 0) {
        messages.push(Messages.error(t("Invalid format of fields: {0}", errors.invalid.join(", "))));
      }
      props.setMessages(messages);
    } else {
      props.onNext(values);
    }
  };

  return form && formulaValues ? (
    <FormulaFormContextProvider
      layout={form}
      systemData={formulaValues ? formulaValues : {}}
      groupData={{}}
      scope="system"
    >
      <Panel
        headingLevel="h4"
        title={props.title}
        footer={
          <FormulaFormContext.Consumer>
            {({ validate }) => (
              <div className="btn-group">
                {props.onPrev ? (
                  <Button
                    id="btn-prev"
                    text={t("Back")}
                    className="btn-default"
                    icon="fa-arrow-left"
                    handler={props.onPrev}
                  />
                ) : null}
                <Button
                  id="btn-next"
                  icon="fa-arrow-right"
                  text={t("Next")}
                  className={"btn-success"}
                  handler={() => {
                    clickNext((validate as any)?.());
                  }}
                />
              </div>
            )}
          </FormulaFormContext.Consumer>
        }
      >
        <SectionToolbar>
          <div className="action-button-wrapper">
            <div className="btn-group">
              <FormulaFormContext.Consumer>
                {({ clearValues }) => (
                  <Button
                    id="reset-btn"
                    icon="fa-eraser"
                    text="Clear values"
                    className="btn btn-default"
                    handler={() =>
                      (clearValues as any)?.(() => window.confirm("Are you sure you want to clear all values?"))
                    }
                  />
                )}
              </FormulaFormContext.Consumer>
            </div>
          </div>
        </SectionToolbar>
        <div style={{ marginTop: "15px" }}>
          <FormulaFormRenderer />
        </div>
      </Panel>
    </FormulaFormContextProvider>
  ) : (
    <Panel
      headingLevel="h4"
      title={props.title}
      footer={
        <div className="btn-group">
          <Button
            id="btn-prev"
            text={t("Back")}
            className="btn-default"
            icon="fa-arrow-left"
            disabled={true}
            handler={() => {}}
          />
          <Button
            id="btn-next"
            icon="fa-arrow-right"
            text={t("Next")}
            className={"btn-success"}
            disabled={true}
            handler={() => {}}
          />
        </div>
      }
    >
      <Loading />
    </Panel>
  );
};

export default withErrorMessages(FormulaConfig);
