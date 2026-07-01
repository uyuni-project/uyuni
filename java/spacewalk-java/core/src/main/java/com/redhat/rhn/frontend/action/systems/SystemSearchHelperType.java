/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.action.systems;

import static com.redhat.rhn.frontend.action.systems.SystemSearchHelperIndex.HARDWARE_DEVICE_INDEX;
import static com.redhat.rhn.frontend.action.systems.SystemSearchHelperIndex.PACKAGES_INDEX;
import static com.redhat.rhn.frontend.action.systems.SystemSearchHelperIndex.SERVER_CUSTOM_INFO_INDEX;
import static com.redhat.rhn.frontend.action.systems.SystemSearchHelperIndex.SERVER_INDEX;
import static com.redhat.rhn.frontend.action.systems.SystemSearchHelperIndex.SNAPSHOT_TAG_INDEX;

import com.redhat.rhn.domain.Labeled;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.UnaryOperator;

public enum SystemSearchHelperType implements Labeled {

    NAME_AND_DESCRIPTION("systemsearch_name_and_description", SERVER_INDEX, buildPrefixQuery("name", "description")),
    ID("systemsearch_id", SERVER_INDEX, buildPrefixQuery("system_id")),
    CUSTOM_INFO("systemsearch_custom_info", SERVER_CUSTOM_INFO_INDEX, buildPrefixQuery("value")),
    SNAPSHOT_TAG("systemsearch_snapshot_tag", SNAPSHOT_TAG_INDEX, buildPrefixQuery("name")),
    CHECKIN("systemsearch_checkin", SERVER_INDEX, buildQuerySearchCheckin()),
    REGISTERED("systemsearch_registered", SERVER_INDEX, buildQuerySearchRegistered()),
    CPU_MODEL("systemsearch_cpu_model", SERVER_INDEX, buildPrefixQuery("cpuModel")),
    CPU_MHZ_LT("systemsearch_cpu_mhz_lt", SERVER_INDEX, buildQueryFrom0ToTerm("cpuMHz")),
    CPU_MHZ_GT("systemsearch_cpu_mhz_gt", SERVER_INDEX, buildQueryFromTermToLongMax("cpuMHz")),
    NUM_CPUS_LT("systemsearch_num_of_cpus_lt", SERVER_INDEX, buildQueryFrom0ToTerm("cpuNumberOfCpus")),
    NUM_CPUS_GT("systemsearch_num_of_cpus_gt", SERVER_INDEX, buildQueryFromTermToLongMax("cpuNumberOfCpus")),
    RAM_LT("systemsearch_ram_lt", SERVER_INDEX, buildQueryFrom0ToTerm("ram")),
    RAM_GT("systemsearch_ram_gt", SERVER_INDEX, buildQueryFromTermToLongMax("ram")),
    HW_DESCRIPTION("systemsearch_hwdevice_description", HARDWARE_DEVICE_INDEX, buildPrefixQuery("description")),
    HW_DRIVER("systemsearch_hwdevice_driver", HARDWARE_DEVICE_INDEX, buildPrefixQuery("driver")),
    HW_DEVICE_ID("systemsearch_hwdevice_device_id", HARDWARE_DEVICE_INDEX, buildPrefixQuery("deviceId")),
    HW_VENDOR_ID("systemsearch_hwdevice_vendor_id", HARDWARE_DEVICE_INDEX, buildPrefixQuery("vendorId")),
    DMI_SYSTEM("systemsearch_dmi_system", SERVER_INDEX, buildPrefixQuery("dmiSystem")),
    DMI_BIOS("systemsearch_dmi_bios", SERVER_INDEX,
            buildPrefixQuery("dmiBiosVendor", "dmiBiosVersion", "dmiBiosRelease")),
    DMI_ASSET("systemsearch_dmi_asset", SERVER_INDEX, buildPrefixQuery("dmiAsset")),
    HOSTNAME("systemsearch_hostname", SERVER_INDEX, buildPrefixQuery("hostname")),
    IP("systemsearch_ip", SERVER_INDEX, buildPrefixQuery("ipaddr")),
    IP6("systemsearch_ipv6", SERVER_INDEX, buildPrefixQuery("ip6addr")),
    INSTALLED_PACKAGES("systemsearch_installed_packages", PACKAGES_INDEX, (terms) -> "filename:(" + terms + "*)"),
    NEEDED_PACKAGES("systemsearch_needed_packages", PACKAGES_INDEX, buildPrefixQuery("name", "filename")),
    RUNNING_KERNEL("systemsearch_running_kernel", SERVER_INDEX, buildPrefixQuery("runningKernel")),
    LOC_COUNTRY_CODE("systemsearch_location_country_code", SERVER_INDEX, buildPrefixQuery("country")),
    LOC_STATE("systemsearch_location_state", SERVER_INDEX, buildPrefixQuery("state")),
    LOC_CITY("systemsearch_location_city", SERVER_INDEX, buildPrefixQuery("city")),
    LOC_ADDRESS("systemsearch_location_address", SERVER_INDEX, buildPrefixQuery("address1", "address2")),
    LOC_BUILDING("systemsearch_location_building", SERVER_INDEX, buildPrefixQuery("building")),
    LOC_ROOM("systemsearch_location_room", SERVER_INDEX, buildPrefixQuery("room")),
    LOC_RACK("systemsearch_location_rack", SERVER_INDEX, buildPrefixQuery("rack")),
    UUID("systemsearch_uuid", SERVER_INDEX, buildPrefixQuery("uuid"));

