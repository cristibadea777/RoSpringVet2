package eu.badeacristian.RoSpringVet.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import eu.badeacristian.RoSpringVet.controllers.AnimalController;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.repositories.AnimalRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AnimalService {
	
	@Autowired
	private AnimalRepository animalRepository;
	
	@Autowired
	private StapanService stapanService;
	
	//selecteaza toate animalele
	public List<Animal> getAllAnimale(){
		List<Animal> animale = new ArrayList<Animal>();
		animalRepository.findAll()
		.forEach(animale::add);
		
		for(Animal animal : animale) {
			log.warn(animal.getNume());
		}
		
        Collections.sort(animale, new Comparator<Animal>() {
            @Override
            public int compare(Animal animal1, Animal animal2) {
                return animal1.getNume().compareTo(animal2.getNume()); //28 Martie 
            }
        });
        

		for(Animal animal : animale) {
			log.warn(animal.getNume());
		}
		
        
		return animale;
		
	}
	
	
	
	//selecteaza un animal in functie de id
	public Optional<Animal> getAnimal(long animalId){
		return animalRepository.findById(animalId);
	}
	//adauga animal
	public void addAnimal(Animal animal) {
		animalRepository.save(animal);
	}
	//update animal
	public void updateAnimal(Animal animal) {
		animalRepository.save(animal);
	}
	//sterge animal
	public void deleteAnimal(long animalId) {
		animalRepository.deleteById(animalId);
	}
	
	public Page<Animal> findPaginatedAnimal(int pageNo, int pageSize, String sortField, String sortDirection) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return this.animalRepository.findAll(pageable);
	}
	
	public Page<Animal> findPaginatedAnimalStapan(long stapanId, int pageNo, int pageSize, String sortField, String sortDirection) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		
		//tipu de date a stapanId a Animal este Stapan
		Optional<Stapan> stapan = stapanService.getStapan(stapanId);
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		return animalRepository.findAllByStapanId(stapan, pageable);
	}

	
	public Page<Animal> findPaginatedSearchAnimalStapan(int pageNo, int pageSize, String sortField, String sortDirection, String field, String text, long stapanId) {
		Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		//tipu de date a stapanId a Animal este Stapan
		Optional<Stapan> stapan = stapanService.getStapan(stapanId);
		
		if(field.equals("nume"))
			return this.animalRepository.findByStapanIdAndNumeContaining(stapan, text, pageable);
		if(field.equals("specie"))
			return this.animalRepository.findByStapanIdAndSpecieContaining(stapan, text, pageable);
		if(field.equals("rasa"))
			return this.animalRepository.findByStapanIdAndRasaContaining(stapan, text, pageable);
		//daca niciun camp nu corespunde, atunci utilizatorul a umblat in URL si a scris ce a vrut pt camp
		return null;
	}
	
	
	//search pt animale.html
	public Page<Animal> findPaginatedSearchAnimale(int pageNo, int pageSize, String sortField, String sortDir,
			String fieldSearch, String txtSearch) {
		
		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
			Sort.by(sortField).descending();
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		
		if(fieldSearch.equals("nume"))
			return this.animalRepository.findByNumeContaining(txtSearch, pageable);
		if(fieldSearch.equals("specie"))
			return this.animalRepository.findBySpecieContaining(txtSearch, pageable);
		if(fieldSearch.equals("rasa"))
			return this.animalRepository.findByRasaContaining(txtSearch, pageable);
		//daca niciun camp nu corespunde, atunci utilizatorul a umblat in URL si a scris ce a vrut pt camp
		return null;
	}
	
	
	
}
