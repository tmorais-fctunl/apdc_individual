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
import com.google.cloud.datastore.Transaction;

import java.util.logging.Logger;

import pt.unl.fct.di.apdc.individual.util.ModifyRoleData;
import pt.unl.fct.di.apdc.individual.util.RegisterData;


@Path("/modifyrole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyRoleResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doModifyRole(ModifyRoleData data) {
		String tokenID = DigestUtils.sha512Hex(data.getToken());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			Entity token = txn.get(tokenKey);
			if (token == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("No session in progress").build();
			}

			if (!tokenID.equals(token.getString("token_id")) | !token.getString("username").equals(data.getUsername())) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Invalid credentials").build();
			}

			if (tokenID.equals(token.getString("token_id")) 
					&& data.getUsername().equals(token.getString("username")) 
					&& token.getLong("expiration_time")<=System.currentTimeMillis()) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Session expired").build();
			}

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
			Entity user = txn.get(userKey);

			if (user==null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
			}
			
			if (user.getString("user_state").equals("DISABLED")) {
				return Response.status(Status.FORBIDDEN).entity("Account disabled").build();
			}
			
			if (user.getBoolean("removed")==true) {
				return Response.status(Status.NOT_FOUND).entity("Account not found (maybe removed)").build();
			}

			Key userToChangeKey = datastore.newKeyFactory().setKind("User").newKey(data.getUserToChange());
			Entity userToChange = txn.get(userToChangeKey);

			if (userToChangeKey==null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User to change role does not exist").build();
			}
			
			if (user.getString("user_role").equals("SU") && !userToChange.getString("user_role").equals("SU") && (data.getRole().equals("USER") | data.getRole().equals("GBO") | data.getRole().equals("GA"))) {
				//Can update role for anyone except SU
				UpdateUserContent(txn, userToChangeKey, userToChange, data.getRole());
				return Response.status(Status.ACCEPTED).entity("Success! User "+userToChange.getString("user_name")+"'s role changed to "+data.getRole()+".").build();
			}
			if (user.getString("user_role").equals("GA")) {
				//can update GBO and USER
				String role = (userToChange.getString("user_role"));
				if ((role.equals("USER") | role.equals("GBO")) && (data.getRole().equals("GBO") | data.getRole().equals("USER"))) {
					UpdateUserContent(txn, userToChangeKey, userToChange, data.getRole());
					return Response.status(Status.ACCEPTED).entity("Success! User "+userToChange.getString("user_name")+"'s role changed to "+data.getRole()+".").build();
				}
				else {
					txn.rollback();
					return Response.status(Status.FORBIDDEN).entity("Insufficient permissions.").build();
				}		
			}

			if (user.getString("user_role").equals("GBO")|user.getString("user_role").equals("USER")) {
				//cannot update
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Insufficient permissions.").build();
			}
			
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something unexpected happened, we hope to fix it soon.").build();
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
	
	
	private void UpdateUserContent(Transaction transaction, Key userKey, Entity user, String role) {
		Entity updateUser = Entity.newBuilder(userKey)
				.set("user_name", user.getString("user_name"))
				.set("user_pwd", user.getString("user_pwd"))
				.set("user_email", user.getString("user_email"))
				.set("user_role", role)
				.set("user_state", user.getString("user_state"))
				.set("user_profile", user.getString("user_profile"))
				.set("user_landphone", user.getString("user_landphone"))
				.set("user_cellphone", user.getString("user_cellphone"))
				.set("user_address", user.getString("user_address"))
				.set("user_comp_address", user.getString("user_comp_address"))
				.set("area_code", user.getString("area_code"))
				.set("removed", user.getBoolean("removed"))
				.build();
		
		transaction.put(updateUser);
		transaction.commit();
	}
	
	
}


