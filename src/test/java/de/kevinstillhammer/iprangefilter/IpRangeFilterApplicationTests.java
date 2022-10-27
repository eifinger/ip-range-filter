package de.kevinstillhammer.iprangefilter;

import de.kevinstillhammer.iprangefilter.filter.Region;
import java.io.IOException;
import java.nio.charset.Charset;
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
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/ip-ranges", String.class)).contains("3.2.34.0/26");
    }

    @Test
    void resultShouldBeTextPlain() {
        assertThat(this.restTemplate
                .getForEntity("http://localhost:" + port + "/ip-ranges", String.class)
                .getHeaders()
                .getContentType()).hasToString("text/plain;charset=UTF-8");
    }

    @ParameterizedTest
    @MethodSource("knownIpForRegion")
    void regionFilterShouldReturnKnownPrefixes(Region region, String knownIp) {
        assertThat(this.restTemplate
                .getForObject("http://localhost:" + port + "/ip-ranges?region=" + region.name(), String.class))
                .contains(knownIp);
    }

    private static Stream<Arguments> knownIpForRegion() {
        return Stream.of(
                Arguments.of(Region.AF, "3.2.34.0/26"),
                Arguments.of(Region.CA, "15.177.100.0/24"),
                Arguments.of(Region.AP, "13.236.0.0/14"),
                Arguments.of(Region.EU, "15.230.158.0/23"),
                Arguments.of(Region.CN, "52.82.169.0/28"),
                Arguments.of(Region.US, "52.93.178.138/32"),
                Arguments.of(Region.SA, "52.93.122.203/32")
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
