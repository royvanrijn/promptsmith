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

### Result
From the example, for this project, creates [default-prompt.md](promptsmith/default-prompt.md). A complete initial prompt for your AI assistant providing the necessary context and metadata to kickstart reviews and code generation, making sure it's using the right **libraries** and **code style**.

### Configuration Parameters
| Parameter | Description | Default |
|-----------|-------------|---------|
| `promptsmith.promptDirectory` | Directory containing template files | `${project.build.sourceDirectory}/templates` |
| `promptsmith.templates` | List of Freemarker template filenames | `[]` |
| `promptsmith.referenceFiles` | List of additional reference files | `[]` |
| `promptsmith.readmeFile` | Override the default README file name | `README.md` |
