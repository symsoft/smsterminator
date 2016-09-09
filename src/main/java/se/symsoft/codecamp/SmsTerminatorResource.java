/*
 * Copyright Symsoft AB 1996-2015. All Rights Reserved.
 */
package se.symsoft.codecamp;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import se.symsoft.cc2016.logutil.Logged;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.util.UUID;

@Path("terminators")
@Api(tags = {"SMS Terminators"})
public class SmsTerminatorResource {

    @Context
    Application app;

    private DynamoDBMapper getDynamoDB() {
        return ((SmsTerminatorService) app).getDynamoDB();
    };

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void list(@Suspended final AsyncResponse asyncResponse) {
        PaginatedScanList<TerminatorData> pageList = getDynamoDB().scan(TerminatorData.class, new DynamoDBScanExpression());
        asyncResponse.resume(pageList);
    }

    @GET
    @Path("ping")
    public void ping(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume("Pong");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create SMS Terminator",
            notes = "Create a new SMS Terminator")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = SmsResponse.class) })
    @Logged
    public void create(@Suspended final AsyncResponse asyncResponse,
                       @ApiParam(value = "Attributes of the SMS Terminator", required = true)
                       final TerminatorData data) throws URISyntaxException {
        data.setId(UUID.randomUUID());
        try {
            getDynamoDB().save(data);
            asyncResponse.resume(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PUT
    @ApiOperation(value = "Update SMS Terminator",
            notes = "Update an existing SMS Terminator")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = SmsResponse.class),
            @ApiResponse(code = 404, message = "SMS terminator not found", response = SmsResponse.class)
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Logged
    public void update(@Suspended final AsyncResponse asyncResponse, final TerminatorData data) throws URISyntaxException {
        try {
            final TerminatorData generatorData = getDynamoDB().load(TerminatorData.class, data.getId());
            if (generatorData == null) {
                asyncResponse.resume(new NotFoundException("Entity with id = " + data.getId() + " not found"));
                return;
            }
            getDynamoDB().save(data);

            asyncResponse.resume(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DELETE
    @ApiOperation(value = "Delete SMS Terminator",
            notes = "Delete an existing SMS Terminator")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = SmsResponse.class),
            @ApiResponse(code = 404, message = "SMS terminator not found", response = SmsResponse.class)
    })
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public void delete(@Suspended final AsyncResponse asyncResponse, @PathParam("id") UUID id) {
        try {
            final TerminatorData generatorData = getDynamoDB().load(TerminatorData.class, id);
            if (generatorData != null) {
                getDynamoDB().delete(generatorData);
                asyncResponse.resume(generatorData);
            } else {
                asyncResponse.resume(new NotFoundException("Entity with id = " + id + " not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
