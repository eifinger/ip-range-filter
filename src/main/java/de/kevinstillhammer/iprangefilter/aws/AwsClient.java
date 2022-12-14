package de.kevinstillhammer.iprangefilter.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AwsClient {

    private final WebClient webClient;

    public AwsClient(
            WebClient.Builder webClientBuilder, @Value("${awsUrl}") String awsUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(awsUrl)
                .build();
    }

    public Mono<IpRanges> getIpRanges() {
        return this.webClient
                .get()
                .retrieve()
                .bodyToMono(IpRanges.class);
    }
}
