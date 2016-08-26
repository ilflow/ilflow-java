package org.ilflow.compiler;

import org.ilflow.model.Variable;

class VariableCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private String output;

    VariableCompiler(TemplateCompiler templateCompiler, String template) {
        this.templateCompiler = templateCompiler;
        this.template = template;
    }

    boolean translate(Variable variable) {
        String result = template;
        output = "";

        String value = variable.value;
        if (value.equals(""))
        {
            if (variable.type.equals("")) {
                return false;
            }

            if (variable.type.equals("String")) {
                value = "\"\"";
            } else if (!variable.type.contains("[") && !variable.type.contains("]")) {
                value = "new " + variable.type + "()";
            }
            else {
                return false;
            }
        }

        result = templateCompiler.Replace(result, "cvarname", variable.name);
        result = templateCompiler.Replace(result, "cvarscope", VariableScoping.getScopePrefix(variable));
        result = templateCompiler.Replace(result, "cvarvalue", value);
        output = result;

        return true;
    }

    String getOutput() {
        return output;
    }
}