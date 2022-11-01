package de.kevinstillhammer.iprangefilter.model;

import lombok.Data;

@Data
public class IpRange {
    private String prefix;
    private String region;
    private String service;
    private String networkBorderGroup;
}
