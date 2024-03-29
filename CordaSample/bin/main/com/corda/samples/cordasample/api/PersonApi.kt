package com.corda.samples.cordasample.api

import com.corda.samples.cordasample.flow.CreatePerson.CreatePersonFlow
import com.corda.samples.cordasample.state.PersonState
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

// http://localhost:12002/api/person/all
// http://localhost:12002/api/person/create
// This API is accessible from /api/person. All paths specified below are relative to it.

@Path("person")
class PersonApi(private val rpcOps: CordaRPCOps) {

    companion object {
        private val logger: Logger = loggerFor<PersonApi>()
    }

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeople(): Response {
        return Response.status(Response.Status.OK).entity(rpcOps.vaultQueryBy<PersonState>().states).build()
    }

    @PUT
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    fun createPerson(data: CreateRequestModel): Response {

        if (data.document.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity( ResponseModel("Documento inválido") ).build()
        }

        return try {

            val signedTx = rpcOps.startTrackedFlow(::CreatePersonFlow, data.name, data.document).returnValue.getOrThrow()
            Response.status(Response.Status.CREATED).entity(signedTx.tx.outputs.single()).build()

        } catch (ex: Throwable) {

            logger.error(ex.message, ex)
            Response.status(Response.Status.BAD_REQUEST).entity( ResponseModel(ex.message.toString()) ).build()

        }
    }
}

data class CreateRequestModel (
    val name : String,
    val document : String
)

data class ResponseModel (
    val message : String
)