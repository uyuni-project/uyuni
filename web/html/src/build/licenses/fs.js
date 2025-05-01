const fs = require("fs").promises;
const { constants, createReadStream } = require("fs");
const { createHash } = require("crypto");

const fileExists = async (path) => {
  try {
    await fs.access(path, constants.F_OK);
    return true;
  } catch {
    return false;
  }
};

async function getFileHash(path) {
  return new Promise((resolve, reject) => {
    const hash = createHash("sha1");
    const stream = createReadStream(path);

    stream.on("data", (chunk) => hash.update(chunk));
    stream.on("end", () => resolve(hash.digest("hex")));
    stream.on("error", reject);
  });
}

module.exports = {
  fileExists,
  getFileHash,
};
