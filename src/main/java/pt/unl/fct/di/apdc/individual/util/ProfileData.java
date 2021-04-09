package pt.unl.fct.di.apdc.individual.util;

import java.util.regex.Matcher;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import pt.unl.fct.di.apdc.individual.resources.RegisterResource;

public class ProfileData {
	
	private static final String PRIVADO = "PRIVADO";
	private static final String PUBLICO = "PUBLICO";
	private static final String regexTelFixo = "([+]351\\s)?[2][0-9]{8}";
	private static final String regexTelMovel = "([+]351\\s)?[9][0-9]{8}";
	//Localidade : CP
	private static final String regexLocalidade = "/[A-Z]*[a-z]* [A-Z]*[a-z]* : \\d{4}\\-\\d{3]/";
	String perfil;
	String telFixo;
	String telMovel;
	String morada;
	String moradaComplementar;
	String localidade;
	private static final Logger LOG = Logger.getLogger(ProfileData.class.getName());
	
	
	public ProfileData() {}
	
	public ProfileData(String perfil, String telFixo, 
			String telMovel, String morada, String moradaComplementar, String localidade) {
		
		if (perfil!=null)
			this.perfil = perfil.toUpperCase();
		this.telFixo = telFixo;
		this.telMovel = telMovel;
		this.morada = morada;
		this.moradaComplementar = moradaComplementar;
		this.localidade = localidade;
	
	}
	
	public boolean validateProfileData () {
		LOG.severe("Entrei no validate");
		if (getPerfil()!=null && !(getPerfil().equals(PUBLICO) | getPerfil().equals(PRIVADO)))
				return false;
		LOG.severe("Passei primeiro if");
		if (getTelMovel()!=null) {
			LOG.severe("Passei segundo if");
			Pattern telMovelPattern = Pattern.compile(regexTelMovel);
			Matcher telMovelMatcher = telMovelPattern.matcher(getTelMovel());
			if (!telMovelMatcher.matches()) {
				return false;	
			}
			LOG.severe("Passei terceiro if");
		}
		if (getTelFixo()!=null) {
			LOG.severe("Passei quarto if");
			Pattern telFixoPattern = Pattern.compile(regexTelFixo);
			Matcher telFixoMatcher = telFixoPattern.matcher(getTelFixo());
			if (!telFixoMatcher.matches())
				return false;
			LOG.severe("Passei quinto if");
		}
		if (getLocalidade() != null) {
			LOG.severe("Passei sexto if");
			Pattern localidadePattern = Pattern.compile(regexLocalidade);
			Matcher localidadeMatcher = localidadePattern.matcher(getTelFixo());
			if (!localidadeMatcher.matches())
				return false;
			LOG.severe("Passei setimo if");
		}
		
		return true;
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
	
	

}
