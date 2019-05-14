package org.ananas.runner.model.core;

import freemarker.template.TemplateException;
import org.ananas.runner.api.JsonUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class DagRequestTest {

	@Test
	public void resolveVariables() throws IOException, TemplateException {
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("dagrequests/example1.json");
		String jsonRequest = new String(Files.readAllBytes(Paths.get(resource.getPath())), StandardCharsets.UTF_8);


		DagRequest req = JsonUtil.fromJson(jsonRequest, DagRequest.class);
		DagRequest newReq = req.resolveVariables();

		assertEquals(6, newReq.dag.steps.size());

		Iterator<Step> it = newReq.dag.steps.iterator();
		while (it.hasNext()) {
			Step step = it.next();

			if (step.id.equals("5c969069d47a400e547d25d9")) {
				assertEquals("some string value", step.config.get("path"));
			}

			if (step.id.equals("5c9697213cc63f7d6665abb0")) {
				assertEquals("123", step.config.get("xlabel"));

				List<String> dimensions = (List<String>)step.config.get("dimension");
				assertEquals("14.05.2019, 17:22", dimensions.get(0));
			}

		}
	}
}
