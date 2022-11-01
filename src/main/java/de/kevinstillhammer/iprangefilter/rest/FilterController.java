package de.kevinstillhammer.iprangefilter.rest;

import de.kevinstillhammer.iprangefilter.aws.AwsClient;
import de.kevinstillhammer.iprangefilter.model.IpRange;
import de.kevinstillhammer.iprangefilter.model.RegionStartingWith;
import de.kevinstillhammer.iprangefilter.model.mapper.IpRangeMapper;
import io.micrometer.core.annotation.Timed;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static de.kevinstillhammer.iprangefilter.model.IpRangeFilter.byRegionStartingWithIgnoreCase;

@RestController
public class FilterController {

    final AwsClient awsClient;
    private final IpRangeMapper ipRangeMapper;

    public FilterController(AwsClient awsClient, IpRangeMapper ipRangeMapper) {
        this.awsClient = awsClient;
        this.ipRangeMapper = ipRangeMapper;
    }

    @GetMapping("/ip-ranges")
    @Timed(value = "ip-ranges-timer")
    public @ResponseBody Flux<String> ipRanges(@RequestParam(required = false, defaultValue = "ALL") RegionStartingWith region) {
        return awsClient
                .getIpRanges()
                .map(ipRangeMapper::ipRangesToIpRange)
                .map(ipRanges -> byRegionStartingWithIgnoreCase(ipRanges, region))
                .flatMapIterable(ipRanges -> ipRanges)
                .map(IpRange::getPrefix)
                .map(prefix -> prefix.concat(System.lineSeparator()));
    }

    @ExceptionHandler(value = {TypeMismatchException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleTypeMismatchException(TypeMismatchException e) {
        var validRegions = Arrays
                .stream(RegionStartingWith.values())
                .map(Enum::name)
                .collect(Collectors.joining(","));
        return String.format("Invalid region '%s'. Valid regions are: %s", e.getValue(), validRegions);
    }
}
