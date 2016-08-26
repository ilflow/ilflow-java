package org.ilflow.compiler;

import org.ilflow.model.Variable;

class VariableResultCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private String output;

    VariableResultCompiler(TemplateCompiler templateCompiler, String template) {
        this.templateCompiler = templateCompiler;
        this.template = template;
    }

    boolean translate(Variable variable) {
        String result = template;
        
        if (!variable.isOutput) {
            output = "";
            return true;
        }

        result = templateCompiler.Replace(result, "cvarname", variable.name);
        result = templateCompiler.Replace(result, "cvarscope", VariableScoping.getScopePrefix(variable));
        output = result;
        return true;
    }

    String getOutput() {
        return output;
    }
}
