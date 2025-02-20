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
