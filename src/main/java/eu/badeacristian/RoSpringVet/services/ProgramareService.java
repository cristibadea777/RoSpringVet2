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
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Programare;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.repositories.ProgramareRepository;

@Service
public class ProgramareService {

	@Autowired
	private ProgramareRepository programareRepository;
	
	@Autowired
	private StapanService stapanService;
	
	@Autowired
	private AnimalService animalService;
	
	//selecteaza toate programarile
	public List<Programare> getAllProgramari(){
		List<Programare> programari = new ArrayList<>();
		programareRepository.findAll()
		.forEach(programari::add);
		return programari;
	}
	//selecteaza o programare in functie de id
	public Optional<Programare> getProgramare(long programareId){
		return programareRepository.findById(programareId);
	}
	//adauga programare
	public void addProgramare(Programare programare) {
		programareRepository.save(programare);
	}
	//update programare
	public void updateProgramare(Programare programare) {
		programareRepository.save(programare);
	}
	//sterge programare
	public void deleteProgramare(Programare programare) {
		programareRepository.delete(programare);
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ CONFIRMATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ CONFIRMATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	
	public Page<Programare> findPaginatedProgramare(int pageNo, int pageSize, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAll(pageable);
	}
	
	
	public void deleteProgramare(long id) {
		programareRepository.deleteById(id);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~TOATE CONFIRMATE - STAPAN
	public Page<Programare> findPaginatedStapan(long id, int pageNo, int pageSize, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Optional<Stapan> stapan = stapanService.getStapan(id);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAllByStapanId(stapan, pageable);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~TOATE CONFIRMATE - ANIMAL
	public Page<Programare> findPaginatedAnimal(long id, int pageNo, int pageSize, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Optional<Animal> animal = animalService.getAnimal(id);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAllByAnimalId(animal, pageable);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~TOATE CONFIRMATE
	public Page<Programare> findPaginatedData(int pageNo, int pageSize, String sortField, String sortDir, String data) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAllByDate(data, pageable);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH CONFIRMATE
	public Page<Programare> findPaginatedSearch(int pageNo, int pageSize, String sortField, String sortDirection, String field, String text) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(field.equals("numestapan"))
			return this.programareRepository.findByNumestapanContaining(text, pageable);
		
		if(field.equals("numeanimal"))
			return this.programareRepository.findAllByNumeanimalContaining(text, pageable);
		
		if(field.equals("date"))
			return this.programareRepository.findAllByDate(text, pageable);
		//daca niciun camp nu corespunde, atunci utilizatorul a umblat in URL si a scris ce a vrut pt camp
		return null;
	}
	
	//~~~~~~~~~~~~~~~~~~~~TOATE PROGRAMARILE CONFIRMATE DIN DATA DE.... (PENTRU  DASHBOARD ANGAJAT)
	public List<Programare> getAllProgramariData(String data){
		List<Programare> listaProgramariData =  this.programareRepository.findAllByDate(data);
		return listaProgramariData;
	}
	
	//~~~~~~~~~~~~~~~~~~~~TOATE PROGRAMARILE CONFIRMATE DIN DATA DE.... (PENTRU  DASHBOARD STAPAN)
	public List<Programare> getAllProgramariDataStapan(String data, long id){
		Optional<Stapan> stapan = stapanService.getStapan(id);
		List<Programare> listaProgramariData =  this.programareRepository.findAllByDateStapan(data, stapan);
		return listaProgramariData;
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ NECONFIRMATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ NECONFIRMATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	
	
	
	//~~~~~~~~~~~~~~~~~~~~~NECONFIRMATE TOATE
	//~~~~~~~~~~~~~~~~~~~~~NECONFIRMATE TOATE
	public Page<Programare> findPaginatedProgramareNeconfirmata(int pageNo, int pageSize, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAllNeconfirmate(pageable);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~NECONFIRMATE STAPAN
	//~~~~~~~~~~~~~~~~~~~~~NECONFIRMATE STAPAN
	public Page<Programare> findPaginatedStapanNeconfirmata(long id, int pageNo, int pageSize, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Optional<Stapan> stapan = stapanService.getStapan(id);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAllByStapanIdNeconfirmata(stapan, pageable);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~NECONFIRMATE ANIMAL
	//~~~~~~~~~~~~~~~~~~~~~NECONFIRMATE ANIMAL
	public Page<Programare> findPaginatedAnimalNeconfirmate(long id, int pageNo, int pageSize, String sortField, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Optional<Animal> animal = animalService.getAnimal(id);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.programareRepository.findAllByAnimalIdNeconfirmate(animal, pageable);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH NECONFIRMATE
	//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH NECONFIRMATE
	public Page<Programare> findPaginatedSearchNeconfirmate(int pageNo, int pageSize, String sortField, String sortDirection, String field, String text) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(field.equals("numestapan"))
			return this.programareRepository.findByNumestapanNeconfirmateContaining(text, pageable);
		
		if(field.equals("numeanimal"))
			return this.programareRepository.findAllByNumeanimalNeconfirmateContaining(text, pageable);
		
		if(field.equals("date"))
			return this.programareRepository.findAllByDateNeconfirmate(text, pageable);
		//daca niciun camp nu corespunde, atunci utilizatorul a umblat in URL si a scris ce a vrut pt camp
		return null;
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~TOATE PROGRAMARILE NECONFIRMATE DIN DATA DE.... (PENTRU  DASHBOARD ANGAJAT)
	//~~~~~~~~~~~~~~~~~~~~TOATE PROGRAMARILE NECONFIRMATE DIN DATA DE.... (PENTRU  DASHBOARD ANGAJAT)
	public List<Programare> getAllProgramariDataNeconfirmate(String data){
		List<Programare> listaProgramariData =  this.programareRepository.findAllByDateNeconfirmate(data);
		return listaProgramariData;
	}
	
	

	
}
