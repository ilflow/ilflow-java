package org.ilflow.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ilflow.model.Project;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IlFlowRepository implements IlFlowRepositoryInterface {
    private static IlFlowRepository ourInstance = new IlFlowRepository();

    public static IlFlowRepository getInstance() {
        return ourInstance;
    }

    private String m_solutionPath = "C:/ILNET/Projects/";

    private IlFlowRepository() {
        if (!new File(m_solutionPath).exists()) new File(m_solutionPath).mkdirs();
    }

    private String PackagePathname(String p) {
        return p.replace('.', '/');
    }

    private static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    public Project GetProject(String projectName) {
        if (null == projectName) return null;

        String realName = m_solutionPath + PackagePathname(projectName) + ".json";

        File file = new File(realName);
        if (!file.exists()) return null;

        String result = null;
        try {
            byte[] content = Files.readAllBytes(Paths.get(realName));
            result = new String(content, Charset.defaultCharset());
        } catch (IOException e) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        Project project = null;

        try {
            project = objectMapper.readValue(result, Project.class);
        } catch (IOException e) {
            return null;
        }

        return project;
    }

    public Boolean SaveProject(Project project) {
        if (null == project.name) return false;

        String pathname = PackagePathname(project.name);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String source = objectMapper.writeValueAsString(project);

            FileWriter out = new FileWriter(m_solutionPath + pathname + ".json");
            out.write(source);
            out.close();
        } catch (IOException e) {
            return false;
        }
        System.out.println("Project " + project.name + " saved");
        return true;
    }

    public Boolean DeleteProject(String name) {
        if (null == name) return false;

        String pathname = PackagePathname(name);

        boolean result = new File(m_solutionPath + pathname + ".json").delete();

        System.out.println("Project " + name + " removed");

        return result;
    }

    public String[] GetProjectNames(String path) {
        if (null == path) return null;

        String realPath = m_solutionPath + PackagePathname(path);
        File folder = new File(realPath);
        if (!folder.exists()) return null;

        List<String> result = new ArrayList<>();

        File[] files = folder.listFiles((File dir, String name) ->
                name.toLowerCase().endsWith(".json")
        );

        for (File file : files) {
            if (file.isFile()) {
                result.add(getBaseName(file.getName()));
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public String[] GetFolderNames(String path) {
        if (null == path) return null;

        String realPath = m_solutionPath + PackagePathname(path);
        File folder = new File(realPath);
        if (!folder.exists()) return null;

        List<String> result = new ArrayList<>();

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                result.add(file.getName());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public Boolean CreateFolder(String path) {
        if (null == path) return false;

        String realPath = m_solutionPath + PackagePathname(path);

        File dir = new File(realPath);

        if (!dir.exists()) {
            return dir.mkdirs();
        } else {
            return dir.isDirectory();
        }
    }

    public Boolean DeleteFolder(String path) {
        if (null == path) return false;

        path = PackagePathname(path);
//        if (!Directory.Exists(path)) return false;
//        try
//        {
//            Directory.Delete(path);
//        }
//        catch
//        {
//            return false;
//        }
        return true;
    }
}
