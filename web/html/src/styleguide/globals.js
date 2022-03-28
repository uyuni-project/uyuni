// See web/html/src/utils/test-utils/setup/t.ts
function t(template, ...substitutions) {
  return (template || "").replaceAll(/{(\d)}/g, (_, match) => substitutions[match]);
}
window.t = t;
