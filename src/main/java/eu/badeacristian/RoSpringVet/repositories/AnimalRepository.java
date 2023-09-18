package eu.badeacristian.RoSpringVet.repositories;



import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Stapan;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {

	Page<Animal> findAllByStapanId(@Param("stapanId") Optional<Stapan> stapan, Pageable pageable);

	
	
	//Search//
	Page<Animal> findByStapanIdAndNumeContaining	(@Param("stapanId") Optional<Stapan> stapan, String text, Pageable pageable);
	Page<Animal> findByStapanIdAndSpecieContaining	(@Param("stapanId") Optional<Stapan> stapan, String text, Pageable pageable);
	Page<Animal> findByStapanIdAndRasaContaining	(@Param("stapanId") Optional<Stapan> stapan, String text, Pageable pageable);

	Page<Animal> findByNumeContaining(String txtSearch, Pageable pageable);
	Page<Animal> findBySpecieContaining(String txtSearch, Pageable pageable);
	Page<Animal> findByRasaContaining(String txtSearch, Pageable pageable);
	

}
