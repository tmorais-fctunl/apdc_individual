package pt.unl.fct.di.apdc.individual.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
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

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private Gson g = new Gson();	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin1(LoginData data, @Context HttpServletRequest request,
			@Context HttpHeaders headers) {

		LOG.fine("Attempt to login user: "+data.getUsername());

		Key userKey = datastore.newKeyFactory().setKind("User")
				.newKey(data.getUsername());

		Key ctrsKey = datastore.newKeyFactory()
				.addAncestors(PathElement.of("User", data.getUsername()))
				.setKind("UserStats").newKey("counters");
		//Geracao automatica do identificador
		Key logKey = datastore.allocateId(
				datastore.newKeyFactory()
				.addAncestors(PathElement.of("User",data.getUsername()))
				.setKind("UserLog").newKey());
		
		

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = datastore.get(userKey);
			if (user == null) {
				//Username does not exist
				LOG.warning("Failed login attempt for username: "+data.getUsername());
				return Response.status(Status.FORBIDDEN).build();
			}
			
			if (user.getString("user_state").equals("DISABLED")) {
				return Response.status(Status.FORBIDDEN).entity("Account disabled").build();
			}
			
			if (user.getBoolean("removed")==true) {
				return Response.status(Status.NOT_FOUND).entity("Account not found (maybe removed)").build();
			}

			//We get the user stats from the storage
			Entity stats = txn.get(ctrsKey);
			if (stats==null) {
				stats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 0L)
						.set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}

			String hashedPWD = user.getString("user_pwd");
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.getPassword()))) {
				//Password is correct
				//Construct logs

				Entity log = Entity.newBuilder(logKey)
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost())
						.set("user_login_latlon",
								//Does not index this property value
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
								.setExcludeFromIndexes(true).build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", Timestamp.now())
						.build();
				//get the user statistics and updates it
				//Copying information every time a user logins is not a good idea, correct this asap
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 1L+stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 0L)
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now())
						.build();
				//Batch operation
				txn.put(log,ustats);
				
				//Return token
				AuthToken token = new AuthToken(data.getUsername(), user.getString("user_role"));
				String tokenID = DigestUtils.sha512Hex(token.getTokenID());
				Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(tokenID);
				Entity entityToken = Entity.newBuilder(tokenKey)
						.set("token_id", tokenID)
						.set("creation_time", token.getCreationData())
						.set("expiration_time", token.getExpirationData())
						.set("username", token.getUsername())
						.set("user_role", token.getRole())
						.build();
				txn.put(entityToken);
				txn.commit();
				
				
				LOG.info("User '"+data.getUsername()+"' logged in successfully.");
				return Response.ok(g.toJson(token)).build();
			}
			else {
				//Incorrect password
				//copying again here, correct this also
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 1L + stats.getLong("user_stats_logins"))
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", stats.getTimestamp("user_last_login"))
						.set("user_last_attempt", Timestamp.now())
						.build();
				txn.put(ustats);
				txn.commit();
				LOG.warning("Wrong password for username: "+data.getUsername());
				return Response.status(Status.FORBIDDEN).build();
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





