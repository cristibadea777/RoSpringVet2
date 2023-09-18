package eu.badeacristian.RoSpringVet.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import eu.badeacristian.RoSpringVet.models.Angajat;
import eu.badeacristian.RoSpringVet.repositories.AngajatRepository;

@Service
public class AngajatService {
	
	@Autowired
	private AngajatRepository angajatRepository;
	
	//selecteaza cu Email
	public Angajat getAngajatByEmail(String email) {
		return angajatRepository.findByEmail(email);
	}
	
	//selecteaza toti angajatii
	public List<Angajat> getAllAngajati(){
		List<Angajat> angajati = new ArrayList<>();
		angajatRepository.findAll()
		.forEach(angajati::add);
		return angajati;
	}
	//selecteaza un angajat in functie de id
	public Optional<Angajat> getAngajat(long angajatId){
		return angajatRepository.findById(angajatId);
	}
	//adauga Angajat
	public void addAngajat(Angajat angajat) {
		angajatRepository.save(angajat);
	}
	//update Angajat
	public void updateAngajat(Angajat angajat) {
		angajatRepository.save(angajat);
	}
	//sterge Angajat
	public void deleteAngajat(long angajatId) {
		angajatRepository.deleteById(angajatId);
	}
	
	public Page<Angajat> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.angajatRepository.findAll(pageable);
	}

	//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
	//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
	public Page<Angajat> findPaginatedSearch(int pageNo, int pageSize, String sortField, String sortDirection, String field, String text) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(field.equals("email"))
			return this.angajatRepository.findByEmailContaining(text, pageable);
		if(field.equals("lastname"))
			return this.angajatRepository.findByLastnameContaining(text, pageable);
		if(field.equals("firstname"))
			return this.angajatRepository.findByFirstnameContaining(text, pageable);
		if(field.equals("nrtelefon"))
			return this.angajatRepository.findByNrtelefonContaining(text, pageable);
		//daca niciun camp nu corespunde, atunci utilizatorul a umblat in URL si a scris ce a vrut pt camp
		return null;
	}

	
	public Page<Angajat> findPaginatedPlecati(int pageNo, int pageSize, String sortField, String sortDirection) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.angajatRepository.findAllPlecati(pageable);
	}

	
	///”””””””””””””astea trebuie lucrate in repozitoriu, ca sa ii arate doar pe aia plecati”””””””””””””””
	
	public Page<Angajat> findPaginatedSearchPlecati(int pageNo, int pageSize, String sortField, String sortDirection, String field, String text) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(field.equals("email"))
			return this.angajatRepository.findByEmailContaining(text, pageable);
		if(field.equals("lastname"))
			return this.angajatRepository.findByLastnameContaining(text, pageable);
		if(field.equals("firstname"))
			return this.angajatRepository.findByFirstnameContaining(text, pageable);
		if(field.equals("nrtelefon"))
			return this.angajatRepository.findByNrtelefonContaining(text, pageable);
		//daca niciun camp nu corespunde, atunci utilizatorul a umblat in URL si a scris ce a vrut pt camp
		return null;
	}
	
	
	
}
