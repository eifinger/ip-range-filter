package de.kevinstillhammer.iprangefilter.filter;

import de.kevinstillhammer.iprangefilter.aws.AwsClient;
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
    public @ResponseBody Flux<String> ipRanges(@RequestParam(required = false) String region) {
        return awsClient.getIpPrefixes();
    }
}
