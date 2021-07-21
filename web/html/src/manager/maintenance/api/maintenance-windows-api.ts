import Network from "utils/network";

/* Returns a list of maintenance schedules or calendars.
 * type == "schedule" for schedules
 * type == "calendar" for calendars
 */
function list(type: "schedule" | "calendar") {
  const endpoint = "/rhn/manager/api/maintenance/" + type + "/list";
  return Network.get(endpoint);
}

/* Returns a list of all calendarNames the user has access to */
function calendarNames() {
  const endpoint = "/rhn/manager/api/maintenance/calendar/names";
  return Network.get(endpoint);
}

/* Returns the details of a schedule or calendar with given id
 * type == "schedule" for schedules
 * type == "calendar" for calendars
 */
function details(id: string, type: "schedule" | "calendar") {
  const endpoint = "/rhn/manager/api/maintenance/" + type + "/" + id + "/details";
  return Network.get(endpoint);
}

export default {
  list,
  calendarNames,
  details,
};
