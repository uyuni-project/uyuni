/** Mimic the translation function `t()`, this doesn't match the full functionality but should suffice for tests */
export default function t(template: string = "", ...substitutions: any[]): string {
  return template.replaceAll(/{(\d)}/g, (_, match: string) => substitutions[match]);
}
