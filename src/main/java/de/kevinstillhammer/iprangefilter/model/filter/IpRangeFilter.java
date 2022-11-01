package de.kevinstillhammer.iprangefilter.model.filter;

import de.kevinstillhammer.iprangefilter.model.IpRange;
import de.kevinstillhammer.iprangefilter.model.Region;
import java.util.List;

public interface IpRangeFilter {
    List<IpRange> apply(List<IpRange> ipRanges);

    static IpRangeFilter forRegion(Region region) {
        if (region.equals(Region.ALL)) {
            return new NoopFilterIpRanges();
        }
        return new RegionFilterIpRanges(region);
    }
}