    private static final List<SystemSearchHelperType> ALL_TYPES = List.of(
            NAME_AND_DESCRIPTION,
            ID,
            CUSTOM_INFO,
            SNAPSHOT_TAG,
            CHECKIN,
            REGISTERED,
            CPU_MODEL,
            CPU_MHZ_LT,
            CPU_MHZ_GT,
            NUM_CPUS_LT,
            NUM_CPUS_GT,
            RAM_LT,
            RAM_GT,
            HW_DESCRIPTION,
            HW_DRIVER,
            HW_DEVICE_ID,
            HW_VENDOR_ID,
            DMI_SYSTEM,
            DMI_BIOS,
            DMI_ASSET,
            HOSTNAME,
            IP,
            IP6,
            INSTALLED_PACKAGES,
            NEEDED_PACKAGES,
            RUNNING_KERNEL,
            LOC_COUNTRY_CODE,
            LOC_STATE,
            LOC_CITY,
            LOC_ADDRESS,
            LOC_BUILDING,
            LOC_ROOM,
            LOC_RACK,
            UUID);

    private static List<SystemSearchHelperType> getSortLowToHighTypes() {
        return List.of(REGISTERED, CPU_MHZ_GT, NUM_CPUS_GT, RAM_GT);
    }

    private static List<SystemSearchHelperType> getSortHighToLowTypes() {
        return List.of(CHECKIN, CPU_MHZ_LT, NUM_CPUS_LT, RAM_LT);
    }

    public static List<SystemSearchHelperType> getDetailsGroup() {
        return List.of(
                NAME_AND_DESCRIPTION, ID,
                CUSTOM_INFO,
                SNAPSHOT_TAG,
                RUNNING_KERNEL,
                UUID);
    }

    public static List<SystemSearchHelperType> getActivityGroup() {
        return List.of(
                CHECKIN,
                REGISTERED);
    }

    public static List<SystemSearchHelperType> getHardwareGroup() {
        return List.of(
                CPU_MODEL,
                CPU_MHZ_LT,
                CPU_MHZ_GT,
                NUM_CPUS_LT,
                NUM_CPUS_GT,
                RAM_LT,
                RAM_GT);
    }

    public static List<SystemSearchHelperType> getDeviceGroup() {
        return List.of(
                HW_DESCRIPTION,
                HW_DRIVER,
                HW_DEVICE_ID,
                HW_VENDOR_ID);
    }

    public static List<SystemSearchHelperType> getDmiInfoGroup() {
        return List.of(
                DMI_SYSTEM,
                DMI_BIOS,
                DMI_ASSET);
    }

    public static List<SystemSearchHelperType> getNetworkInfoGroup() {
        return List.of(
                HOSTNAME,
                IP,
                IP6);
    }

    public static List<SystemSearchHelperType> getPackagesGroup() {
        return List.of(
                INSTALLED_PACKAGES,
                NEEDED_PACKAGES);
    }

    public static List<SystemSearchHelperType> getLocationGroup() {
        return List.of(
                LOC_COUNTRY_CODE,
                LOC_STATE,
                LOC_CITY,
                LOC_ADDRESS,
                LOC_BUILDING,
                LOC_ROOM,
                LOC_RACK);
    }

