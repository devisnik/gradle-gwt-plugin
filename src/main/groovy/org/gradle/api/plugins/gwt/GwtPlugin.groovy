/*
   Copyright 2011 the original author or authors.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.gradle.api.plugins.gwt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.WarPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.api.tasks.testing.Test
/**
 *
 * @author Markus Kobler
 */
class GwtPlugin implements Plugin<Project> {

    public static final String GWT_CONFIGURATION_NAME = "gwt";

    public static final String COMPILE_GWT_TASK_NAME = "compileGwt"
    public static final String SOURCES_GWT_TASK_NAME = "gwtSources"
    public static final String GWT_DEV_MODE_TASK_NAME = "gwtDevMode"
    public static final String GWT_TASKS_GROUP_NAME = 'GWT'

    void apply(Project project) {
        project.plugins.apply(JavaPlugin.class)

        GwtPluginConvention pluginConvention = new GwtPluginConvention(project)
        project.convention.plugins.gwt = pluginConvention

        excludeFiles(project)

        configureConfigurations(project.configurations)

        if (project.plugins.hasPlugin(WarPlugin.class)) {
            addCompileGwtTask(project)
            addGwtDevModeTask(project)
            configureTestTaskDefaults(project)
        } else {
            configureSourceGeneration(project)
        }


        configureGwtDependenciesIfVersionSpecified(project, pluginConvention)

    }

    private void configureSourceGeneration(Project project) {
        Jar sourcesTask = project.tasks.create(SOURCES_GWT_TASK_NAME, Jar.class)
        sourcesTask.description = 'Assembles a jar archive containing the sources, needed for GWT compilation by dependant modules'
        sourcesTask.group = GWT_TASKS_GROUP_NAME
        sourcesTask.dependsOn JavaPlugin.CLASSES_TASK_NAME
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn sourcesTask
        sourcesTask.classifier = 'sources'
        sourcesTask.from project.sourceSets.main.allSource
        project.artifacts {
            archives sourcesTask
        }
    }

    void addCompileGwtTask(Project project) {
        project.tasks.withType(CompileGwt.class).all { CompileGwt task ->

            task.conventionMapping.modules = { project.convention.getPlugin(GwtPluginConvention.class).gwtModules }

            task.buildDir = project.file("build/gwt/out")
            task.workDir = project.file("build/gwt/work")
            task.extraDir = project.file("build/gwt/extra")
            task.genDir = project.file("build/gwt/extra")
            task.classpath = gwtTaskClasspath(project)
        }

        CompileGwt compileGwt = project.tasks.create(COMPILE_GWT_TASK_NAME, CompileGwt.class)
        compileGwt.group = GWT_TASKS_GROUP_NAME
        compileGwt.dependsOn(JavaPlugin.CLASSES_TASK_NAME)

        project.tasks.war.dependsOn(compileGwt)
        project.tasks.war.configure {
            from compileGwt.buildDir
        }

        compileGwt.description = "Compile GWT Modules"
    }

    private FileCollection gwtTaskClasspath(Project project) {
        SourceSet mainSourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        def sourcesFromProject = project.files(mainSourceSet.resources.srcDirs, mainSourceSet.java.srcDirs, mainSourceSet.output.classesDir, mainSourceSet.compileClasspath)
        def sourcesFromDependentProjects = sourcesFromDependentProjects(project)
        if (sourcesFromDependentProjects) return sourcesFromProject + sourcesFromDependentProjects
        return sourcesFromProject
    }

    private static def sourcesFromDependentProjects(Project project) {
        project.configurations.gwt.dependencies.withType(ProjectDependency.class).collect {
            def sourceSets = it.dependencyProject.convention.getPlugin(JavaPluginConvention).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            it.dependencyProject.files(
                    sourceSets.resources.srcDirs,
                    sourceSets.java.srcDirs,
            )
        }
    }


