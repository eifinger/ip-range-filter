package de.kevinstillhammer.iprangefilter.model;

import java.util.List;

public class IpRangeFilter {
    private IpRangeFilter() {
    }

    public static List<IpRange> byRegionStartingWithIgnoreCase(List<IpRange> ipRanges, RegionStartingWith region) {
        if (region.equals(RegionStartingWith.ALL)) {
            return ipRanges;
        }
        return ipRanges
                .stream()
                .filter(ipRange -> ipRange
                        .getRegion()
                        .startsWith(region
                                .name()
                                .toLowerCase()))
                .toList();
    }
}
