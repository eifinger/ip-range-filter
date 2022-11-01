package de.kevinstillhammer.iprangefilter.model.mapper;

import de.kevinstillhammer.iprangefilter.aws.IpRanges;
import de.kevinstillhammer.iprangefilter.model.IpRange;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IpRangeMapper {

    private final IpRangePrefixMapper mapper;

    public IpRangeMapper(IpRangePrefixMapper mapper) {
        this.mapper = mapper;
    }

    public List<IpRange> ipRangesToIpRange(IpRanges ipRanges) {
        return ipRanges
                .getPrefixes()
                .stream()
                .map(mapper::toIpRange)
                .toList();
    }
}
