package eu.badeacristian.RoSpringVet.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.badeacristian.RoSpringVet.models.Angajat;

@Repository
public interface AngajatRepository extends JpaRepository<Angajat, Long>{

	Angajat findByEmail(String email);

	@Query(value = "SELECT * FROM angajat WHERE angajat.functie != 'PLECAT' ", nativeQuery = true)
	Page<Angajat> findAll(Pageable pageable);
	
	
	//functii cautare
	Page<Angajat> findByEmailContaining(String text, Pageable pageable);
	Page<Angajat> findByFirstnameContaining(String text, Pageable pageable);
	Page<Angajat> findByLastnameContaining(String text, Pageable pageable);
	Page<Angajat> findByNrtelefonContaining(String text, Pageable pageable);
	
	
	//~~~~~~~~~~~~~~~~PLECATI~~~~~~~~~~~~~~~~~~~~~~~~
	@Query(value = "SELECT * FROM angajat WHERE angajat.functie = 'PLECAT' ", nativeQuery = true)
	Page<Angajat> findAllPlecati(Pageable pageable);
	
}
