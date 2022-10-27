package de.kevinstillhammer.iprangefilter.aws;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Prefix {
    @JsonProperty("ip_prefix")
    @JsonAlias("ipv6_prefix")
    private String ipPrefix;
    @JsonProperty("region")
    private String region;
    @JsonProperty("service")
    private String service;
    @JsonProperty("network_border_group")
    private String networkBorderGroup;
}
