package org.ilflow.compiler;

import org.ilflow.model.Node;
import org.ilflow.model.ProcessParameter;

class NodeCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private ParameterCompiler parameterCompiler;

    private String output;

    NodeCompiler(TemplateCompiler templateCompiler, String template) {
        this.templateCompiler = templateCompiler;
        this.template = template;

        parameterCompiler = new ParameterCompiler(templateCompiler, templateCompiler.Extract(template, "csubparam"));
    }

    boolean translate(Node node) {
        String result = template;

        result = templateCompiler.Replace(result, "cnodename", node.name);
        result = templateCompiler.Replace(result, "cnodefnname", "fn_" + node.name);
        result = templateCompiler.Replace(result, "cnodefnnamecb", "fn_" + node.name + "_cb");

        if (node.nodeIsSubProcess)
        {
            result = templateCompiler.Replace(result, "cnodefncode", "");
            result = templateCompiler.Replace(result, "cnodefncodecb", node.function); //! ParseFunction(project, process, node.function));
        }
        else
        {
            result = templateCompiler.Replace(result, "cnodefncode", node.function); //! ParseFunction(project, process, node.function));
            result = templateCompiler.Replace(result, "cnodefncodecb", "");
        }
        // Parameters
        if (node.nodeIsSubProcess)
        {
            result = templateCompiler.Replace(result, "cnodeisfn", "");

            StringBuilder code = new StringBuilder();

            for (ProcessParameter item : node.processParameters)
            {
                if(!parameterCompiler.translate(item)) return false;
                code.append(parameterCompiler.getOutput());
            }

            result = templateCompiler.Replace(result, "csubparam", code.toString());

            if (node.process.contains("."))
            {
                result = templateCompiler.Replace(result, "csubname", "dynamic");
                result = templateCompiler.Replace(result, "cnodeisinternal", "");

                String processName = node.process.substring(node.process.lastIndexOf(".") + 1);
                String projectName = node.process.substring(0, node.process.length() - processName.length() - 1);

                result = templateCompiler.Replace(result, "csubprojectname", projectName);
                result = templateCompiler.Replace(result, "csubprocessname", processName);
                result = templateCompiler.Replace(result, "cnodefnproxy", "fn_" + node.name + "_proxy");
            }
            else
            {
                result = templateCompiler.Replace(result, "csubname", "c_" + node.process);
                result = templateCompiler.Replace(result, "cnodeisexternal", "");
            }
        }
        else
        {
            result = templateCompiler.Replace(result, "cnodeissubprocess", "");
            result = templateCompiler.Replace(result, "cnodeisexternal", "");
        }
        // Connections
        {
            String shell_goto = templateCompiler.Extract(result, "cnodefngoto");

            StringBuilder code = new StringBuilder();

            for (String item : node.nextNodes)
            {
                code.append(templateCompiler.Replace(shell_goto, "cnodefngotoname", "fn_" + item));
            }

            result = templateCompiler.Replace(result, "cnodefngoto", code.toString());
        }

        output = result;
        return true;
    }

    String getOutput() {
        return output;
    }
}
