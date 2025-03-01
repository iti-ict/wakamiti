/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven.utils;


import org.apache.commons.io.input.XmlStreamReader;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ProjectStub extends MavenProjectStub {

    public ProjectStub(File pom) {
        final MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;
        try {
            model = pomReader.read(new XmlStreamReader(pom));
            setModel(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setGroupId(model.getGroupId());
        setArtifactId(model.getArtifactId());
        setVersion(model.getVersion());
        setName(model.getName());
        setUrl(model.getUrl());
        setPackaging(model.getPackaging());
        setBuild(model.getBuild());

        final List<String> compileSourceRoots = new ArrayList<>();
        compileSourceRoots.add(getBasedir() + "/src/main/java");
        setCompileSourceRoots(compileSourceRoots);

        final List<String> testCompileSourceRoots = new ArrayList<>();
        testCompileSourceRoots.add(getBasedir() + "/src/test/java");
        setTestCompileSourceRoots(testCompileSourceRoots);

        // normalize some expressions
        getBuild().setDirectory("${project.basedir}/target");
        getBuild().setTestOutputDirectory(new File(getBasedir(), "target/classes").getAbsolutePath());
    }

    @Override
    public List<Plugin> getBuildPlugins() {
        return getBuild().getPlugins();
    }
}
