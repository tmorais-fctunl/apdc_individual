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
import pt.unl.fct.di.apdc.individual.util.LogoutData;
import pt.unl.fct.di.apdc.individual.util.ModifyData;
import pt.unl.fct.di.apdc.individual.util.ProfileData;


@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(ModifyResource.class.getName());

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response modify (ModifyData data) {
		
		ProfileData profileData = data.profile();
	
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
				LOG.severe("credenciais invalidos");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Invalid credentials").build();
			}
			
			if (tokenID.equals(token.getString("token_id")) && 
					data.getUsername().equals(token.getString("username")) && 
					token.getLong("expiration_time")<=System.currentTimeMillis()) {
				LOG.severe("sessao expirou");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Session expired").build();
			}
			LOG.severe("passei terceiro if");
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
		

			if (!profileData.validateProfileData()) {
				LOG.severe("Perfil nao valido");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Profile data invalid.").build();
			}
			

			Entity updateUser = Entity.newBuilder(userKey)
					.set("user_name", user.getString("user_name"))
					.set("user_pwd", user.getString("user_pwd"))
					.set("user_email", user.getString("user_email"))
					.set("user_role", user.getString("user_role"))
					.set("user_state", user.getString("user_state"))
					.set("user_profile", profileData.getPerfil()== null ? new String() : profileData.getPerfil())
					.set("user_landphone", profileData.getTelFixo()== null ? new String(): profileData.getTelFixo())
					.set("user_cellphone", profileData.getTelMovel() == null?  new String() : profileData.getTelMovel())
					.set("user_address", profileData.getMorada() == null? new String() : profileData.getMorada())
					.set("user_comp_address", profileData.getMoradaComplementar() == null ? new String() : profileData.getMoradaComplementar())
					.set("area_code", profileData.getLocalidade()== null ? new String() : profileData.getLocalidade())
					.set("removed", false)
					.build();

			txn.put(updateUser);
			txn.commit();
			
			return Response.status(Status.ACCEPTED).entity("Profile data changed.").build();
		}
		catch(Exception e) {
			
			txn.rollback();
			
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
