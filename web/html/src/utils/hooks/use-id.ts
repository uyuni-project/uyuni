// This module is obsolete once we update to React 18
import { useState } from "react";

// Be explicit that no tests should ever rely on these ids
const prefix = `generated-${Math.floor(Math.random() * 1000)}-`;
let counter = 0;
export const useId = () => {
  const [id] = useState(() => {
    const uniqueId = counter++;
    return `${prefix}${uniqueId}`;
  });
  return id;
};
