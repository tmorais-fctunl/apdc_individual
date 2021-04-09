package pt.unl.fct.di.apdc.individual.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import com.google.cloud.datastore.StringValue;

import java.util.logging.Logger;

import pt.unl.fct.di.apdc.individual.util.AuthToken;
import pt.unl.fct.di.apdc.individual.util.LoginData;
import pt.unl.fct.di.apdc.individual.util.LogoutData;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogout(LogoutData data) {
		String tokenID = DigestUtils.sha512Hex(data.getToken());
		Key authKey = datastore.newKeyFactory().setKind("Token").newKey(tokenID);

		Transaction txn = datastore.newTransaction();
		try {
			Entity token = txn.get(authKey);

			if (token == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("No session in progress").build();
			}

			if (!tokenID.equals(token.getString("token_id")) | !token.getString("username").equals(data.getUsername())) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Invalid credentials").build();
			}

			if (tokenID.equals(token.getString("token_id")) && 
					data.getUsername().equals(token.getString("username")) && 
					token.getLong("expiration_time")>System.currentTimeMillis()) {

				Entity updatedToken = Entity.newBuilder(authKey)
						.set("token_id", tokenID)
						.set("creation_time", token.getLong("creation_time"))
						.set("expiration_time", System.currentTimeMillis())
						.set("username", token.getString("username"))
						.set("user_role", token.getString("user_role"))
						.build();
				txn.put(updatedToken);
				txn.commit();
				LOG.info("User '"+data.getUsername()+"' logged out successfully.");
				return Response.ok("Logout").build();
			}

			else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("You can not log out if you are already logged out!").build();
			}


		}
		catch(Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}





}
