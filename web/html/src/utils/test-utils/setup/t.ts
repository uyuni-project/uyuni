/** Mimic the translation function `t()`, this doesn't match the full functionality but should suffice for tests */
export default function t(template: string, ...substitutions: any[]): string;
export default function t(template: JSX.Element, ...substitutions: any[]): never;
export default function t(template: string | JSX.Element = "", ...substitutions: any[]): string {
  if (typeof template !== "string") {
    throw new TypeError("Template translations are not currently supported in tests");
  }
  return template.replaceAll(/{(\d)}/g, (_, match: string) => substitutions[match]);
}
