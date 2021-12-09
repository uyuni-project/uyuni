import * as React from "react";
import Network from "utils/network";
import { Utils } from "utils/functions";
import { Button } from "components/buttons";
import { Messages, MessageType } from "components/messages";
import {
  FormulaFormContext,
  FormulaFormContextProvider,
  FormulaFormRenderer,
  text,
  get,
} from "./formulas/FormulaComponentGenerator";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

const capitalize = Utils.capitalize;

const defaultMessageTexts = {
  pillar_only_formula_saved: <p>{t("Formula saved. Applying the highstate is not needed for this formula.")}</p>,
};

type Props = {
  /** URL to get the server data */
  dataUrl: string;

  /** Externally provided data instead of a URL to get data */
  getDataPromise?: () => Promise<any>;

  /** URL to save the data, data is sent as post request */
  saveUrl: string;

  /** Function to add the formula nav bar */
  addFormulaNavBar?: (formulaList: any, activeFormulaId: number) => void;

  /** The id of the formula to be shown */
  formulaId: number;

  /** The id of the system to be shown */
  systemId: number;

  /** Function that returns the URL to a formula page by id (used for prev/next buttons) */
  getFormulaUrl: (formulaId: number) => any;

  /** current active scope (system or group) */
  scope: "system" | "group";

  messageTexts: Record<string, any>;
};

type State = {
  formulaName: string;
  formulaList: any[];
  formulaRawLayout: any;
  systemData: any;
  groupData: any;
  formulaChanged: boolean;
  metadata?: any;
  formulaMetadata?: any;
  messages: string[];
  warnings: string[];
  errors: string[];
};

class FormulaForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      formulaName: "",
      formulaList: [],
      formulaRawLayout: {},
      systemData: {},
      groupData: {},
      formulaChanged: false,
      messages: [],
      warnings: [],
      errors: [],
    };

    window.addEventListener(
      "beforeunload",
      function (this: FormulaForm, e) {
        if (!this.state.formulaChanged) return null;

        let confirmationMessage = "You have unsaved changes. If you leave before saving, your changes will be lost.";

        get(e, window.event).returnValue = confirmationMessage; //Gecko + IE
        return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
      }.bind(this)
    );

    this.init();
  }

  init = () => {
    var dataPromise;
    if (this.props.getDataPromise) {
      dataPromise = this.props.getDataPromise();
    } else {
      dataPromise = Network.get(this.props.dataUrl);
    }

    dataPromise.then((data) => {
      if (data === null)
        this.setState({
          formulaName: "",
          formulaList: [],
          formulaRawLayout: {},
          systemData: {},
          groupData: {},
          formulaChanged: false,
          metadata: {},
        });
      else {
        if (
          data.formula_list.filter(
            (formula) => formula !== "caasp-management-settings" && DEPRECATED_unsafeEquals(formula, data.formula_name)
          ).length > 1
        ) {
          this.state.warnings.push(
            t(
              'Multiple Group formulas detected. Only one formula for "{0}" can be used on each system!',
              capitalize(data.formula_name)
            )
          );
        }
        const rawLayout = data.layout;
        this.setState({
          formulaName: data.formula_name,
          formulaList: data.formula_list,
          formulaRawLayout: rawLayout,
          systemData: get(data.system_data, {}),
          groupData: get(data.group_data, {}),
          formulaChanged: false,
          formulaMetadata: data.metadata,
        });
      }
    });
  };

  saveFormula = (data) => {
    this.setState({ formulaChanged: false });
    let scope = this.props.scope;
    let formType = scope.toUpperCase();
    if (formType === "SYSTEM") {
      formType = "SERVER";
    }

    if (data.errors) {
      const messages: string[] = [];
      if (data.errors.required && data.errors.required.length > 0) {
        messages.push(t("Please input required fields: {0}", data.errors.required.join(", ")));
      }
      if (data.errors.invalid && data.errors.invalid.length > 0) {
        messages.push(t("Invalid format of fields: {0}", data.errors.invalid.join(", ")));
      }
      this.setState({
        messages: [],
        errors: messages,
      });
    } else {
      let formData = {
        type: formType,
        id: this.props.systemId,
        formula_name: this.state.formulaName,
        content: data.values,
      };

      Network.post(this.props.saveUrl, formData).then(
        function (this: FormulaForm, data) {
          if (data instanceof Array) {
            this.setState({ messages: data.map((msg) => this.getMessageText(msg)), errors: [] });
          }
        }.bind(this),
        function (this: FormulaForm, error) {
          try {
            this.setState({
              errors: [JSON.parse(error.responseText)],
            });
          } catch (e) {
            this.setState({
              errors: Network.errorMessageByStatus(error.status),
            });
          }
        }.bind(this)
      );
      window.scrollTo(0, 0);
    }
  };

  getMessageText = (msg) => {
    if (!this.props.messageTexts[msg] && defaultMessageTexts[msg]) {
      return t(defaultMessageTexts[msg]);
    }
    /**
     * TODO: There is a bug here, in some uses messageTexts can also contain elements,
     * a-la in group-formula.renderer.tsx. This case is not accounted for and needs
     * separate consideration.
     */
    return this.props.messageTexts[msg] ? t(this.props.messageTexts[msg]) : msg;
  };

  render() {
    const defaultMessage = (
      <p>{t("On this page you can configure Salt Formulas to automatically install and configure software.")}</p>
    );

    let messageItems: MessageType[] = this.state.messages.map((msg) => {
      return { severity: "info", text: msg };
    });
    messageItems = messageItems.concat(
      this.state.errors.map((msg) => {
        return { severity: "error", text: msg };
      })
    );
    messageItems = messageItems.concat(
      this.state.warnings.map((msg) => {
        return { severity: "warning", text: msg };
      })
    );
    const messages = <Messages items={messageItems} />;

    if (
      this.state.formulaRawLayout === undefined ||
      this.state.formulaRawLayout === null ||
      jQuery.isEmptyObject(this.state.formulaRawLayout)
    ) {
      if (this.props.addFormulaNavBar !== undefined)
        this.props.addFormulaNavBar(get(this.state.formulaList, ["Not found"]), this.props.formulaId);
      return (
        <div>
          {defaultMessage}
          {messages}
          <div className="panel panel-default">
            <div className="panel-heading">
              <h4>Error while loading form!</h4>
            </div>
            <div className="panel-body">
              The requested form could not get loaded! The corresponding formula either does not exist or has no valid
              layout file.
            </div>
          </div>
        </div>
      );
    } else {
      if (this.props.addFormulaNavBar !== undefined) {
        this.props.addFormulaNavBar(this.state.formulaList, this.props.formulaId);
      }
      const nextHref = this.props.getFormulaUrl(this.props.formulaId + 1);
      const prevHref = this.props.getFormulaUrl(this.props.formulaId - 1);
      return (
        <FormulaFormContextProvider
          layout={this.state.formulaRawLayout}
          systemData={this.state.systemData}
          groupData={this.state.groupData}
          scope={this.props.scope}
        >
          <div>
            {defaultMessage}
            {messages}
            <div className="form-horizontal">
              <SectionToolbar>
                <div className="btn-group">
                  <button
                    id="prev-btn"
                    type="button"
                    onClick={() => (window.location.href = prevHref)}
                    disabled={this.props.formulaId === 0}
                    className="btn btn-default"
                  >
                    <i className="fa fa-arrow-left" /> Prev
                  </button>
                  <button
                    id="next-btn"
                    type="button"
                    onClick={() => (window.location.href = nextHref)}
                    disabled={this.props.formulaId >= this.state.formulaList.length - 1}
                    className="btn btn-default"
                  >
                    Next <i className="fa fa-arrow-right fa-right" />
                  </button>
                </div>
                <div className="action-button-wrapper">
                  <FormulaFormContext.Consumer>
                    {({ validate, clearValues }: { validate: any; clearValues: any }) => (
                      <div className="btn-group">
                        <Button
                          id="save-btn"
                          icon="fa-floppy-o"
                          text="Save Formula"
                          className={"btn btn-success"}
                          handler={() => this.saveFormula(validate?.())}
                        />
                        <Button
                          id="reset-btn"
                          icon="fa-eraser"
                          text="Clear values"
                          className="btn btn-default"
                          handler={() =>
                            clearValues?.(() => window.confirm("Are you sure you want to clear all values?"))
                          }
                        />
                      </div>
                    )}
                  </FormulaFormContext.Consumer>
                </div>
              </SectionToolbar>
              <div className="panel panel-default">
                <div className="panel-heading">
                  <h3>{capitalize(get(this.state.formulaName, "Unnamed"))}</h3>
                </div>
                <div className="panel-body">
                  <div className="formula-content">
                    <p>{text(this.state.formulaMetadata.description)}</p>
                    <hr />
                    <FormulaFormRenderer />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </FormulaFormContextProvider>
      );
    }
  }
}

export default FormulaForm;
