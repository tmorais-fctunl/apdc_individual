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
import pt.unl.fct.di.apdc.individual.util.ChangePasswordData;
import pt.unl.fct.di.apdc.individual.util.LoginData;

@Path("/changepassword")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePasswordResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doChangePassword(ChangePasswordData data) {

		Key userKey = datastore.newKeyFactory().setKind("User")
				.newKey(data.getUsername());

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = datastore.get(userKey);
			if (user == null) {
				//Username does not exist
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("User doesnt exist.").build();
			}

			if (user.getString("user_state").equals("DISABLED")) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Account disabled").build();
			}

			if (user.getBoolean("removed")==true) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("Account not found (maybe removed)").build();
			}

			if (!data.validPassword()) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("New password and confirmation dont match.").build();
			}

			String hashedPWD = user.getString("user_pwd");
			if (!hashedPWD.equals(DigestUtils.sha512Hex(data.getPassword()))) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Incorrect password.").build();
			}

			user = Entity.newBuilder(userKey)
					.set("user_name", user.getString("user_name"))
					.set("user_pwd", DigestUtils.sha512Hex(data.getNewPassword()))
					.set("user_email", user.getString("user_email"))
					.set("user_role", user.getString("user_role"))
					.set("user_state", user.getString("user_state"))
					.set("user_profile", user.getString("user_profile"))
					.set("user_landphone", user.getString("user_landphone"))
					.set("user_cellphone", user.getString("user_cellphone"))
					.set("user_address", user.getString("user_address"))
					.set("user_comp_address", user.getString("user_comp_address"))
					.set("area_code", user.getString("area_code"))
					.set("removed", user.getBoolean("removed"))
					.build();

			txn.put(user);
			txn.commit();
			return Response.status(Status.ACCEPTED).entity("Password changed successfuly.").build();

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
