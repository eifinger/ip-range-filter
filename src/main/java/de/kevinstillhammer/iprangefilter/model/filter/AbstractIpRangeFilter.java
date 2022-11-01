package de.kevinstillhammer.iprangefilter.model.filter;

import de.kevinstillhammer.iprangefilter.model.IpRange;
import java.util.List;
import java.util.function.Function;

class AbstractIpRangeFilter implements IpRangeFilter {

    private final Function<IpRange, Boolean> filter;

    protected AbstractIpRangeFilter(Function<IpRange, Boolean> filter) {
        this.filter = filter;
    }

    public List<IpRange> apply(List<IpRange> ipRanges) {
        return ipRanges
                .stream()
                .filter(this.filter::apply)
                .toList();
    }
}
