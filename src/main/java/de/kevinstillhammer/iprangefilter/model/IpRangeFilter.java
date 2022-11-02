package de.kevinstillhammer.iprangefilter.model;

import java.util.function.Function;

public final class IpRangeFilter {
    private IpRangeFilter() {
    }

    /**
     * Filter for IpRanges where the region starts with the supplied enum value.
     *
     * @param regionStartsWith IpRange region should start with
     * @return True if the region of the IpRange starts with the value. False otherwise.
     */
    public static Function<IpRange, Boolean> forRegionStartsWith(RegionStartsWith regionStartsWith) {
        if (regionStartsWith.equals(RegionStartsWith.ALL)) {
            return ipRange -> true;
        }
        return ipRange -> ipRange
                .getRegion()
                .startsWith(regionStartsWith
                        .name()
                        .toLowerCase());
    }
}
