import { RawChannelType, ServerChannelType } from "core/channels/type/channels.type";
import State from "./channels-selection-state";
import { rawChannelsToDerivedChannels, derivedChannelsToRowDefinitions } from "./channels-selection-transforms";

let mockServerChannelId = 0;
const getMockServerChannel = (partial: Partial<ServerChannelType> = {}): ServerChannelType => {
  const id = partial.id ?? mockServerChannelId++;
  const mockServerChannel = {
    id,
    name: `mock ${id}`,
    label: `mock-${id}`,
    archLabel: "channel-x86_64",
    custom: true,
    isCloned: false,
    recommended: false,
    subscribable: false,
  };
  return Object.assign(mockServerChannel, partial);
};

describe("channels selection data transforms", () => {
  const PARENT_ID = 1000;
  const CHILD_A_ID = 1001;
  const CHILD_B_ID = 1002;

  const rawChannels: RawChannelType[] = [
    // A base channel with no children
    {
      base: getMockServerChannel(),
      children: [],
    },
    // A base channel where one of the children is required
    {
      base: getMockServerChannel({ id: PARENT_ID }),
      children: [
        // One child that is not required in any way
        getMockServerChannel(),
        // Two children that are in the requires map
        getMockServerChannel({ id: CHILD_A_ID }),
        getMockServerChannel({ id: CHILD_B_ID }),
      ],
    },
    // A base channel where one of the children is recommended
    { base: getMockServerChannel(), children: [getMockServerChannel({ recommended: true })] },
  ];
  // These maps include their own id in the server response
  const rawRequiresMap = {
    [PARENT_ID]: [],
    // This child requires the other child while the other one doesn't require this one
    [CHILD_A_ID]: [PARENT_ID, CHILD_A_ID, CHILD_B_ID],
    [CHILD_B_ID]: [PARENT_ID, CHILD_B_ID],
  };

  describe("rawChannelsToDerivedChannels", () => {
    test("works on empty data", () => {
      const { baseChannels, channelsMap, requiresMap, requiredByMap } = rawChannelsToDerivedChannels([], {});

      expect(baseChannels).toEqual([]);
      expect(channelsMap).toEqual(new Map());
      expect(requiresMap).toEqual(new Map());
      expect(requiredByMap).toEqual(new Map());
    });

    test("identifies base channels", () => {
      const { baseChannels } = rawChannelsToDerivedChannels(rawChannels, rawRequiresMap);
      expect(baseChannels.length).toEqual(rawChannels.length);
    });

    test("maps all channels", () => {
      const { channelsMap } = rawChannelsToDerivedChannels(rawChannels, rawRequiresMap);

      const channelsCount =
        rawChannels.length + rawChannels.reduce((total, rawChannel) => total + rawChannel.children.length, 0);
      expect(channelsMap.size).toEqual(channelsCount);
      expect(channelsMap.has(PARENT_ID)).toEqual(true);
      expect(channelsMap.has(CHILD_A_ID)).toEqual(true);
      expect(channelsMap.has(CHILD_B_ID)).toEqual(true);
    });

    test("identifies two-way requirements with no duplicates", () => {
      const { channelsMap, requiresMap, requiredByMap } = rawChannelsToDerivedChannels(rawChannels, rawRequiresMap);
      const parent = channelsMap.get(PARENT_ID);
      const childA = channelsMap.get(CHILD_A_ID);
      const childB = channelsMap.get(CHILD_B_ID);

      // The parent doesn't require anything
      expect(requiresMap.get(PARENT_ID)).toEqual(undefined);
      // This child requires both the parent and the other child
      expect(requiresMap.get(CHILD_A_ID)).toEqual(new Set([parent, childB]));
      // This child requires the parent
      expect(requiresMap.get(CHILD_B_ID)).toEqual(new Set([parent]));

      // Ensure the reverse mapping matches
      expect(requiredByMap.get(PARENT_ID)).toEqual(new Set([childA, childB]));
      expect(requiredByMap.get(CHILD_A_ID)).toEqual(undefined);
      expect(requiredByMap.get(CHILD_B_ID)).toEqual(new Set([childA]));
    });
  });

  describe("State", () => {
    // TODO: Test this separately
    const derivedChannels = rawChannelsToDerivedChannels(rawChannels, rawRequiresMap);

    test("works on empty data", () => {
      const rows = derivedChannelsToRowDefinitions([], new State());
      expect(rows).toEqual([]);
    });
  });

  // TODO: Move to a separate file
  describe("State", () => {
    test("renders nothing when no base selection has been made", () => {
      const state = new State();
      const result = state.resolveChange(rawChannelsToDerivedChannels(rawChannels, rawRequiresMap));
      expect(result).toEqual(undefined);
    });

    test("renders nothing when no channels are present", () => {
      const state = new State();
      const result = state.resolveChange({ selectedBaseChannelId: PARENT_ID });
      expect(result).toEqual(undefined);
    });

    test("yields a row for each base channel", () => {
      const state = new State();
      state.resolveChange(rawChannelsToDerivedChannels(rawChannels, rawRequiresMap));
      const result = state.resolveChange({ selectedBaseChannelId: PARENT_ID });
      expect(result?.rows.length).toBeGreaterThanOrEqual(rawChannels.length);
    });
  });
});
