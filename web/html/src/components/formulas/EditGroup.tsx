import { Component, Fragment } from "react";

import { productName } from "core/user-preferences";

import { SectionState } from "components/FormulaForm";
import { Panel } from "components/panels/Panel";
import { Highlight } from "components/table/Highlight";
import { DEPRECATED_onClick } from "components/utils";
import HelpIcon from "components/utils/HelpIcon";

import { Formulas, Utils } from "utils/functions";

import {
  ElementDefinition,
  generateFormulaComponent,
  generateFormulaComponentForId,
  isFiltered,
} from "./FormulaComponentGenerator";

const EditGroupSubtype = Formulas.EditGroupSubtype;
const getEditGroupSubtype = Formulas.getEditGroupSubtype;
const deepCopy = Utils.deepCopy;
// circular dependencies are bad
const getSafeCollapseId = (id: string) => id.replace(/[^a-zA-Z0-9_-]/g, "-");

type EditGroupProps = {
  id: string;
  key: any;
  disabled?: boolean;
  value: any;
  formulaForm: any;
  element: ElementDefinition;
  sectionsExpanded: SectionState;
  setSectionsExpanded: (SectionState) => void;
  isVisibleByCriteria?: () => boolean;
  criteria: string;
};

type EditGroupState = {
  visible: boolean;
};

/*
 * Base class for edit-group.
 * Based on the edit-group data, the corresponing shape of component is used.
 */
class EditGroup extends Component<EditGroupProps, EditGroupState> {
  constructor(props: EditGroupProps) {
    super(props);
    this.state = {
      visible: props.sectionsExpanded !== SectionState.Collapsed,
    };
  }

  componentDidUpdate(prevProps: Readonly<EditGroupProps>) {
    if (
      this.props.sectionsExpanded !== SectionState.Mixed &&
      this.props.sectionsExpanded !== prevProps.sectionsExpanded
    ) {
      this.setState({ visible: this.props.sectionsExpanded === SectionState.Expanded });
    }
  }

  isDisabled = () => {
    const formScope = this.props.formulaForm.props.scope;
    const elementScope = this.props.element.$scope;
    return (
      elementScope === "readonly" || (formScope !== elementScope && elementScope !== "system") || this.props.disabled
    );
  };

  handleAddItem = () => {
    if (this.props.element.$maxItems! <= this.props.value.length || this.isDisabled()) return;

    this.props.setSectionsExpanded(SectionState.Mixed);
    const newValueProps = this.props.value;
    const newValue = deepCopy(this.props.element.$newItemValue);

    newValueProps.push(newValue);

    this.props.formulaForm.handleChange({
      id: this.props.id,
      value: newValueProps,
    });
  };

  handleRemoveItem = (index: number) => {
    if (this.props.element.$minItems! >= this.props.value.length || this.isDisabled()) return;

    this.props.value.splice(index, 1);
    this.props.formulaForm.handleChange({
      id: this.props.id,
      value: this.props.value,
    });
  };

  isVisible = () => {
    return this.state.visible;
  };

  setVisible = (index, visible) => {
    // index not needed here
    this.setState({ visible: visible });
    this.props.setSectionsExpanded(SectionState.Mixed);
  };

