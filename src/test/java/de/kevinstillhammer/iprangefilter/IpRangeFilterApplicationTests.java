package de.kevinstillhammer.iprangefilter;

import de.kevinstillhammer.iprangefilter.model.RegionStartingWith;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {IpRangeFilterApplicationTests.MockServerInitializer.class},
                      classes = {IpRangeFilterApplication.class})
@Testcontainers
class IpRangeFilterApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    MockServerClient mockServerClient;

    @Container
    public static final MockServerContainer mockServer = new MockServerContainer(DockerImageName
            .parse("mockserver/mockserver")
            .withTag("mockserver-" + MockServerClient.class
                    .getPackage()
                    .getImplementationVersion()));

    @Value("classpath:ip_ranges_26_10_2022.json")
    Resource ipRangeFile;

    @BeforeAll
    public void setUp() throws IOException {
        mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort());
        mockServerClient
                .when(request().withPath("/ip-ranges.json"))
                .respond(response()
                        .withBody(StreamUtils.copyToString(ipRangeFile.getInputStream(), Charset.defaultCharset()))
                        .withContentType(MediaType.APPLICATION_JSON));
    }

    @AfterAll
    public void tearDown() {
        mockServerClient.close();
    }

    @Test
    void defaultShouldContainKnownIPRange() {
        var url = String.format("http://localhost:%s/ip-ranges", port);
        assertThat(this.restTemplate.getForObject(url, String.class)).contains("3.2.34.0/26");
    }

    @Test
    void defaultShouldReturnAllIpranges() {
        var url = String.format("http://localhost:%s/ip-ranges", port);
        assertThat(this.restTemplate.getForObject(url, String.class).split(System.lineSeparator())).hasSize(8820);
    }

    @Test
    void ipRangesShouldBeSeparatedByNewLine() {
        var url = String.format("http://localhost:%s/ip-ranges", port);
        assertThat(this.restTemplate.getForObject(url, String.class)).startsWith("3.2.34.0/26" + System.lineSeparator() + "3.5.140.0/22");
    }

    @Test
    void resultShouldBeTextPlain() {
        var url = String.format("http://localhost:%s/ip-ranges", port);
        assertThat(this.restTemplate
                .getForEntity(url, String.class)
                .getHeaders()
                .getContentType()).hasToString("text/plain;charset=UTF-8");
    }

    @Test
    void invalidFilterShouldRespondWithValidFilter() {
        var url = String.format("http://localhost:%s/ip-ranges?region=%s", port, "invalid");
        var response = this.restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Valid regions are");

        assertThat(Arrays.stream(RegionStartingWith.values())).allSatisfy(region -> response
                .getBody()
                .contains(region.name()));
    }

    @ParameterizedTest
    @MethodSource("knownIpForRegion")
    void regionFilterShouldReturnKnownPrefixes(RegionStartingWith region, String knownIp) {
        var url = String.format("http://localhost:%s/ip-ranges?region=%s", port, region.name());
        var result = this.restTemplate.getForObject(url, String.class);
        assertThat(result).contains(knownIp);
    }

    private static Stream<Arguments> knownIpForRegion() {
        return Stream.of(
                Arguments.of(RegionStartingWith.AF, "3.2.34.0/26", 142),
                Arguments.of(RegionStartingWith.CA, "15.177.100.0/24", 204),
                Arguments.of(RegionStartingWith.AP, "13.236.0.0/14", 2082),
                Arguments.of(RegionStartingWith.EU, "15.230.158.0/23", 1936),
                Arguments.of(RegionStartingWith.CN, "52.82.169.0/28", 298),
                Arguments.of(RegionStartingWith.US, "52.93.178.138/32", 3172),
                Arguments.of(RegionStartingWith.SA, "52.93.122.203/32", 284)
        );
    }

    @ParameterizedTest
    @MethodSource("amountOfIpsForRegion")
    void regionFilterShouldReturnCorrectAmountOfIps(RegionStartingWith region, int amountOfIps) {
        var url = String.format("http://localhost:%s/ip-ranges?region=%s", port, region.name());
        var result = this.restTemplate.getForObject(url, String.class);
        assertThat(result.split(System.lineSeparator())).hasSize(amountOfIps);
    }

    private static Stream<Arguments> amountOfIpsForRegion() {
        return Stream.of(
                Arguments.of(RegionStartingWith.AF, 142),
                Arguments.of(RegionStartingWith.CA, 204),
                Arguments.of(RegionStartingWith.AP, 2082),
                Arguments.of(RegionStartingWith.EU, 1936),
                Arguments.of(RegionStartingWith.CN, 298),
                Arguments.of(RegionStartingWith.US, 3172),
                Arguments.of(RegionStartingWith.SA, 284)
        );
    }

    public static class MockServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            mockServer.start();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                    "awsUrl=" + mockServer.getEndpoint() + "/ip-ranges.json");
        }
    }
}
