/*
 * Copyright Symsoft AB 1996-2015. All Rights Reserved.
 */
package se.symsoft.codecamp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import se.symsoft.cc2016.logutil.Logged;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.UUID;

@Path("deliver")
@Api(tags = {"SMS Delivery"})
public class SmsDeliverResource {

    @Context
    Application app;

    @POST
    @Path("{realm}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deliver SMS",
            notes = "Deliver an SMS to the Terminator")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = SmsResponse.class) })
    @Logged
    public void deliver(@Context Application app, @Suspended final AsyncResponse asyncResponse,
                        @PathParam("realm")
                        @ApiParam(value = "The realm to which the SMS shall be delivered", required = true)
                        String realm,
                        @ApiParam(value = "Attributes of SMS to be delivered", required = true) SmsSubmit data) throws URISyntaxException {
        try {
            TerminatorData terminatorData = ((SmsTerminatorService) app).getTerminatorData(realm);
            if (terminatorData == null) {
                asyncResponse.resume(new NotFoundException("Realm " + realm + "does not exist"));
                return;
            }
            int rand = new Random().nextInt(100);

            SmsResponse response = new SmsResponse();
            response.setResponseCode(rand < terminatorData.getSuccessRate() ? 0 : 1);
            response.setRefId(UUID.randomUUID());
            response.setServiceCentreTime(System.currentTimeMillis());
            asyncResponse.resume(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("ping")
    public void ping(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume("Pong");
    }


}
