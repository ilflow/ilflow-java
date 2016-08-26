package org.ilflow.api.handler;

import io.swagger.annotations.*;
import org.ilflow.model.Project;
import org.ilflow.repository.IlFlowRepository;
import org.ilflow.repository.IlFlowRepositoryInterface;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/repository")
@Api(value = "/repository",
        authorizations = {@Authorization(value = "api_key")},
        description = "Repository operations",
        tags = "repository")
@Produces({MediaType.APPLICATION_JSON})
public class RepositoryHandler {
    private static IlFlowRepositoryInterface repository = IlFlowRepository.getInstance();

    @GET
    @Path("/project/{name}")
    @ApiOperation(value = "Find project source by name",
            response = Project.class,
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid name supplied"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    public Response getProjectByName(
            @ApiParam(value = "Name of the project to be fetched", required = true) @PathParam("name") String name) {

        Project result = repository.GetProject(name);

        if (null != result) {
            return Response.ok().entity(result).build();
        } else {
            return Response.status(404).build();
        }
    }

    @POST
    @Path("/project")
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Save a project to the repository")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid project supplied"),
    })
    public Response createProject(
            @ApiParam(value = "Project object that needs to be saved to the repository", required = true) Project project) {

        boolean result = repository.SaveProject(project);

        if (result) {
            return Response.ok().build();
        } else {
            return Response.status(400).build();
        }
    }

    @GET
    @Path("/projects")
    @ApiOperation(value = "Find project names by path",
            response = String[].class,
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid path supplied"),
            @ApiResponse(code = 404, message = "Path not found")
    })
    public Response getProjectNamesByPath(
            @ApiParam(value = "Path in which to search") @QueryParam("path") String path) {

        if (null == path) path = "";

        String[] result = repository.GetProjectNames(path);

        if (null != result) {
            return Response.ok().entity(result).build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Path("/folders")
    @ApiOperation(value = "Find folder names by path",
            response = String[].class,
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid path supplied"),
            @ApiResponse(code = 404, message = "Path not found")
    })
    public Response getFolderNamesByPath(
            @ApiParam(value = "Path in which to search") @QueryParam("path") String path) {

        if (null == path) path = "";

        String[] result = repository.GetFolderNames(path);

        if (null != result) {
            return Response.ok().entity(result).build();
        } else {
            return Response.status(404).build();
        }
    }

    @POST
    @Path("/folder")
    @Consumes({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Save a folder to the repository")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid path supplied"),
    })
    public Response createProject(
            @ApiParam(value = "Folder path that needs to be saved to the repository", required = true) String path) {

        boolean result = repository.CreateFolder(path);

        if (result) {
            return Response.ok().build();
        } else {
            return Response.status(400).build();
        }
    }
}