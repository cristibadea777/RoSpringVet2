package eu.badeacristian.RoSpringVet.services;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.Tratament;
import eu.badeacristian.RoSpringVet.repositories.TratamentRepository;

@Service
public class TratamentService {
	
	@Autowired
	private TratamentRepository tratamentRepository;
	
	@Autowired
	private AnimalService animalService;
	
	
	@Autowired
	private StapanService stapanService;

	//selecteaza un tratament in functie de id
	public Optional<Tratament> getTratament(long tratamentId){
		return tratamentRepository.findById(tratamentId);
	}
	//adauga tratament
	public void addTratament(Tratament tratament) {
		tratamentRepository.save(tratament);
	}
	//update tratament
	public void updateTratament(Tratament tratament) {
		tratamentRepository.save(tratament);
	}
	//sterge tratament
	public void deleteTratament(Tratament tratament) {
		tratamentRepository.delete(tratament);
	}
	
	public Page<Tratament> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.tratamentRepository.findAll(pageable);
	}
	
	
	public Page<Tratament> findPaginatedTratamenteActiveStapan(long id, int pageNo, int pageSize, String sortField,
			String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
			
		Optional<Stapan> stapan = stapanService.getStapan(id);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		return this.tratamentRepository.findAll(stapan, pageable);
		
	}
	
	//pentru dashboard stapani
	public int getTotalTratamenteStapan(long id) {
		Optional<Stapan> stapan = stapanService.getStapan(id);
		return this.tratamentRepository.findAllTratamenteStapan(stapan).size();
	}
	
	
	
	public Page<Tratament> findPaginatedSearchTratamenteActiveStapan(long id, int pageNo, int pageSize,
			String sortField, String sortDir, String fieldSearch, String txtSearch) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Optional<Stapan> stapan = stapanService.getStapan(id);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(fieldSearch.equals("numeanimal"))
			return  this.tratamentRepository.findAllByAnimalNume(stapan, txtSearch, pageable);
		if(fieldSearch.equals("datainceput"))
			return this.tratamentRepository.findAllByDatainceput(stapan, txtSearch, pageable);
		if(fieldSearch.equals("datasfarsit"))
			return this.tratamentRepository.findAllByDataSfarsit(stapan, txtSearch, pageable);
		if(fieldSearch.equals("metodatratament"))
			return this.tratamentRepository.findAllByMetodatratament(stapan, txtSearch, pageable);
		
		return this.tratamentRepository.findAll(stapan, pageable);
	}
	
	
	
	
	public Page<Tratament> findPaginatedSearchTratamenteVechiStapan(long id, int pageNo, int pageSize,
			String sortField, String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();		
		Optional<Stapan> stapan = stapanService.getStapan(id);
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		return this.tratamentRepository.findAllVechi(stapan, pageable);
	}
	
	
	
	public Page<Tratament> findPaginatedTratamenteActiveAnimal(long id, int pageNo, int pageSize, String sortField,
			String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Optional<Animal> animal = animalService.getAnimal(id);
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		return this.tratamentRepository.findAllByAnimalId(animal, pageable);
	}
	
	
	public Page<Tratament> findPaginatedTratamenteVechiAnimal(long id, int pageNo, int pageSize, String sortField,
			String sortDir) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();		
		Optional<Animal> animal = animalService.getAnimal(id);
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		return this.tratamentRepository.findAllVechiAnimal(animal, pageable);
	}
	
	//~~~~~~~~~PT TOATE TRATAMENTELE~~~~~~~~~
	public Page<Tratament> findPaginatedSearchToateTratamenteActive(int pageNo, int pageSize, String sortField,
			String sortDir, String fieldSearch, String txtSearch) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(fieldSearch.equals("numeanimal"))
			return  this.tratamentRepository.findAllByToateTratamenteActiveNumeAnimal(txtSearch, pageable);
		
		if(fieldSearch.equals("numestapan"))
			return  this.tratamentRepository.findAllByToateTratamenteActiveNumeStapan(txtSearch, pageable);
		
		if(fieldSearch.equals("datainceput"))
			return this.tratamentRepository.findAllByToateTratamenteActiveDataInceput(txtSearch, pageable);
		
		if(fieldSearch.equals("datasfarsit"))
			return this.tratamentRepository.findAllByToateTratamenteActiveDataSfarsit(txtSearch, pageable);
		
		if(fieldSearch.equals("metodatratament"))
			return this.tratamentRepository.findAllByToateTratamenteActiveMetodaTratament(txtSearch, pageable);
		
		return this.tratamentRepository.findAll(pageable);
	}
	
	
}
