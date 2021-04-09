package pt.unl.fct.di.apdc.individual.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;


import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import pt.unl.fct.di.apdc.individual.util.ListByRoleData;
import pt.unl.fct.di.apdc.individual.util.ModifyStateData;
import pt.unl.fct.di.apdc.individual.util.RegisterData;

@Path("/listbyrole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListByRoleResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private Gson g = new Gson();

	@POST
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doListByRole(ListByRoleData data) {

		String tokenID = DigestUtils.sha512Hex(data.getToken());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(tokenID);
		try {
			Entity token = datastore.get(tokenKey);
			if (token == null) {
				return Response.status(Status.NOT_FOUND).entity("No session in progress").build();
			}

			if (!tokenID.equals(token.getString("token_id")) | !token.getString("username").equals(data.getUsername())) {
				return Response.status(Status.FORBIDDEN).entity("Invalid credentials").build();
			}

			if (tokenID.equals(token.getString("token_id")) 
					&& data.getUsername().equals(token.getString("username")) 
					&& token.getLong("expiration_time")<=System.currentTimeMillis()) {
				return Response.status(Status.FORBIDDEN).entity("Session expired").build();
			}

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
			Entity user = datastore.get(userKey);

			if (user==null) {
				return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
			}

			if (user.getString("user_state").equals("DISABLED")) {
				return Response.status(Status.FORBIDDEN).entity("Account disabled").build();
			}

			if (user.getBoolean("removed")==true) {
				return Response.status(Status.NOT_FOUND).entity("Account not found (maybe removed)").build();
			}

			if (user.getString("user_role").equals("USER")) {
				return Response.status(Status.FORBIDDEN).entity("No permission.").build();
			}


			if (!(data.getRole().equals("USER") || data.getRole().equals("GBO") || data.getRole().equals("GA") || data.getRole().equals("SU"))) {
				return Response.status(Status.BAD_REQUEST).entity("Invalid role").build();
			}
			
			Query<Entity> query = Query.newEntityQueryBuilder()
	                .setKind("User")
	                .setFilter(PropertyFilter.eq("user_role", data.getRole()))
	                .build();

	        QueryResults<Entity> res = datastore.run(query);

	        List<String> users = new ArrayList<>();
	        
	        res.forEachRemaining(usernames -> {
	            users.add(usernames.getString(("user_name")));
	        });
	        
	        return Response.status(Status.ACCEPTED).entity(g.toJson(users)).build();








		}
		catch(Exception e) {
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}



}
