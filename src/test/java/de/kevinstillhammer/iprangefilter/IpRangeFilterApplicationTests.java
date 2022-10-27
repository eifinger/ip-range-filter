package de.kevinstillhammer.iprangefilter;

import java.io.IOException;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

    public static class MockServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {
            mockServer.start();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(configurableApplicationContext,
                    "awsUrl=" + mockServer.getEndpoint() + "/ip-ranges.json");
        }
    }
}
