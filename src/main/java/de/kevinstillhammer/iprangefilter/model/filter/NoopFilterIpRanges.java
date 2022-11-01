package de.kevinstillhammer.iprangefilter.model.filter;

public class NoopFilterIpRanges extends AbstractIpRangeFilter {
    protected NoopFilterIpRanges() {
        super(ipRange -> true);
    }
}
