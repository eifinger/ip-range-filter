package de.kevinstillhammer.iprangefilter.model.filter;

import de.kevinstillhammer.iprangefilter.model.Region;

public class RegionFilterIpRanges extends AbstractIpRangeFilter {
    protected RegionFilterIpRanges(Region region) {
        super(ipRange -> ipRange
                .getRegion()
                .startsWith(region
                        .name()
                        .toLowerCase()));

    }
}
