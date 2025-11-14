import { BaseChannelType, ChannelTreeType, ChannelType, ChildChannelType } from "core/channels/type/channels.type";

import { ActionChainLink, ActionLink } from "components/links";
import { MessageType, Utils as MessagesUtils } from "components/messages/messages";

import Network from "utils/network";

import { MigrationScheduleRequest } from "./types";

/**
 * Process the channels and the mandatory data to extract the information needed by the frontend logic
 * @param baseChannelTrees the array of channel trees
 * @param mandatoryMap a record mapping the requirements for each channel
 * @returns an object containing:
 *  - channelTrees, a clone of the baseChannelTree with all the fields of {@link BaseChannelType} and {@link ChildChannelType} populated
 *  - channelsMap, a map of {@link ChannelType} where the key is the channel id
 *  - baseChannels, an array containing all the base channels
 *  - requiresMap a mapping detailing for each channel id which other channels it requries
 *  - requiredByMap a mapping detailing for each channel id, which other channels requires it
 */
function processChannelData(
  baseChannelTrees: ChannelTreeType[],
  mandatoryMap: [number, number[]][],
  reversedMandatoryMap: [number, number[]][]
) {
  const channelsMap: Map<number, ChannelType> = new Map();
  const baseChannels: BaseChannelType[] = [];

  // Process the base channel trees
  const channelTrees: ChannelTreeType[] = baseChannelTrees.map((channelTree) => {
    const base = channelTree.base;

    // Add to the channels map
    channelsMap.set(base.id, base);
    baseChannels.push(base);

    const children = channelTree.children.map((child) => {
      // Add to the channels map
      channelsMap.set(child.id, child);

      return child;
    });

    return { base, children };
  });

  // Convert the Array<[number, number[]]> we receive from json to a Map<number, Set<ChannelType>>
  const requiresMap = arrayToMapWithSets(mandatoryMap, channelsMap);
  const requiredByMap = arrayToMapWithSets(reversedMandatoryMap, channelsMap);

  return { channelTrees, channelsMap, baseChannels, requiresMap, requiredByMap };
}

function arrayToMapWithSets(
  entries: [number, number[]][],
  channelsMap: Map<number, ChannelType>
): Map<number, Set<ChannelType>> {
  // Convert each entry's array of values into a Set
  const entriesWithSets: [number, Set<ChannelType>][] = entries.map(([key, values]) => {
    const channelSet = new Set<ChannelType>();
    values.forEach((channelId) => {
      const channel = channelsMap.get(channelId);
      if (channel !== undefined) {
        channelSet.add(channel);
      } else {
        Loggerhead.warn(`Cannot find channel with id ${channelId}. Ignoring.`);
      }
    });

    return [key, channelSet];
  });

  // Create the final Map from the new entries
  return new Map(entriesWithSets);
}

/**
 * Constructs a new ChannelTreeType instance from the base and its children.
 * @param baseChannel the base channel
 * @param children the children channels
 * @returns a new instance of {@link ChannelTreeType}
 */
function getAsChannelTree(baseChannel: BaseChannelType, children: Set<ChildChannelType>): ChannelTreeType {
  return {
    base: baseChannel,
    children: Array.from(children.values()),
  };
}

async function performMigration(migrationRequest: MigrationScheduleRequest): Promise<MessageType[]> {
  const response = await Network.post("/rhn/manager/api/systems/migration/schedule", migrationRequest);
  return MessagesUtils.info(
    migrationRequest.actionChain ? (
      <span>
        {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
          name: migrationRequest.actionChain,
          link: (str) => <ActionChainLink id={response.data}>{str}</ActionChainLink>,
        })}
      </span>
    ) : (
      <span>
        {t("The action has been <link>scheduled</link>.", {
          link: (str) => <ActionLink id={response.data}>{str}</ActionLink>,
        })}
      </span>
    )
  );
}

export const MigrationUtils = { processChannelData, getAsChannelTree, performMigration };
