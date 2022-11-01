package de.kevinstillhammer.iprangefilter.aws;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Data;

@Data
public class IpRanges {
    @JsonProperty("syncToken")
    private String syncToken;
    @JsonProperty("createDate")
    private String createDate;
    @JsonProperty("prefixes")
    private List<Prefix> prefixes = Collections.emptyList();
    @JsonProperty("ipv6_prefixes")
    private List<Prefix> ipv6Prefixes = Collections.emptyList();
}
