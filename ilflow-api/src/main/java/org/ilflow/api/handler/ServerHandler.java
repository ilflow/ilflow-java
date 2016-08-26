package org.ilflow.api.handler;

import io.swagger.annotations.*;
import io.swagger.annotations.ApiResponse;
import org.ilflow.repository.IlFlowRepository;
import org.ilflow.server.IlFlowServer;
import org.ilflow.server.IlFlowServerInterface;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;

@Path("/server")
@Api(value = "/server",
        authorizations = {@Authorization(value = "api_key")},
        description = "Server operations",
        tags = "server")
@Produces({MediaType.APPLICATION_JSON})
public class ServerHandler {
    static IlFlowServerInterface server = new IlFlowServer(IlFlowRepository.getInstance());

    @GET
    @Path("/load/project/{name}")
    @ApiOperation(value = "Load a project into the server",
            response = Long.class,
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid name supplied"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    public Response getPetById(
            @ApiParam(value = "Name of the project to be loaded", required = true) @PathParam("name") String name) {

        long result = server.Load(name);

        if (0 != result) {
            return Response.ok().entity(result).build();
        } else {
            return Response.status(404).build();
        }
    }
/*
    @DELETE
    @Path("/{petId}")
    @ApiOperation(value = "Deletes a pet")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found")})
    public Response deletePet(
            @ApiParam() @HeaderParam("api_key") String apiKey,
            @ApiParam(value = "Pet id to delete", required = true) @PathParam("petId") Long petId) {
        //if (petData.deletePet(petId)) {
        return Response.ok().build();
        //    } else {
        //      return Response.status(Response.Status.NOT_FOUND).build();
        //    }
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Add a new pet to the store")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public Response addPet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true) String pet) {
        String updatedPet = "";
        return Response.ok().entity(updatedPet).build();
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Update an existing pet")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found"),
            @ApiResponse(code = 405, message = "Validation exception")})
    public Response updatePet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true) String pet) {
        String updatedPet = "";
        return Response.ok().entity(updatedPet).build();
    }

    @GET
    @Path("/findByStatus")
    @ApiOperation(value = "Finds Pets by status",
            notes = "Multiple status values can be provided with comma separated strings",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid status value")})
    public Response findPetsByStatus(
            @ApiParam(value = "Status values that need to be considered for filter", required = true, defaultValue = "available", allowableValues = "available,pending,sold", allowMultiple = true) @QueryParam("status") String status) {
        return Response.ok("").build();
    }

    @GET
    @Path("/findByTags")
    @ApiOperation(value = "Finds Pets by tags",
            notes = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.",
            response = String.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid tag value")})
    @Deprecated
    public Response findPetsByTags(
            @HeaderParam("api_key") String api_key,
            @ApiParam(value = "Tags to filter by", required = true, allowMultiple = true) @QueryParam("tags") String tags) {
        return Response.ok("").build();
    }

    @POST
    @Path("/{petId}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @ApiOperation(value = "Updates a pet in the store with form data",
            consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @ApiResponses(value = {
            @ApiResponse(code = 405, message = "Invalid input")})
    public Response updatePetWithForm(
            @ApiParam(value = "ID of pet that needs to be updated", required = true) @PathParam("petId") Long petId,
            @ApiParam(value = "Updated name of the pet", required = false) @FormParam("name") String name,
            @ApiParam(value = "Updated status of the pet", required = false) @FormParam("status") String status) {
        String pet = "";
        if (pet != null) {
            return Response.ok().build();
        } else
            return Response.status(404).build();
    }
    */
}