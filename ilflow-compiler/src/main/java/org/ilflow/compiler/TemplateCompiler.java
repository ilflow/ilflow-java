package org.ilflow.compiler;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateCompiler {
    private String template;

    private HashMap<String, CompiledPatterns> compiledPatterns;

    public TemplateCompiler() {
        compiledPatterns = new HashMap<>();
    }

    private class CompiledPatterns {
        private Pattern rs;
        private Pattern re;
    }

    private CompiledPatterns GetCompiledPatterns(String id) {
        if(!compiledPatterns.containsKey(id)) {
            CompiledPatterns cp = new CompiledPatterns();

            cp.rs = Pattern.compile("/\\*" + id + "\\*/", Pattern.DOTALL);
            cp.re = Pattern.compile("/\\*/" + id + "\\*/", Pattern.DOTALL);

            compiledPatterns.put(id,cp);
        }

        return compiledPatterns.get(id);
    }

    public boolean readTemplate() {
        template = "";

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try {
            java.net.URL url = classloader.getResource("Template.java");
            Path resPath = Paths.get(url.toURI());

            template = new String(Files.readAllBytes(resPath), Charset.defaultCharset());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public String getTemplate() {
        return template;
    }

    public String Replace(String text, String id, String with) {
        StringBuilder res = new StringBuilder();

        CompiledPatterns cp = GetCompiledPatterns(id);

        int pos = 0;

        Matcher ms = cp.rs.matcher(text);
        Matcher me = cp.re.matcher(text);

        while (ms.find()) {
            int start = ms.start();

            if (start - pos > 0) res.append(text.substring(pos, start));

            res.append(with);

            if (me.find(start)) {
                pos = me.end();
            } else {
                pos = text.length();
            }
        }

        if (text.length() - pos > 0) res.append(text.substring(pos));

        return res.toString();
    }

    public String Extract(String text, String id, int from) {
        CompiledPatterns cp = GetCompiledPatterns(id);

        Matcher ms = cp.rs.matcher(text);
        Matcher me = cp.re.matcher(text);

        if (ms.find(from)) {
            int start = ms.end();
            int end;

            if (me.find(start)) {
                end = me.start();
            } else {
                end = text.length() - start;
            }

            return text.substring(start, end);
        }

        return "";
    }

    public String Extract(String text, String id) {
        return Extract(text, id, 0);
    }

}
