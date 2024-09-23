import * as generatedStories from "./stories.generated";

const storyGroups = Object.groupBy(Object.values(generatedStories), (item) => item.groupName);

export default Object.entries(storyGroups).map(([title, stories]) => ({ title, stories }));
