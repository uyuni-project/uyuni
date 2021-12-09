import * as React from "react";
import { useEffect, useState } from "react";
import useClustersApi from "../shared/api/use-clusters-api";
import { Messages } from "components/messages";
import { Loading } from "components/utils/Loading";
import {
  FormulaFormContext,
  FormulaFormContextProvider,
  FormulaFormRenderer,
} from "components/formulas/FormulaComponentGenerator";
import { Panel } from "components/panels/Panel";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Button } from "components/buttons";

import { ClusterType, ErrorMessagesType, FormulaValuesType } from "../shared/api/use-clusters-api";
import { MessageType } from "components/messages";

type Props = {
  cluster: ClusterType;
  setMessages: (arg0: Array<MessageType>) => void;
  hasEditingPermissions: boolean;
};

const ManagementSettings = (props: Props) => {
  const [form, setForm] = useState<any>(null);
  const [values, setValues] = useState<FormulaValuesType | null | undefined>(null);
  const { fetchProviderFormulaForm, fetchClusterFormulaData, saveClusterFormulaData } = useClustersApi();

  useEffect(() => {
    fetchProviderFormulaForm(props.cluster.provider.label, "settings")
      .then((data) => {
        setForm(data.form);
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      });

    fetchClusterFormulaData(props.cluster.id, "settings")
      .then((data) => {
        setValues(data);
      })
      .catch((error: ErrorMessagesType) => {
        props.setMessages(error.messages);
      });
  }, []);

  // TODO make save async
  const save = ({ errors, values }) => {
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
      saveClusterFormulaData(props.cluster.id, "settings", values)
        .then((data) => {
          props.setMessages([Messages.success(t("Settings saved successfully"))]);
        })
        .catch((error: ErrorMessagesType) => {
          props.setMessages(error.messages);
        });
    }
  };

  return form && values ? (
    <React.Fragment>
      <FormulaFormContextProvider layout={form} systemData={values} groupData={{}} scope="system">
        {props.hasEditingPermissions && (
          <SectionToolbar>
            <div className="action-button-wrapper">
              <div className="btn-group">
                <FormulaFormContext.Consumer>
                  {({ validate, clearValues }: { validate: any; clearValues: any }) => (
                    <React.Fragment>
                      <Button
                        id="btn-save"
                        icon="fa-floppy-o"
                        text={t("Save")}
                        className="btn-success"
                        handler={() => {
                          save(validate?.());
                        }}
                      />
                      <Button
                        id="reset-btn"
                        icon="fa-eraser"
                        text="Clear values"
                        className="btn-default"
                        handler={() =>
                          clearValues?.(() => window.confirm("Are you sure you want to clear all values?"))
                        }
                      />
                    </React.Fragment>
                  )}
                </FormulaFormContext.Consumer>
              </div>
            </div>
          </SectionToolbar>
        )}
        <Panel headingLevel="h3" title={t("Provider Settings")}>
          <FormulaFormRenderer />
        </Panel>
      </FormulaFormContextProvider>
    </React.Fragment>
  ) : (
    <Loading />
  );
};

export default ManagementSettings;
