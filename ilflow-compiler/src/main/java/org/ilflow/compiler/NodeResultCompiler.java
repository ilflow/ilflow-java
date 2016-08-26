package org.ilflow.compiler;

import org.ilflow.model.Node;

class NodeResultCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private String output;

    NodeResultCompiler(TemplateCompiler templateCompiler, String template) {
        this.templateCompiler = templateCompiler;
        this.template = template;
    }

    boolean translate(Node node) {
        String result = template;

        if (!node.nodeIsSubProcess) {
            output = "";
            return true;
        }

        result = templateCompiler.Replace(result, "cnodename", node.name);

        output = result;
        return true;
    }

    String getOutput() {
        return output;
    }
}
