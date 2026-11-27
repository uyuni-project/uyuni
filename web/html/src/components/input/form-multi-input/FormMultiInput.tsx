/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { type ReactNode, useContext } from "react";

import { Button } from "components/buttons";
import { Panel } from "components/panels/Panel";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { FormContext } from "../form/Form";

/**
 * Layout of the items:
 * - `singleLine`: each item on one line, with the remove button at the end of the line
 * - `group`: the fields of each item stacked vertically, with the remove button in the
 *   top-right corner and a divider between items
 * - `panel`: each item in its own Panel, with the remove button in the Panel heading
 */
export type FormMultiInputVariant = "singleLine" | "group" | "panel";

type Props = {
  /** Id of the component */
  id: string;

  /** Title of the panel for the list of fields */
  title: string;

  /** String starting the field names in the model */
  prefix: string;

  /**
   * Callback function adding fields to the model.
   * It takes one parameter for the index of the new row.
   */
  onAdd: (newIdx: number) => void;

  /**
   * Callback function removing fields to the model.
   * It takes one parameter for the index of the row to delete in the model.
   */
  onRemove: (idx: number) => void;

  /**
   * A function that renders the fields of one row given it's index.
   * The index parameter should be used for the field names.
   */
  children: (index: number) => ReactNode;

  /** Whether the fields are enabled or not. */
  disabled?: boolean;

  /** Icon to display in the Panel of each item, only used by the `panel` variant */
  panelIcon?: (idx: number) => string;

  /** Title to display in the Panel of each item, only used by the `panel` variant */
  panelTitle?: (idx: number) => string;

  /** Content to display between the title and the first fields */
  header?: ReactNode;

  /** CSS class for the element containing the fields of one item, used by the `singleLine` and `group` variants */
  rowClass?: string;

  /** HTML tag name containing the panel header text */
  panelHeading?: keyof JSX.IntrinsicElements;

  /** CSS class for the wrapping Panel */
  panelClassName?: string;

  /** Layout of the items */
  variant: FormMultiInputVariant;
};

/**
 * Compute the list of item keys in the model based of fields named like `${prefix}${idx}_${name}`
 */
export function getOrderedItemsFromModel(model: any, prefix: string): number[] {
  if (typeof model === "undefined" || model === null) {
    return [];
  }
  return Object.keys(model)
    .map((property) => {
      const result = property.match(new RegExp(`^${prefix}([0-9]+)`));
      if (!DEPRECATED_unsafeEquals(result, null)) {
        return Number.parseInt(result[1], 10);
      }
      return -1;
    }) // only return one of each matching properties
    .filter((property, index, array) => property >= 0 && index === array.indexOf(property))
    .sort();
}

/**
 * Component handling list of items in forms. This component takes a function rendering the fields of one row
 * as child. This function is given a number representing the item index as parameter.
 *
 * For the component to recognize the fields it handles, those need to have a prefixed name. For instance if
 * this is reprenting a list of remote hosts, the prefix could be 'hosts'.
 * The fields model would be following this scheme: `<prefix><index>_somename`
 *
 * Thus for the hosts example, the fields names would be: `hosts0_name`, `hosts0_port`. The children function for
 * this example could look like this:
 *
 * ```js
 * (index) = (
 *   <>
 *     <Text name={`hosts${index}_host`} label={t('Hostname')} />
 *     <Text name={`hosts${index}_port`} label={t('Port')} />
 *   </>
 * )
 * ```
 *
 * Just like other uses of the input fields, the model is automatically updated when the value changes.
 *
 * In order to help converting from a tree-like model to a flat one for use with this component and back,
 * see the `components/intput/form-utils` `flattenModel()` and `unflattenModel()` functions.
 *
 * When the user clicks the ''add'' or ''remove'' button, the `onAdd` or `onRemove` callback functions are called.
 * Those are the ones changing the model to add the new fields. Here is how to add the fields for our hosts example:
 *
 * ```js
 * onAdd={(index: number) => {
 *   const newProperties = {
 *     [`hosts${index}_name`]: '',
 *     [`hosts${index}_port`]: '',
 *   };
 *   setModel(Object.assign({}, model, newProperties));
 * }}
 * ```
 *
 * And how to remove them:
 *
 * ```js
 * onRemove={(index: number) => {
 *   setModel(Object.entries(model).reduce((res, entry) => {
 *     const property = !entry[0].startsWith(`hosts${index}_`)
 *      ? { [entry[0]]: entry[1] }
 *      : undefined;
 *     return Object.assign(res, property);
 *   }, {}));
 * }}
 * ```
 */
export function FormMultiInput({ disabled = false, variant, ...props }: Props) {
  const formContext = useContext(FormContext);
  const items = getOrderedItemsFromModel(formContext.model, props.prefix);
  const new_index = (items.length > 0 && items[items.length - 1] + 1) || 0;
  return (
    <Panel
      key={props.id}
      title={props.title}
      headingLevel={props.panelHeading || "h2"}
      buttons={
        <Button
          icon="fa-plus"
          title={t(`Add ${props.title}`)}
          id={`add_${props.prefix}`}
          className="btn-default btn-sm"
          handler={() => props.onAdd(new_index)}
        />
      }
      className={props.panelClassName}
    >
      {props.header}
      {items.map((index) => {
        const removeButton = (
          <Button
            icon="fa-trash"
            title={t("Remove")}
            id={`remove_${props.prefix}${index}`}
            className="btn-tertiary btn-sm"
            handler={() => props.onRemove(index)}
            disabled={disabled}
          />
        );
        const children = props.children(index);
        if (variant === "singleLine") {
          return (
            <div
              key={`${props.prefix}${index}-singleline`}
              className={`d-flex align-items-start gap-2 multi-field-singleline ${props.rowClass ?? ""}`}
            >
              <div className="flex-grow-1">{children}</div>
              {removeButton}
            </div>
          );
        }
        if (variant === "group") {
          const isLast = index === items[items.length - 1];
          return (
            <div
              key={`${props.prefix}${index}-group`}
              className={`multi-field-group ${isLast ? "" : "border-bottom mb-3"} pb-3 ${props.rowClass ?? ""}`}
            >
              <div className="d-flex justify-content-end mb-2">{removeButton}</div>
              {children}
            </div>
          );
        }
        if (variant === "panel") {
          if (!DEPRECATED_unsafeEquals(props.panelTitle, null) || !DEPRECATED_unsafeEquals(props.panelIcon, null)) {
            const icon = !DEPRECATED_unsafeEquals(props.panelIcon, null) ? props.panelIcon(index) : null;
            const title = !DEPRECATED_unsafeEquals(props.panelTitle, null) ? props.panelTitle(index) : null;
            return (
              <Panel key={`${props.prefix}${index}`} icon={icon} title={title} headingLevel="h3" buttons={removeButton}>
                {children}
              </Panel>
            );
          }
          return (
            <Panel key={`${props.prefix}${index}`} title={`Item ${index + 1}`} headingLevel="h3" buttons={removeButton}>
              {children}
            </Panel>
          );
        }
      })}
    </Panel>
  );
}
