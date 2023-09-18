package eu.badeacristian.RoSpringVet.models;


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class Angajat extends Persoana{
	
	//mosteneste Persoana

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long angajatId;
	
	private String functie;
	
	private String descriere;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "angajatId")
	@JsonIgnore
	private List<Vizita> vizite;
	
	private String imagine;

	public Angajat(String firstname, String lastname, String nrtelefon, String email, String parola, String functie, String descriere,
			List<Vizita> vizite, String imagine) {
		super(firstname, lastname, nrtelefon, email, parola);
		this.functie = functie;
		this.vizite = vizite;
		this.imagine = imagine;
		this.descriere = descriere;
	}

	


	

	
	
}
