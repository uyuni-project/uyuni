const { exec } = require("child_process");

const getDependencies = async () => {
  return new Promise((resolve, reject) => {
    exec("yarn list --production --ignore-optional --json", async (error, stdout) => {
      if (error) {
        reject(error);
        return;
      }

      const tree = stdout
        .split("\n")
        .filter(Boolean)
        .map((line) => JSON.parse(line))
        .find((line) => line.type === "tree");

      if (!tree) {
        reject(new TypeError("No tree data found"));
        return;
      }

      const dependencies = [];

      const walk = (node) => {
        if (node.shadow) {
          return;
        }

        dependencies.push(node.name);
        if (node.children) {
          node.children.forEach((item) => {
            if (item.shadow) {
              return;
            }

            walk(item);
          });
        }
      };
      tree.data.trees.forEach((item) => {
        if (item.shadow) {
          return;
        }
        walk(item);
      });

      resolve(dependencies);
    });
  });
};

module.exports = { getDependencies };
