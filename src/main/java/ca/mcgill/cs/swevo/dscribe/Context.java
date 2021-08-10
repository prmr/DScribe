/*******************************************************************************
 * Copyright 2020 McGill University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package ca.mcgill.cs.swevo.dscribe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import ca.mcgill.cs.swevo.dscribe.template.InMemoryTemplateRepository;
import ca.mcgill.cs.swevo.dscribe.template.TemplateRepository;

/**
 * Represents the context of execution of DScribe. It contains user settings such as the template
 * repository location.
 * 
 * @author mnassif
 *
 */
public class Context {
  public enum TestClassNameConvention {
    POSTFIX, PREFIX
  };

  private static final Context INSTANCE = new Context();
  private static final String ROOT = Paths.get("..").toAbsolutePath().normalize().toString();
  private String templateRepoPath = Path.of("templates").toString(); // default location of
                                                                     // templates
  private ClassLoader classLoader = Context.class.getClassLoader(); // default class loader

  private TestClassNameConvention testClassNameConvention = TestClassNameConvention.POSTFIX;

  private Context() {}

  public static Context instance() {
    return INSTANCE;
  }

  // TO DO: Edit
  public List<Path> srcPaths() {
    return List.of(Path.of(ROOT, "JetUML", "src", "ca", "mcgill", "cs", "jetuml", "geom"),
        Path.of(ROOT, "JetUML", "src", "ca", "mcgill", "cs", "jetuml", "diagram"));
  }

  public String templateRepositoryPath() {
    return templateRepoPath;
  }

  public void setTemplateRepositoryPath(String templateRepositoryPath) {
    assert templateRepositoryPath != null;
    this.templateRepoPath = templateRepositoryPath;
  }

  public ClassLoader classLoader() {
    return classLoader;
  }

  public void setClassLoader(ClassLoader classLoader) {
    assert classLoader != null;
    this.classLoader = classLoader;
  }

  public TestClassNameConvention testClassNameConvention() {
    return testClassNameConvention;
  }

  public TemplateRepository templateRepository() {
    return new InMemoryTemplateRepository(templateRepositoryPath());
  }
}
