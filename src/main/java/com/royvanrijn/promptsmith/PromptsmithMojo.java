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

@Mojo(name = "generate-prompt", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
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
            String content = Files.readString(file); // No formatting the contents.
            files.put(filePath, content);
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
            String content = Files.readString(readmePath);
            return content;
        } else {
            getLog().warn("README not found: " + readmeFile);
            return "_No README available._";
        }
    }
}
