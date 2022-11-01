package de.kevinstillhammer.iprangefilter.model.mapper;

import de.kevinstillhammer.iprangefilter.aws.Prefix;
import de.kevinstillhammer.iprangefilter.model.IpRange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IpRangePrefixMapper {
    @Mapping(target = "prefix", source = "ipPrefix")
    IpRange toIpRange(Prefix prefix);
}
