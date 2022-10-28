package de.kevinstillhammer.iprangefilter.filter;

import de.kevinstillhammer.iprangefilter.aws.AwsClient;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class FilterController {

    final AwsClient awsClient;

    public FilterController(AwsClient awsClient) {
        this.awsClient = awsClient;
    }

    @GetMapping("/ip-ranges")
    @Timed(value = "ip-ranges-timer")
    public @ResponseBody Flux<String> ipRanges(@RequestParam(required = false, defaultValue = "ALL") Region region) {
        if (region.equals(Region.ALL)) {
            return awsClient.getIpPrefixes().map(prefix -> prefix.concat(System.lineSeparator()));
        }
        return awsClient.getIpPrefixesForRegionStartingWith(region.name()).map(prefix -> prefix.concat(System.lineSeparator()));
    }
}
