package com.nginx.image.core;
/*
import com.google.common.base.Optional;
import com.nginx.image.PhotoResizerConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.client.Client;

import static org.assertj.core.api.Assertions.assertThat;

*
 * Created by chrisstetson on 10/27/15.

public class PhotoResizerTest
{

    private static final String TMP_FILE = createTempFile();
    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-example.yml");

    @ClassRule
    public static final DropwizardAppRule<PhotoResizerConfiguration> RULE = new DropwizardAppRule<>(
            PhotoResizer.class, CONFIG_PATH,
            ConfigOverride.config("database.url", "jdbc:h2:" + TMP_FILE));


    private Client client;

    @Before
    public void setUp() throws Exception
    {


    }

    private static String createTempFile() {
        try {
            return File.createTempFile("test-example", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @Test
    public void testResize() throws Exception
    {
        final Optional<String> image = Optional.fromNullable("image=");
        final PhotoResizer photoResizer = client.target("http://localhost:" + RULE.getLocalPort() + "/hello-world")
                .queryParam("image", "")
                .request()
                .get(PhotoResizer.class);
        assertThat(photoResizer.resizeImage()).isEqualTo(RULE.getConfiguration().buildTemplate().render(name));


    }
}
*/