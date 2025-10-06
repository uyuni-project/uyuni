import { createHash } from "crypto";
import { createReadStream, promises as fs } from "node:fs";

/**
 * Check if a given license file exists and does not have Git conflicts
 */
export const isValidLicenseFile = async (path) => {
  try {
    const content = await fs.readFile(path, "utf8");
    // This is a shortcut to keep build times low, if this ever causes a bug, add a check for the "=======" and ">>>>>>>" markers too
    const hasConflict = content.includes("<<<<<<<");
    return !hasConflict;
  } catch {
    return false;
  }
};

export const getFileHash = async (path) => {
  return new Promise((resolve, reject) => {
    const hash = createHash("sha1");
    const stream = createReadStream(path);

    stream.on("data", (chunk) => hash.update(chunk));
    stream.on("end", () => resolve(hash.digest("hex")));
    stream.on("error", reject);
  });
};
