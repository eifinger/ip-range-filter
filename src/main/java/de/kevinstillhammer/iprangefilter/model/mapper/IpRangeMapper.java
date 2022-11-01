package de.kevinstillhammer.iprangefilter.model.mapper;

import de.kevinstillhammer.iprangefilter.aws.IpRanges;
import de.kevinstillhammer.iprangefilter.model.IpRange;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class IpRangeMapper {

    private final IpRangePrefixMapper mapper;

    public IpRangeMapper(IpRangePrefixMapper mapper) {
        this.mapper = mapper;
    }

    public List<IpRange> ipRangesToIpRange(IpRanges ipRanges) {
        var v4 = ipRanges
                .getPrefixes()
                .stream()
                .map(mapper::toIpRange);
        var v6 = ipRanges
                .getIpv6Prefixes()
                .stream()
                .map(mapper::toIpRange);
        return Stream.concat(v4, v6).toList();
    }
}
