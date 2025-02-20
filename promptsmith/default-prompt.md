# Project Context

## README
    # Promptsmith (a Maven Plugin)
    
    ## Project Information
    Promptsmith is a Maven plugin that generates an AI-friendly prompt containing essential project details such as dependencies, reference files, and project metadata. This is useful for AI-assisted code reviews, documentation, or workflow automation.
    
    - **Project Directory:** `${projectDirectory}`
    - **Maven Version:** `${mavenVersion}`
    
    ## Features
    - Automatically collects project metadata, including dependencies and reference files.
    - Uses Freemarker templates to generate customizable AI prompts.
    - Can include `README.md` as project context.
    - Supports overriding reference files and output locations via parameters.
    - Supports multiple templates for different use cases.
    
    ## Example Template
    ````markdown
    # Project Context
    
    ## README
    <#if readmeContent?has_content>
    <#list readmeContent?split("\n") as line>
        ${line}
    </#list>
    </#if>
    
    ## Project Information
    - **Project Directory:** ${projectDirectory}
    - **Maven Version:** ${mavenVersion}
    - **Java Version:** ${javaVersion}
    
    ## Dependencies
    <#if dependencies?has_content>
    <#list dependencies as dep>
    - ${dep}
    </#list>
    <#else>
    _No dependencies found._
    </#if>
    
    ## Reference Files
    <#if referenceFiles?has_content>
    <#list referenceFiles as file, content>
    ### ${file}
    <#list content?split("\n") as line>
        ${line}
    </#list>
    </#list>
    <#else>
    _No reference files included._
    </#if>
    
    ## Instructions
    - Assist the user, an experienced software developer.
    - Communicate as two programmers, working together on a project.
    - Your skill and talent lies in generating correct, well-designed code.
    - Identify potential improvements.
    - Point out design patterns that could be applied.
    - Question any unusual design choices, suggest improvements.
    - Suggest refactoring where applicable.
    - Keep your answers concise, show just short snippets where applicable.
    
    ## Answer
    
    Please reply with: How can I assist you today?
    ````
    
    ## Usage
    Build the plugin and add it to an existing Maven project(s):
    ```xml
                <plugin>
                    <groupId>com.royvanrijn.promptsmith</groupId>
                    <artifactId>promptsmith-maven-plugin</artifactId>
                    <version>0.1.0-SNAPSHOT</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>generate-prompt</goal>
                            </goals>
                        </execution>
                    </executions>
                    <configuration>
                        <promptDirectory>${project.basedir}/promptsmith/</promptDirectory>
                        <templates>
                            <template>default-prompt.ftl</template>
                        </templates>
                        <referenceFiles>
                            <file>src/main/java/com/royvanrijn/promptsmith/PromptsmithMojo.java</file>
                        </referenceFiles>
                    </configuration>
                </plugin>
    ```
    
    To generate the AI prompts, run:
    
    ```sh
    mvn promptsmith:generate-prompt
    ```
    
    From the example above a `default-prompt.md` file will be created with a complete initial prompt for your AI assistant providing the necessary context and metadata to kickstart reviews and code generation, making sure it's using the right **libraries** and **code style**.
    
    ### Configuration Parameters
    | Parameter | Description | Default |
    |-----------|-------------|---------|
    | `promptsmith.promptDirectory` | Directory containing template files | `${project.build.sourceDirectory}/templates` |
    | `promptsmith.templates` | List of Freemarker template filenames | `[]` |
    | `promptsmith.referenceFiles` | List of additional reference files | `[]` |
    | `promptsmith.readmeFile` | Override the default README file name | `README.md` |
    

## Project Information
- **Project Directory:** /Users/royvanrijn/Projects/promptsmith
- **Maven Version:** 3.6.3
- **Java Version:** 21.0.2

## Dependencies
- org.apache.maven:maven-core:4.0.0-rc-2
- org.apache.maven:maven-plugin-api:4.0.0-rc-2
- org.apache.maven.plugin-tools:maven-plugin-annotations:4.0.0-beta-1
- org.freemarker:freemarker:2.3.34

