/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.jbehave.client;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.jboss.shrinkwrap.resolver.api.maven.filter.StrictFilter;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Listens for the {@link BeforeSuite} event.
 * Unpacks the jbehave-site-resources.zip and jbehave-core-resources.zip
 * archives from the Maven Central repo.
 *
 * @author Vineet Reynolds
 */
public class ViewResourcesUnpacker {

    private String targetDirectory = "target/jbehave/view";

    public void extractResources(@Observes BeforeSuite event) {
        String globalSettings = null;
        String userSettings = null;
        if (new File(System.getenv().get("M2_HOME") + "/conf/settings.xml").exists()) {
            globalSettings = new File(System.getenv().get("M2_HOME") + "/conf/settings.xml").getAbsolutePath();
        }
        if (new File(System.getProperty("user.home") + "/.m2/settings.xml").exists()) {
            userSettings = new File(System.getProperty("user.home") + "/.m2/settings.xml").getAbsolutePath();
        }
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class);
        if (globalSettings != null) {
            resolver.configureFrom(globalSettings);
        }
        if (userSettings != null) {
            resolver.configureFrom(userSettings);
        }
        resolver.loadMetadataFromPom("pom.xml");
        File[] siteResources = resolver.artifact("org.jbehave.site:jbehave-site-resources:zip")
                .artifact("org.jbehave:jbehave-core:zip:resources")
                .resolveAsFiles(new StrictFilter());
        File destination = new File(targetDirectory);
        for (File resource : siteResources) {
            try {
                unpack(resource, destination);
            } catch (ZipException zipEx) {
                throw new RuntimeException(zipEx);
            } catch (IOException ioEx) {
                throw new RuntimeException(ioEx);
            }
        }

    }

    private void unpack(File resource, File destination) throws ZipException, IOException {
        destination.mkdirs();

        ZipFile zip = new ZipFile(resource);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry currentEntry = entries.nextElement();
            File entryFile = new File(destination, currentEntry.getName());
            File parentFile = entryFile.getParentFile();
            parentFile.mkdirs();

            if (currentEntry.isDirectory()) {
                entryFile.mkdirs();
            } else {
                BufferedInputStream bis = new BufferedInputStream(zip.getInputStream(currentEntry));
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(entryFile));

                byte[] buffer = new byte[1024];
                int readBytes;
                while ((readBytes = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, readBytes);
                }

                bis.close();
                out.close();
            }
        }
    }

}
