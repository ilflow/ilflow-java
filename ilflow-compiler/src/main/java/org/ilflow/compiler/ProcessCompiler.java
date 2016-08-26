package org.ilflow.compiler;

import org.ilflow.model.Node;
import org.ilflow.model.Process;
import org.ilflow.model.Variable;

class ProcessCompiler {
    private TemplateCompiler templateCompiler;
    private String template;

    private NodeCompiler nodeCompiler;
    private VariableCompiler variableCompiler;
    private VariableCompiler globalVariableCompiler;
    private NodeResultCompiler nodeResultCompiler;
    private VariableResultCompiler variableResultCompiler;

    private String output;

    ProcessCompiler(TemplateCompiler templateCompiler, String template) {
        this.templateCompiler = templateCompiler;
        this.template = template;
        this.nodeCompiler = new NodeCompiler(templateCompiler, templateCompiler.Extract(template, "cnodefn"));
        this.variableCompiler = new VariableCompiler(templateCompiler, templateCompiler.Extract(template, "varinit"));
        this.globalVariableCompiler = new VariableCompiler(templateCompiler, templateCompiler.Extract(template, "gvarinit"));
        this.nodeResultCompiler = new NodeResultCompiler(templateCompiler, templateCompiler.Extract(template, "cnoderesult"));
        this.variableResultCompiler = new VariableResultCompiler(templateCompiler, templateCompiler.Extract(template, "cvarresult"));
    }

    boolean translate(Process process) {
        String result = template;

        result = templateCompiler.Replace(result, "cname", "c_" + process.name);
        result = templateCompiler.Replace(result, "centry", "fn_" + process.entryNode);

        // Function calls:
        {
            StringBuilder code = new StringBuilder();

            for (Node item : process.nodes)
            {
                if(!nodeCompiler.translate(item)) return false;
                String node_code = nodeCompiler.getOutput();

                if (!item.name.equals(process.exitNode))
                {
                    node_code = templateCompiler.Replace(node_code, "cnodeisfinal", "");
                }

                code.append(node_code);
            }

            result = templateCompiler.Replace(result, "cnodefn", code.toString());
        }

        // Variable init:
        {
            StringBuilder code = new StringBuilder();

            for (Variable item : process.variables)
            {
                if (item.level == Variable.Level.GLOBAL) continue;
                if(!variableCompiler.translate(item)) return false;
                code.append(variableCompiler.getOutput());
            }

            result = templateCompiler.Replace(result, "varinit", code.toString());

            code = new StringBuilder();

            for (Variable item : process.variables)
            {
                if (item.level != Variable.Level.GLOBAL) continue;
                if(!globalVariableCompiler.translate(item)) return false;
                code.append(globalVariableCompiler.getOutput());
            }

            result = templateCompiler.Replace(result, "gvarinit", code.toString());
        }

        // Transition tables:
        {
            String shell_function = templateCompiler.Extract(result, "transition");

            StringBuilder code = new StringBuilder();

            for (Node startNode : process.nodes)
            {
                for (String endNodeName : startNode.nextNodes)
                {
                    String transition_code = templateCompiler.Replace(shell_function, "cnodestartname", startNode.name);
                    transition_code = templateCompiler.Replace(transition_code, "cnodeendname", endNodeName);

                    code.append(transition_code);
                }
            }

            result = templateCompiler.Replace(result, "transition", code.toString());
        }

        // Node names:
        {
            String shell_function = templateCompiler.Extract(result, "nodename");

            StringBuilder code = new StringBuilder();

            for (Node item : process.nodes)
            {
                {
                    String nodename_code = templateCompiler.Replace(shell_function, "cnodecodename", "fn_" + item.name);
                    nodename_code = templateCompiler.Replace(nodename_code, "cnoderealname", item.name);
                    code.append(nodename_code);
                }
                {
                    String nodename_code = templateCompiler.Replace(shell_function, "cnodecodename", "fn_" + item.name + "_cb");
                    nodename_code = templateCompiler.Replace(nodename_code, "cnoderealname", item.name);
                    code.append(nodename_code);
                }
            }

            result = templateCompiler.Replace(result, "nodename", code.toString());
        }

        // Result passing:
        {
            String shell_results = templateCompiler.Extract(result, "cresult");

            StringBuilder code = new StringBuilder();

            for (Variable item : process.variables)
            {
                if(!variableResultCompiler.translate(item)) return false;
                code.append(variableResultCompiler.getOutput());
            }

            shell_results = templateCompiler.Replace(shell_results, "cvarresult", code.toString());

            code = new StringBuilder();

            for (Node item : process.nodes)
            {
                if(!nodeResultCompiler.translate(item)) return false;
                code.append(nodeResultCompiler.getOutput());
            }

            shell_results = templateCompiler.Replace(shell_results, "cnoderesult", code.toString());

            result = templateCompiler.Replace(result, "cresult", shell_results);
        }

        output = result;

        return true;
    }

    String getOutput() {
        return output;
    }
}
