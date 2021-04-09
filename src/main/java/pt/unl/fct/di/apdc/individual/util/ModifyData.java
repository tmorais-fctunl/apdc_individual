package pt.unl.fct.di.apdc.individual.util;



public class ModifyData {

	private String perfil;
	private String telFixo;
	private String telMovel;
	private String morada;
	private String moradaComplementar;
	private String localidade;
	private String username;
	private String token;
	private ProfileData profile;
	
	public ModifyData() {}
	
	public ModifyData(String perfil, String telFixo, 
			String telMovel, String morada, String moradaComplementar, String localidade,
			String username, String token) {
		
		this.username = username;
		this.token = token;
		this.perfil = perfil.toUpperCase();
		this.telFixo = telFixo;
		this.telMovel = telMovel;
		this.morada = morada;
		this.moradaComplementar = moradaComplementar;
		this.localidade = localidade;
	}
	
	
	
	public ProfileData profile() {
		return new ProfileData(perfil, telFixo, telMovel, morada, moradaComplementar, localidade);
	}

	public String getPerfil() {
		return perfil;
	}

	public String getTelFixo() {
		return telFixo;
	}

	public String getTelMovel() {
		return telMovel;
	}

	public String getMorada() {
		return morada;
	}

	public String getMoradaComplementar() {
		return moradaComplementar;
	}

	public String getLocalidade() {
		return localidade;
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}
	
	
}