  render() {
    const element = this.props.element;
    const subType = getEditGroupSubtype(element);

    let Component;
    if (subType === EditGroupSubtype.PRIMITIVE_LIST) {
      Component = EditPrimitiveGroup;
    } else if (subType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
      Component = EditPrimitiveDictionaryGroup;
    } else {
      Component = EditDictionaryGroup;
    }

    return this.props.isVisibleByCriteria?.() ? (
      <Panel
        key={this.props.id}
        headingLevel="h4"
        className="formula-content-section"
        collapseId={getSafeCollapseId(this.props.id)}
        collapsClose={!this.state.visible}
        header={
          <Highlight
            enabled={isFiltered(this.props.criteria)}
            text={this.props.element.$name}
            highlight={this.props.criteria}
          />
        }
      >
        <Fragment>
          {"$help" in this.props.element && this.props.element.$help !== this.props.element.$name ? (
            <p>{this.props.element.$help}</p>
          ) : null}
          <Component
            handleRemoveItem={this.handleRemoveItem}
            handleAddItem={this.handleAddItem}
            isDisabled={this.isDisabled()}
            id={this.props.id}
            key={this.props.key}
            element={this.props.element}
            value={this.props.value}
            sectionsExpanded={this.props.sectionsExpanded}
            setSectionsExpanded={this.props.setSectionsExpanded}
            formulaForm={this.props.formulaForm}
          />
          {getEditGroupSubtype(element) !== EditGroupSubtype.PRIMITIVE_LIST &&
            getEditGroupSubtype(element) !== EditGroupSubtype.PRIMITIVE_DICTIONARY && (
              <div className="form-group form-add-button">
                <div className="col-lg-3 "></div>
                <div className="col-lg-9 ">
                  <button
                    className="btn btn-default"
                    type="button"
                    id={this.props.id + "#add_item"}
                    data-bs-toggle="tooltip"
                    aria-label={
                      this.props.element.$maxItems! <= this.props.value.length
                        ? "Max number of items reached"
                        : undefined
                    }
                    title={
                      this.props.element.$maxItems! <= this.props.value.length
                        ? "Max number of items reached"
                        : undefined
                    }
                    disabled={this.props.element.$maxItems! <= this.props.value.length || this.props.disabled}
                    {...DEPRECATED_onClick(this.handleAddItem)}
                  >
                    Add {this.props.element.$name}
                  </button>
                </div>
              </div>
            )}
          {getEditGroupSubtype(element) === EditGroupSubtype.PRIMITIVE_DICTIONARY && (
            <div className="form-group">
              <div className="col-lg-3 "></div>
              <div className="col-lg-9 ">
                <hr className="mt-1 mb-3"></hr>
                <button
                  className="btn btn-default"
                  type="button"
                  id={this.props.id + "#add_item"}
                  data-bs-toggle="tooltip"
                  title={
                    this.props.element.$maxItems! <= this.props.value.length ? "Max number of items reached" : undefined
                  }
                  disabled={this.props.element.$maxItems! <= this.props.value.length || this.props.disabled}
                  {...DEPRECATED_onClick(this.handleAddItem)}
                >
                  Add {this.props.element.$name}
                </button>
              </div>
            </div>
          )}
        </Fragment>
      </Panel>
    ) : null;
  }
}

type EditPrimitiveGroupProps = {
  id: string;
  value: any;
  element: ElementDefinition;
  formulaForm: any;
  isDisabled?: boolean;
  handleRemoveItem: (...args: any[]) => any;
  handleAddItem?: (...args: any[]) => any;
};

/*
 * Used for rendering edit-groups in the form of "list of primitive types",
 * to be rendered as a list of simple form elements in the UI.
 */
class EditPrimitiveGroup extends Component<EditPrimitiveGroupProps> {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  simpleWrapper = (name, required, element, i, help = null, isLastItem = false) => {
    return (
      <Fragment>
        <div className="col-lg-4 offset-lg-3">{element}</div>
        {required ? (
          <span className="required-form-field" style={{ float: "left", paddingRight: "10px" }}>
            *
          </span>
        ) : null}
        <HelpIcon text={this.props.element["$help"]} />
        <div className="col-lg-3">
          {isLastItem ? (
            <button
              className="btn btn-default"
              data-bs-toggle="tooltip"
              type="button"
              title={
                this.props.element.$maxItems! <= this.props.value.length
                  ? "Max number of items reached"
                  : `Add ${this.props.element.$name}`
              }
              disabled={this.props.element.$maxItems! <= this.props.value.length || this.props.isDisabled}
              onClick={() => this.props.handleAddItem?.()}
            >
              <i className="fa fa-plus" />
            </button>
          ) : (
            <RemoveButton
              minItems={this.props.element.$minItems!}
              currentLength={this.props.value.length}
              handleRemoveItem={() => this.props.handleRemoveItem(i)}
            />
          )}
        </div>
      </Fragment>
    );
  };

