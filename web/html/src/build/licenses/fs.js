const fs = require("fs").promises;
const { createReadStream } = require("fs");
const { createHash } = require("crypto");

/**
 * Check if a given license file exists and does not have Git conflicts
 */
const isValidLicenseFile = async (path) => {
  try {
    const content = await fs.readFile(path, "utf8");
    // This is a shortcut to keep build times low, if this ever causes a bug, add a check for the "=======" and ">>>>>>>" markers too
    const hasConflict = content.includes("<<<<<<<");
    return !hasConflict;
  } catch {
    return false;
  }
};

const getFileHash = async (path) => {
  return new Promise((resolve, reject) => {
    const hash = createHash("sha1");
    const stream = createReadStream(path);

    stream.on("data", (chunk) => hash.update(chunk));
    stream.on("end", () => resolve(hash.digest("hex")));
    stream.on("error", reject);
  });
};

module.exports = {
  isValidLicenseFile,
  getFileHash,
};
