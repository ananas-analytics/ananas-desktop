package org.ananas.runner.model.steps.scripting;


import com.google.common.io.CharStreams;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.beam.sdk.io.fs.MatchResult;
import org.apache.beam.sdk.io.fs.MatchResult.Metadata;
import org.apache.beam.sdk.io.fs.MatchResult.Status;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A Text UDF Transform Function. Note that this class's implementation is not threadsafe
 */

/**
 * Grabs code from a FileSystem, loads it into the Nashorn Javascript Engine, and executes
 * Javascript Functions.
 */
class JavascriptRuntime implements Serializable {

	private static final long serialVersionUID = 6796422611469431942L;
	private ScriptEngine invocable;
	private String functionName;

	JavascriptRuntime(String functionName,
					  Collection<String> scripts) throws ScriptException {
		this.invocable = newInvocable(scripts);
		this.functionName = functionName;
	}

	private ScriptEngine getInvocable() {
		return this.invocable;
	}

	private static ScriptEngine newInvocable(Collection<String> scripts) throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		for (String script : scripts) {
			engine.eval(script);
		}

		if (engine == null) {
			throw new RuntimeException("No javascript engine was loaded");
		}
		return engine;
	}

	List<String> invoke(String data) throws ScriptException, IOException, NoSuchMethodException {
		List<String> strings = new ArrayList<>();
		ScriptObjectMirror json = (ScriptObjectMirror) this.invocable.eval("JSON");
		Object o = json.callMember("parse", data);
		Object result = ((Invocable) this.invocable).invokeFunction(functionName(), o);
		if (result == null || ScriptObjectMirror.isUndefined(result)) {
			return strings;
		} else if (result instanceof String) {
			strings.add((String) result);
		} else if (result instanceof ScriptObjectMirror) {
			ScriptObjectMirror jsObj = ((ScriptObjectMirror) result);
			if (jsObj.isArray()) {
				for (int i = 0; i < jsObj.size(); i++) {
					strings.add((String) json.callMember("stringify", jsObj.getSlot(i)));
				}
			} else {
				strings.add((String) json.callMember("stringify", result));
			}
		} else {
			String className = result.getClass().getTypeName();
			throw new RuntimeException(
					"UDF Function did not return a String. Instead got: " + className);
		}
		return strings;
	}

	private String functionName() {
		return this.functionName;
	}

	/**
	 * Loads into memory scripts from a File System from a given path. Supports any file system that
	 * {@link FileSystems} supports.
	 *
	 * @return a collection of scripts loaded as UF8 Strings
	 */
	private static Collection<String> getScripts(String path) throws IOException {
		MatchResult result = FileSystems.match(path);
		checkArgument(
				result.status() == Status.OK && !result.metadata().isEmpty(),
				"Failed to match any files with the pattern: " + path);

		List<String> scripts =
				result
						.metadata()
						.stream()
						.filter(metadata -> metadata.resourceId().getFilename().endsWith(".js"))
						.map(Metadata::resourceId)
						.map(
								resourceId -> {
									try (Reader reader =
												 Channels.newReader(
														 FileSystems.open(resourceId), StandardCharsets.UTF_8.name())) {
										return CharStreams.toString(reader);
									} catch (IOException e) {
										throw new UncheckedIOException(e);
									}
								})
						.collect(Collectors.toList());

		return scripts;
	}

}