    public static List<SystemSearchHelperType> getActionErrorModes() {
        return List.of(
                ID,
                CPU_MHZ_LT,
                CPU_MHZ_GT,
                RAM_LT,
                RAM_GT,
                NUM_CPUS_LT,
                NUM_CPUS_GT,
                CHECKIN,
                REGISTERED);
    }

    private static UnaryOperator<String> buildPrefixQuery(String prefix) {
        return (terms) -> "%s:(%s)".formatted(prefix, terms);
    }

    private static UnaryOperator<String> buildPrefixQuery(String prefix1, String prefix2) {
        return (terms) -> "%s:(%s) %s:(%s)".formatted(prefix1, terms, prefix2, terms);
    }

    private static UnaryOperator<String> buildPrefixQuery(String prefix1, String prefix2, String prefix3) {
        return (terms) -> "%s:(%s) %s:(%s) %s:(%s)".formatted(prefix1, terms, prefix2, terms, prefix3, terms);
    }

    private static UnaryOperator<String> buildQueryFromTermToLongMax(String prefix) {
        return (terms) -> "%s:{%s TO %d}".formatted(prefix, terms, Long.MAX_VALUE);
    }

    private static UnaryOperator<String> buildQueryFrom0ToTerm(String prefix) {
        return (terms) -> "%s:{0 TO %s}".formatted(prefix, terms);
    }

    private static String formatDateString(Date d) {
        String dateFormat = "yyyyMMddHHmm";
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(dateFormat);
        // Lucene uses GMT for indexing
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(d);
    }

    private static UnaryOperator<String> buildQuerySearchCheckin() {
        return (terms) -> {
            int numDays = Integer.parseInt(terms);
            Calendar startDate = Calendar.getInstance();
            // SearchRange:  [EPOCH - TargetDate]
            startDate.add(Calendar.DATE, -1 * numDays);
            return "checkin:[\"" + formatDateString(new Date(0)) +
                    "\" TO \"" + formatDateString(startDate.getTime()) + "\"]";
        };
    }

    private static UnaryOperator<String> buildQuerySearchRegistered() {
        return (terms) -> {
            int numDays = Integer.parseInt(terms);
            Calendar startDate = Calendar.getInstance();
            // SearchRange:  [TargetDate - NOW]
            startDate.add(Calendar.DATE, (-1 * numDays) - 1);
            return "registered:{\"" + formatDateString(startDate.getTime()) +
                    "\" TO \"" + formatDateString(Calendar.getInstance().getTime()) + "\"}";
        };
    }

    /**
     * @param mode the mode label
     * @return the type corresponding to a mode label
     */
    public static Optional<SystemSearchHelperType> find(String mode) {
        return ALL_TYPES.stream()
                .filter(t -> t.equalsMode(mode))
                .findFirst();
    }

    /**
     * @param viewMode the mode label
     * @return true if one of the low to high sort types are corresponding
     */
    public static boolean isSortLowToHighMode(String viewMode) {
        return getSortLowToHighTypes().stream()
                .anyMatch(t -> t.equalsMode(viewMode));
    }

    /**
     * @param viewMode the mode label
     * @return true if one of the high to low sort types are corresponding
     */
    public static boolean isSortHighToLowMode(String viewMode) {
        return getSortHighToLowTypes().stream()
                .anyMatch(t -> t.equalsMode(viewMode));
    }

    /**
     * @param viewMode the mode label
     * @return true if mode is one of the action error modes
     */
    public static boolean isActionErrorMode(String viewMode) {
        return getActionErrorModes().stream()
                .anyMatch(t -> t.equalsMode(viewMode));
    }

    private final String label;

    private final SystemSearchHelperIndex index;

    private final UnaryOperator<String> queryFromTerms;

    SystemSearchHelperType(String labelIn, SystemSearchHelperIndex indexIn, UnaryOperator<String> queryFromTermsIn) {
        this.label = labelIn;
        this.index = indexIn;
        this.queryFromTerms = queryFromTermsIn;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getIndexLabel() {
        return index.getLabel();
    }

    /**
     * Gets the query string
     *
     * @param terms terms of the query
     * @return the query string
     */
    public String getQuery(String terms) {
        return queryFromTerms.apply(terms);
    }

    /**
     * Gets the query string
     *
     * @param mode string to compare equals
     * @return true if input string is equal to label
     */
    public boolean equalsMode(String mode) {
        return getLabel().equals(mode);
    }
}
