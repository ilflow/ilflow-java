package org.ilflow.compiler;

import org.ilflow.model.ProcessParameter;
import org.ilflow.model.Variable;

public class ParameterCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private String output;

    ParameterCompiler(TemplateCompiler templateCompiler, String template) {
        this.templateCompiler = templateCompiler;
        this.template = template;
    }

    boolean translate(ProcessParameter parameter) {
        String result = template;
        output = "";

        Process sub = null; //project.FindProcess(node.Subprocess);

        if (sub == null) return true; //false;

        Variable var = null; //sub.FindVar(m_Name);

        if (var == null) return false;

        if (parameter.value.equals("")) return false;

        result = templateCompiler.Replace(result, "cparamname", parameter.name);
        String scope;
        switch (var.level)
        {
            case GLOBAL: scope = "g"; break;
            case INTERMEDIATE: scope = "i"; break;
            case LOCAL: scope = "l"; break;
            default: return false;
        }
        result = templateCompiler.Replace(result, "cparamscope", scope);
        result = templateCompiler.Replace(result, "cparamvalue", parameter.value);// node.ParseFunction(project, process, parameter.value));

        output = result;

        return true;
    }

    String getOutput() {
        return output;
    }
}
