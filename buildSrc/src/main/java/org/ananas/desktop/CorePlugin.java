package org.ananas.desktop;

import java.util.Set;

import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.Project;
import org.gradle.api.ProjectState;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.Plugin;
import org.gradle.api.DefaultTask;

class CorePlugin implements Plugin<Settings> {
	public void apply(Settings settings) {
		Gradle gradle = settings.getGradle();

		gradle.projectsLoaded((Gradle g) -> {
			Project rootProject = gradle.getRootProject();
			ProjectVersion version = this.getProjectVersion(rootProject);
			this.injectProjectVersion(rootProject, version);	
			this.injectRunnerJarName(rootProject, version);
		});
	}	

	private ProjectVersion getProjectVersion(Project project) {
		int major = Integer.parseInt((String)project.property("major"));
		int minor = Integer.parseInt((String)project.property("minor"));
		int patch = Integer.parseInt((String)project.property("patch"));
		boolean release = Boolean.parseBoolean((String)project.property("release"));
		return new ProjectVersion(major, minor, patch, release);
	}

	/**
	 * inject project to all subprojects
	 */
	private void injectProjectVersion(Project project, ProjectVersion version) {
		project.setVersion(version);

		Set<Project> subProjects = project.getSubprojects();
		for (Project subProject : subProjects) {
			subProject.setVersion(version);
		}
	}

	private void injectRunnerJarName(Project project, ProjectVersion version) {
		project.getExtensions().add("runnerJarName", "runner-all-" + version);
		project.getExtensions().add("runnerJarBaseName", "runner-all");
		Set<Project> projects = project.getSubprojects();
		for (Project subProject : projects) {
			subProject.getExtensions().add("runnerJarName", "runner-all-" + version);
			subProject.getExtensions().add("runnerJarBaseName", "runner-all");
		}
	}
}
