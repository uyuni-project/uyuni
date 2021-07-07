module.exports = {
  // See https://eslint.org/docs/developer-guide/working-with-rules#rule-basics
  meta: {
    type: "problem",
    docs: {
      description: "Disallow using raw Javascript Date instances",
      category: "Possible Errors",
      recommended: true,
    },
    fixable: false,
    schema: [],
  },
  create: function(context) {
    return {
      Identifier: function(node) {
        if (node.name === "Date" && node.parent.type === "NewExpression") {
          const args = (node.parent.arguments || []).map(arg => arg.raw).filter(Boolean);
          context.report({
            node: node.parent,
            message: "Don't use raw Javascript Date instances",
            suggest: [
              {
                desc: "Use `moment()` instead",
                fix: function(fixer) {
                  return fixer.replaceText(node.parent, `moment(${args.join(", ")})`);
                },
              },
            ],
          });
        }
      },
    };
  },
};
