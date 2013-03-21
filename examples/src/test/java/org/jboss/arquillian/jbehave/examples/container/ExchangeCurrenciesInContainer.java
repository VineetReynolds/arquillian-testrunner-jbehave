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
package org.jboss.arquillian.jbehave.examples.container;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.jbehave.core.ArquillianInstanceStepsFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.runner.RunWith;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * A JBehave story, that uses the Arquillian JUnit test runner to execute the story.
 * 
 * @author Vineet Reynolds
 *
 */
@RunWith(Arquillian.class)
public class ExchangeCurrenciesInContainer extends JUnitStory
{
   
   @Deployment
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "test-incontainer.war")
            .addPackage("org.jboss.arquillian.jbehave.domain")
            .addPackage("org.jboss.arquillian.jbehave.examples.container")
            .addAsResource("org/jboss/arquillian/jbehave/examples/container/exchange_currencies_in_container.story")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
      archive.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
            .artifact("com.google.guava:guava:11.0.1")
            .resolveAs(JavaArchive.class));
      return archive;
   }

   public ExchangeCurrenciesInContainer()
   {
      /* 
       * Configure JBehave to use the Guava SameThreadExecutorService.
       * This enables the ArquillianInstanceStepsFactory to access
       * the ThreadLocal contexts and datastores.
       */
      configuredEmbedder().useExecutorService(MoreExecutors.sameThreadExecutor());
   }
   
   @Override
   public Configuration configuration()
   {
      Configuration configuration = new MostUsefulConfiguration()
            .useStoryPathResolver(new UnderscoredCamelCaseResolver())
            .useStoryReporterBuilder(new StoryReporterBuilder()
                  .withDefaultFormats()
                  .withFormats(CONSOLE, TXT, HTML, XML)
                  .withFailureTrace(true));
      return configuration;
   }
   
   @Override
   public InjectableStepsFactory stepsFactory()
   {
      return new ArquillianInstanceStepsFactory(configuration(), new ExchangeCurrenciesInContainerSteps());
   }
   
}