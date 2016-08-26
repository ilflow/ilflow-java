package org.ilflow.repository;

import org.ilflow.model.Project;

import java.util.HashMap;

public interface IlFlowRepositoryInterface {
    Project GetProject(String projectName);

    Boolean SaveProject(Project project);

    Boolean DeleteProject(String projectName);

    String[] GetProjectNames(String path);

    String[] GetFolderNames(String path);

    Boolean CreateFolder(String path);

    Boolean DeleteFolder(String path);

}
