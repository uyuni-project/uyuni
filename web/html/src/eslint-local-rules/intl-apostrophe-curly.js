module.exports = {
  // See https://eslint.org/docs/developer-guide/working-with-rules#rule-basics
  meta: {
    type: "problem",
    docs: {
      description:
        "A single apostrophe followed by a curly brace will not work as variable interpolation, a single apostrophe is used as the escape characted by the message syntax.",
      category: "Possible Errors",
      recommended: true,
    },
    fixable: false,
    schema: [],
  },
  create: function (context) {
    return {
      // See https://eslint.org/docs/latest/extend/selectors
      "CallExpression[callee.name='t'] > Literal": function (node) {
        const value = node.value;
        if (typeof value !== "string") {
          return;
        }

        // Match a single apostrophe followed by a curly, ignore double apostrophe followed by a curly
        // See https://formatjs.io/docs/core-concepts/icu-syntax/#quoting--escaping
        const matches = Array.from(value.matchAll(/[^']'{/g));
        matches.forEach((match) => {
          context.report({
            node: node,
            message: `This curly brace will be inserted verbatim and no variable interpolation will occur. A single apostrophe is used as the escape characters in the message syntax, similar to a backslash in Bash. See https://formatjs.io/docs/core-concepts/icu-syntax/#quoting--escaping`,
            loc: {
              start: {
                line: node.loc.start.line,
                column: node.loc.start.column + match.index + 2,
              },
              end: {
                line: node.loc.start.line,
                column: node.loc.start.column + match.index + 4,
              },
            },
          });
        });
      },
    };
  },
};