    void addGwtDevModeTask(Project project) {

        project.tasks.withType(GwtDevMode.class).all { GwtDevMode task ->

            task.conventionMapping.modules = { project.convention.getPlugin(GwtPluginConvention.class).gwtModules }
            task.classpath = getGwtDevModeTaskClasspath(project)
            task.warDir = getWebappDir(project)
        }

        GwtDevMode gwtDevMode = project.tasks.create(GWT_DEV_MODE_TASK_NAME, GwtDevMode.class)
        gwtDevMode.group = GWT_TASKS_GROUP_NAME
        gwtDevMode.dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        gwtDevMode.description = "Run's GWT Developer Mode"

    }

    private FileCollection  getGwtDevModeTaskClasspath(Project project) {
        SourceSet mainSourceSet = project.convention.getPlugin(JavaPluginConvention.class).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        // TODO optionally enable reloading of other local gwtModules source code...
        def reloadableResources = project.files(mainSourceSet.resources.srcDirs, mainSourceSet.java.srcDirs)

        def sourcesFromProject = project.files(reloadableResources, mainSourceSet.runtimeClasspath)
        def sourcesFromDependentProjects = sourcesFromDependentProjects(project)
        if (sourcesFromDependentProjects) return sourcesFromProject + sourcesFromDependentProjects
        return sourcesFromProject
    }

    private void configureTestTaskDefaults(final Project project) {

        project.tasks.withType(Test.class).all { Test test ->

            test.dependsOn(COMPILE_GWT_TASK_NAME)

            test.conventionMapping.classpath = {

                def sourceSets = project.convention.getPlugin(JavaPluginConvention.class).sourceSets

                SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
                SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

                project.files(testSourceSet.java.srcDirs,
                        testSourceSet.runtimeClasspath,
                        mainSourceSet.resources.srcDirs,
                        mainSourceSet.java.srcDirs,
                        mainSourceSet.output.classesDir,
                        mainSourceSet.compileClasspath);
            }

        }
    }


    private void configureConfigurations(ConfigurationContainer configurationContainer) {
        Configuration gwtConfiguration = configurationContainer.create(GWT_CONFIGURATION_NAME).setVisible(false).
                setDescription("Libraries required to compile this GWT project but not needed at runtime");
        try {
            configurationContainer.getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(gwtConfiguration)
        } catch (UnknownConfigurationException ex) {
            configurationContainer.getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME).extendsFrom(gwtConfiguration)
        }
    }

    private File getWebappDir(Project project) {
        def webappDir = project.file("src/main/webapp")
        try {
            webappDir = project.convention.getPlugin(WarPluginConvention.class).webAppDir
        } catch (IllegalStateException ex) {
            // ignore
        }
        println "====>" + webappDir
        return webappDir;
    }

    private void excludeFiles(final Project project) {
        project.tasks.withType(War.class).all { War war ->

            def excludePattern = project.convention.getPlugin(GwtPluginConvention.class).excludePattern

            if (excludePattern != null && excludePattern.length() > 0) {
                war.doFirst {
                    rootSpec.eachFile { details ->
                        if (details.path.matches(excludePattern)) {
                            war.getLogger().debug("Excluding : {}", details.path);
                            details.exclude()
                        }
                    }
                }
            }
        }
    }

    private void configureGwtDependenciesIfVersionSpecified(
            final Project project, final GwtPluginConvention convention) {

        project.gradle.taskGraph.whenReady { TaskExecutionGraph taskGraph ->
            if (needToAddGwtDependencies(convention)) {
                project.dependencies {
                    gwt "com.google.gwt:gwt-dev:${convention.gwtVersion}"
                    gwt "com.google.gwt:gwt-user:${convention.gwtVersion}"
                    runtime "com.google.gwt:gwt-servlet:${convention.gwtVersion}"
                }
            }
        }
    }

    private boolean needToAddGwtDependencies(GwtPluginConvention convention) {
        convention.gwtVersion
    }

}