  render() {
    const elements: React.ReactNode[] = [];
    const itemIndices: string[] = [];

    // Collect all valid indices (not "$meta")
    for (const i in this.props.value) {
      if (i !== "$meta") {
        itemIndices.push(i);
      }
    }

    const lastIndex = itemIndices.length - 1;

    for (const idx in itemIndices) {
      const i = itemIndices[idx];
      const isLastItem = parseInt(idx, 10) === lastIndex;
      const id = this.props.id + "#" + i;

      // Create a wrapper that passes isLastItem information
      const wrapperWithContext = (name, required, element, help = null) => {
        return this.simpleWrapper(name, required, element, i, help, isLastItem);
      };

      elements.push(
        <div className="form-group formula-primitive-group-item" id={id} key={id}>
          <div className="formula-primitive-group-item-content">
            {generateFormulaComponentForId(
              this.props.element.$prototype,
              this.props.value[i],
              this.props.formulaForm,
              id,
              wrapperWithContext,
              this.props.isDisabled
            )}
          </div>
        </div>
      );
    }
    return <div id={this.props.id + "$elements"}>{elements}</div>;
  }
}

type EditPrimitiveDictionaryGroupProps = {
  id: string;
  value: any;
  element: ElementDefinition;
  formulaForm: any;
  isDisabled?: boolean;
  handleRemoveItem: (...args: any[]) => any;
};

/*
 * Used for rendering edit-groups in the form of "dictionary of primitive types",
 * to be rendered as a list of [key, value] in the UI.
 */
class EditPrimitiveDictionaryGroup extends Component<EditPrimitiveDictionaryGroupProps> {
  pairElementWrapper(elementName) {
    return (name, required, element) => (
      <div className="d-inline-block me-3" key={elementName}>
        <label className="control-label me-3">
          {elementName}
          {required ? <span className="required-form-field"> *</span> : null}:
        </label>
        <div className="d-inline-block">{element}</div>
      </div>
    );
  }

  render() {
    const elements: React.ReactNode[] = [];

    for (const i in this.props.value) {
      if (i === "$meta") {
        continue;
      }
      const id = this.props.id + "#" + i;
      elements.push(
        <div className="d-flex align-items-center offset-lg-3 mb-3 gap-3" id={id} key={id}>
          {generateFormulaComponentForId(
            this.props.element.$prototype.$key,
            this.props.value[i][0],
            this.props.formulaForm,
            id + "#0",
            this.pairElementWrapper(this.props.element.$prototype.$key.$name),
            this.props.isDisabled
          )}
          {generateFormulaComponentForId(
            this.props.element.$prototype,
            this.props.value[i][1],
            this.props.formulaForm,
            id + "#1",
            this.pairElementWrapper(get(this.props.element.$prototype.$name, "Value")),
            this.props.isDisabled
          )}
          <RemoveButton
            minItems={this.props.element.$minItems!}
            currentLength={this.props.value.length}
            handleRemoveItem={() => this.props.handleRemoveItem(i)}
          />
        </div>
      );
    }
    return (
      <div id={this.props.id + "$elements"} className="pair-group">
        {elements}
      </div>
    );
  }
}

type RemoveButtonProps = {
  minItems: number;
  currentLength: number;
  handleRemoveItem: (...args: any[]) => any;
};

class RemoveButton extends Component<RemoveButtonProps> {
  render() {
    return (
      <button
        className="btn btn-tertiary"
        type="button"
        data-bs-toggle="tooltip"
        title={this.props.minItems >= this.props.currentLength ? "Min number of items reached" : "Remove item"}
        aria-label={this.props.minItems >= this.props.currentLength ? "Min number of items reached" : "Remove item"}
        onClick={() => this.props.handleRemoveItem()}
        disabled={this.props.minItems >= this.props.currentLength}
      >
        <i className="fa fa-times" />
      </button>
    );
  }
}

type EditDictionaryGroupProps = {
  id: string;
  element: ElementDefinition;
  value: any;
  isDisabled?: boolean;
  formulaForm: any;
  sectionsExpanded: SectionState;
  setSectionsExpanded: (SectionState) => void;
  handleRemoveItem: (...args: any[]) => any;
};

type EditDictionaryGroupState = {
  visibility: Map<string, boolean>;
};

