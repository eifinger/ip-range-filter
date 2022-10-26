package de.kevinstillhammer.iprangefilter;

import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class IpRangeFilterApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("classpath:ip_ranges_26_10_2022.json")
	Resource ipRangeFile;

	@Container
	public MockServerContainer mockServer = new MockServerContainer(
			DockerImageName.parse(
					"mockserver/mockserver"
			).withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion())
	);

	@Test
	void defaultShouldContainKnownIPRange() throws IOException {
		try (var mockServerClient = new MockServerClient(mockServer.getHost(), mockServer.getServerPort())) {
			mockServerClient.when(request()
							.withPath("/ip-ranges.json"))
					.respond(response()
							.withBody(StreamUtils.copyToString(ipRangeFile.getInputStream(), Charset.defaultCharset())));
		}

		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/ip-ranges",
				String.class)).contains("3.2.34.0/26");
	}
}