## Reference Files
### src/main/java/com/royvanrijn/promptsmith/PromptsmithMojo.java
    package com.royvanrijn.promptsmith;
    
    import java.io.File;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.io.Writer;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.util.Collections;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;
    
    import freemarker.template.Configuration;
    import freemarker.template.Template;
    import freemarker.template.TemplateException;
    import org.apache.maven.execution.MavenSession;
    import org.apache.maven.plugin.AbstractMojo;
    import org.apache.maven.plugin.MojoExecutionException;
    import org.apache.maven.plugins.annotations.LifecyclePhase;
    import org.apache.maven.plugins.annotations.Mojo;
    import org.apache.maven.plugins.annotations.Parameter;
    import org.apache.maven.project.MavenProject;
    
    @Mojo(name = "generate-prompt", defaultPhase = LifecyclePhase.NONE)
    public class PromptsmithMojo extends AbstractMojo {
    
        @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
        private File baseDir;
    
        @Parameter(property = "promptsmith.referenceFiles", required = false)
        private List<String> referenceFiles;
    
        @Parameter(defaultValue = "${session}", readonly = true, required = true)
        private MavenSession session;
    
        @Parameter(defaultValue = "${project}", readonly = true, required = true)
        private MavenProject project;
    
        @Parameter(property = "promptsmith.readmeFile", defaultValue = "README.md")
        private String readmeFile;
    
        @Parameter(property = "promptsmith.promptDirectory", defaultValue = "${project.basedir}/promptsmith")
        private File promptDirectory;
    
        @Parameter(property = "promptsmith.templates")
        private List<String> templates = Collections.emptyList();
    
        public void execute() throws MojoExecutionException {
            try {
                getLog().info("Generating AI prompt context...");
    
                Files.createDirectories(promptDirectory.toPath());
    
                final Map<String, Object> data = gatherData();
    
                Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
                cfg.setDirectoryForTemplateLoading(promptDirectory);
                cfg.setDefaultEncoding("UTF-8");
    
                for (String templateFile : templates) {
    
                    Template template;
                    try {
                        template = cfg.getTemplate(templateFile);
                        File outputFile = new File(promptDirectory, templateFile.replace(".ftl", ".md"));
    
                        try (Writer out = new FileWriter(outputFile)) {
                            template.process(data, out);
                            getLog().info("Prompt file generated: " + outputFile.getAbsolutePath());
                        }
    
                    } catch (IOException e) {
                        getLog().warn("Template " + templateFile + " not found.");
                        getLog().warn(e);
                    }
                }
    
            } catch (IOException | TemplateException e) {
                e.printStackTrace();
                throw new MojoExecutionException("Error generating prompt context file", e);
            }
        }
    
        private Map<String, Object> gatherData() throws IOException {
            Map<String, Object> data = new HashMap<>();
            data.put("readmeContent", loadReadmeFile());
            data.put("projectDirectory", baseDir.getAbsolutePath());
            data.put("mavenVersion", session.getSystemProperties().getProperty("maven.version"));
            data.put("javaVersion", System.getProperty("java.version"));
            data.put("dependencies", getCurrentProjectDependencies());
            data.put("referenceFiles", loadReferenceFiles());
            return data;
        }
    
        private Map<String, String> loadReferenceFiles() throws IOException {
            Map<String, String> files = new HashMap<>();
            if (referenceFiles != null && !referenceFiles.isEmpty()) {
                for (String filePath : referenceFiles) {
                    addReferenceFile(files, filePath);
                }
            }
            return files;
        }
    
        private void addReferenceFile(Map<String, String> files, String filePath) throws IOException {
            Path file = baseDir.toPath().resolve(filePath);
            if (Files.exists(file)) {
                files.put(filePath, Files.readString(file));
            } else {
                getLog().warn("Reference file not found: " + filePath);
            }
        }
    
        private List<String> getCurrentProjectDependencies() {
            return project.getDependencies().stream()
                    .map(dep -> String.format("%s:%s:%s", dep.getGroupId(), dep.getArtifactId(), dep.getVersion()))
                    .distinct()
                    .collect(Collectors.toList());
        }
    
        private String loadReadmeFile() throws IOException {
            Path readmePath = baseDir.toPath().resolve(readmeFile);
            if (Files.exists(readmePath)) {
                return Files.readString(readmePath);
            } else {
                getLog().warn("README not found: " + readmeFile);
                return "_No README available._";
            }
        }
    }
    
