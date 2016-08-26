package org.ilflow.compiler;

import org.ilflow.model.Variable;

class VariableScoping {
    static String getScopePrefix(Variable variable)
    {
        switch (variable.level)
        {
            case GLOBAL: return "g";
            case INTERMEDIATE: return "i";
            case LOCAL: return "l";
            default: return "";
        }
    }
}
