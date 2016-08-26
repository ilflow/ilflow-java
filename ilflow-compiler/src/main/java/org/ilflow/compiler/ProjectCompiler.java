package org.ilflow.compiler;

import org.ilflow.model.Project;
import org.ilflow.model.Process;

public class ProjectCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private ProcessCompiler processCompiler;

    private String output;

    public ProjectCompiler(TemplateCompiler templateCompiler) {
        template = templateCompiler.getTemplate();
        this.templateCompiler = templateCompiler;
        this.processCompiler = new ProcessCompiler(templateCompiler, templateCompiler.Extract(template, "class"));
    }

    public boolean translate(Project project) {
        String result = template;
        result = templateCompiler.Replace(result, "skip", "");

        result = templateCompiler.Replace(result, "sclass", "c_" + project.runProcess);

        StringBuilder code = new StringBuilder();

        for(Process item : project.processes)
        {
            if(!processCompiler.translate(item)) return false;
            code.append(processCompiler.getOutput());
        }

        result = templateCompiler.Replace(result, "class", code.toString());

        output = result;

        return true;
    }

    public String getOutput() {
        return output;
    }
}
