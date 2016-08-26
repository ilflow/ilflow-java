package org.ilflow.bridge;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    static public void CloneHashDeep(HashMap table)
    {
        Object[] keys = new Object[table.size()];
        for (Object o : table.keySet())
        {
//            if (table.get(o) instanceof Cloneable)
//            {
//                table.put(o, ((Cloneable)table.get(o)).clone());
//            }

            if (table.get(o) instanceof HashMap) {
                table.put(o, ((HashMap)table.get(o)).clone());
                CloneHashDeep((HashMap)table.get(o));
            }
        }
    }

    static public HashMap Clone(HashMap table)
    {
        HashMap result = (HashMap)table.clone();

        CloneHashDeep(result);
        return result;
    }

    public static Map Clone(Map data) {
        return data;
    }
}
