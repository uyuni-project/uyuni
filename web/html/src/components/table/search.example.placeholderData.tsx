export type PlaceholderRow = {
  id: string;
};

export const getPlaceholderDataWithSearch = async (criteria: string) => {
  Loggerhead.debug(`criteria: "${criteria}"`);
  return new Promise<PlaceholderRow[]>((resolve) => {
    window.setTimeout(() => {
      // This is just placeholder data to demo the functionality, actually you would be doing the filtering on the server
      const placeholderData = new Array(100)
        .fill(null)
        .map((_, index) => ({ id: index.toString() }))
        .filter((item) => item.id.includes(criteria));
      resolve(placeholderData);
    }, 150);
  });
};