/*
 * Used for rendering edit-groups that are backed up list of dictionaries
 * to be rendered as a list of key-value groups in the UI.
 */
class EditDictionaryGroup extends Component<EditDictionaryGroupProps, EditDictionaryGroupState> {
  constructor(props) {
    super(props);
    this.state = {
      visibility: new Map(),
    };
  }

  componentDidUpdate(prevProps: Readonly<EditDictionaryGroupProps>) {
    if (
      this.props.sectionsExpanded !== SectionState.Mixed &&
      this.props.sectionsExpanded !== prevProps.sectionsExpanded
    ) {
      this.setAllVisible(this.props.sectionsExpanded === SectionState.Expanded);
    }
  }

  wrapKeyGroup(element_name, required, innerHTML) {
    return (
      <div className="form-group" key={element_name}>
        <label className="col-lg-3 control-label">
          {element_name}
          <span className="required-form-field"> *</span>:
        </label>
        <div className="col-lg-6">{innerHTML}</div>
        <div className="col-lg-3">
          <HelpIcon
            text={t(
              "This field is used as a 'key' identifier in the resulting pillar data. It must be unique across all items in this group and should not be changed once set."
            )}
          />
        </div>
      </div>
    );
  }

  generateItemName(item_index) {
    let name = this.props.element.$itemName;
    name = name.replace(/\${i}/g, parseInt(item_index, 10) + 1);
    name = name.replace(/\${.*}/g, (txt) => get(this.props.value[item_index][txt.substring(2, txt.length - 1)], txt));
    name = name.replace(/\${productName}/g, productName);
    return name;
  }

  isVisible = (index) => {
    return this.state.visibility.get(index) === undefined || this.state.visibility.get(index) !== false;
  };

  setVisible = (index, visible) => {
    const { visibility } = this.state;
    visibility.set(index, visible);
    this.props.setSectionsExpanded(SectionState.Mixed);
    this.setState({ visibility });
  };

  setAllVisible(visible) {
    const { visibility } = this.state;
    for (const i in this.props.value) {
      visibility.set(i, visible);
      this.setState({ visibility });
    }
  }

  render() {
    const elements: React.ReactNode[] = [];
    for (const i in this.props.value) {
      if (i === "$meta") {
        continue;
      }
      const id = this.props.id + "#" + i;

      const item_elements: React.ReactNode[] = [];
      for (const element_name in this.props.element.$prototype) {
        if (element_name.startsWith("$") && element_name !== "$key") continue;
        item_elements.push(
          generateFormulaComponent(
            this.props.element.$prototype[element_name],
            this.props.value[i][element_name],
            this.props.formulaForm,
            id,
            element_name === "$key" ? this.wrapKeyGroup : undefined,
            this.props.isDisabled
          )
        );
      }

      elements.push(
        <Panel
          key={id}
          headingLevel="h4"
          className="formula-content-section"
          collapseId={getSafeCollapseId(id)}
          collapsClose={!this.isVisible(i)}
          header={
            <div className="group-heading">
              {this.props.element.$itemName
                ? this.generateItemName(i)
                : `${this.props.element.$name}-${parseInt(i, 10) + 1}`}
            </div>
          }
          buttons={
            <button
              className="btn btn-tertiary"
              data-bs-toggle="tooltip"
              title={
                this.props.element.$minItems! >= this.props.value.length
                  ? "Min number of items reached"
                  : this.props.element.$itemName
                    ? `Remove ${this.generateItemName(i)}`
                    : `Remove ${this.props.element.$name}-${parseInt(i, 10) + 1}`
              }
              disabled={this.props.element.$minItems! >= this.props.value.length || this.props.isDisabled}
              {...DEPRECATED_onClick(() => this.props.handleRemoveItem(i))}
            >
              <i className="fa fa-trash" />
            </button>
          }
        >
          <div className="group-section"> {item_elements}</div>
        </Panel>
      );
    }
    return <div id={this.props.id + "$elements"}>{elements}</div>;
  }
}

function get<T>(value?: T, default_value?: T) {
  if (value === undefined) return default_value;
  return value;
}

export default EditGroup;
