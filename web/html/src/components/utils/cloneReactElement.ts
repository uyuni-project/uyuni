import * as React from "react";

/**
 * When cloning an element, the element might be either a component that accepts custom props or a regular DOM node such
 * as a span etc which do not accept them. For the latter case, it isn't valid to pass those props through.
 *
 * Cloning can be useful if you want to use partial application of props where one abstraction layer adds one part of
 * the props and a different layer adds the rest.
 * Please use this sparingly since it makes refactoring and understanding intent by reading code considerably harder.
 */
export function cloneReactElement<P>(
  element: React.ReactNode | ((props: P) => JSX.Element),
  props?: P,
  ...children: React.ReactNode[]
) {
  const unwrappedProps = typeof element === "string" || typeof (element as any)?.type === "string" ? undefined : props;
  return React.isValidElement(element) ? React.cloneElement(element, unwrappedProps, ...children) : element;
}
