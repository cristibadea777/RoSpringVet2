package eu.badeacristian.RoSpringVet.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.Tratament;

@Repository
public interface TratamentRepository extends JpaRepository<Tratament, Long> {
	
	//veziTratamenteActiveAnimal
	//veziTratamenteVechiAnimal
	//@@@@@@@@@ANIMALE@@@@@@@@@
	//@@@@@@@@@ANIMALE@@@@@@@@@
	//@@@@@@@@@ANIMALE@@@@@@@@@
	//tratamente active
	//Cu aceasta metoda vom gasi toate tratamentele active ale unui anumit animal, dand ca parametru ID-ul animalului.
	//Tratamente active adica data lor de sfarsit este mai mare decat data curenta
	@Query(value = "SELECT * FROM tratament WHERE tratament.animal_id = ?1 AND datasfarsit > curdate()", nativeQuery = true)
	Page<Tratament> findAllByAnimalId(@Param("animalId") Optional<Animal> animal, Pageable pageable);
	
	//tratamente vechi
	@Query(value = "SELECT * FROM tratament WHERE tratament.animal_id = ?1 AND datasfarsit < curdate()", nativeQuery = true)
	Page<Tratament> findAllVechiAnimal(Optional<Animal> animal, Pageable pageable);
	
	//veziTratamenteActiveStapan
	//veziTratamenteVechiStapan
	//@@@@@@@@@STAPANI@@@@@@@@@
	//@@@@@@@@@STAPANI@@@@@@@@@
	//@@@@@@@@@STAPANI@@@@@@@@@
	//Cu aceasta metoda vom gasi toate tratamentele active ale animalelor unui stapan, dand ca parametru ID-ul stapanului.
	//Tratamente active adica data lor de sfarsit este mai mare decat data curenta
	//Tratament nu contine cheie straina id stapan, deci fac un INNER JOIN
	//SELECT * 
	//FROM  appvet1.TRATAMENT
	//	INNER JOIN appvet1.ANIMAL 
	//		ON TRATAMENT.animal_id = ANIMAL.animal_id
	//WHERE ANIMAL.stapan_id = ?1 						(primul parametru)
	//	AND datasfarsit > curdate() 					(si pt a fi tratamente active, data curenta sa fie mai mica decat data sfarsit)
	//am avut probleme cu generarea automata a SQL de catre Spring Data JPA, interpreta "INNER" ca fiind tabel, deci am dat un ALIAS tabelelor si acum functioneaza 
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate()", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate()",
			nativeQuery = true)
	Page<Tratament> findAll(@Param("stapanId") Optional<Stapan> stapan, Pageable pageable);

	
	//tratamente vechi
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit < curdate()", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit < curdate()",
			nativeQuery = true)
	Page<Tratament> findAllVechi(@Param("stapanId") Optional<Stapan> stapan, Pageable pageable);
	
	//functia search
	//nume
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND a.nume LIKE %?2%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND a.nume LIKE %?2%",
			nativeQuery = true)
	Page<Tratament> findAllByAnimalNume(@Param("stapanId") Optional<Stapan> stapan, @Param("txtSearch") String txtSearch, Pageable pageable);
	//data inceput
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND t.datainceput LIKE %?2%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND t.datainceput LIKE %?2%",
			nativeQuery = true)
	Page<Tratament> findAllByDatainceput(@Param("stapanId") Optional<Stapan> stapan, @Param("txtSearch") String txtSearch, Pageable pageable);
	//data sfarsit
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND t.datasfarsit LIKE %?2%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND t.datasfarsit LIKE %?2%",
			nativeQuery = true)
	Page<Tratament> findAllByDataSfarsit(@Param("stapanId") Optional<Stapan> stapan, @Param("txtSearch") String txtSearch, Pageable pageable);
	//metoda tratament
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND t.metodatratament LIKE %?2%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE a.stapan_id = ?1 AND t.datasfarsit > curdate() AND t.metodatratament LIKE %?2%",
			nativeQuery = true)
	Page<Tratament> findAllByMetodatratament(@Param("stapanId") Optional<Stapan> stapan, @Param("txtSearch") String txtSearch, Pageable pageable);


	//tratamente.html
	//@@@@@@@@@TRATAMENTE@@@@@@@@@
	//@@@@@@@@@TRATAMENTE@@@@@@@@@
	//@@@@@@@@@TRATAMENTE@@@@@@@@@
	
	//toate tratamentele ACTIVE
	@Query(	value = "SELECT * FROM tratament WHERE datasfarsit > curdate()",
			nativeQuery = true)
	Page<Tratament> findAll(Pageable pageable);
	
	//search
	
	//nume animal
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE t.datasfarsit > curdate() AND a.nume LIKE %?1%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE t.datasfarsit > curdate() AND a.nume LIKE %?1%",
			nativeQuery = true)
	Page<Tratament> findAllByToateTratamenteActiveNumeAnimal(@Param("txtSearch") String txtSearch, Pageable pageable);
	//nume stapan
	@Query(	value = 	 "SELECT t.*, a.*, s.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id INNER JOIN stapan AS s ON a.stapan_id = s.stapan_id WHERE t.datasfarsit > curdate() AND s.firstname LIKE %?1% OR s.lastname LIKE %?1%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id INNER JOIN stapan AS s ON a.stapan_id = s.stapan_id WHERE t.datasfarsit > curdate() AND s.firstname LIKE %?1% OR s.lastname LIKE %?1%",
			nativeQuery = true)
	Page<Tratament> findAllByToateTratamenteActiveNumeStapan(String txtSearch, Pageable pageable);
	//data inceput
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE  t.datasfarsit > curdate() AND t.datainceput LIKE %?1%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE  t.datasfarsit > curdate() AND t.datainceput LIKE %?1%",
			nativeQuery = true)
	Page<Tratament> findAllByToateTratamenteActiveDataInceput(@Param("txtSearch") String txtSearch, Pageable pageable);
	//data sfarsit
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE t.datasfarsit > curdate() AND t.datasfarsit LIKE %?1%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE t.datasfarsit > curdate() AND t.datasfarsit LIKE %?1%",
			nativeQuery = true)
	Page<Tratament> findAllByToateTratamenteActiveDataSfarsit(@Param("txtSearch")String txtSearch, Pageable pageable);
	//metoda tratament
	@Query(	value = 	 "SELECT t.*, a.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE t.datasfarsit > curdate() AND t.metodatratament LIKE %?1%", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id WHERE t.datasfarsit > curdate() AND t.metodatratament LIKE %?1%",
			nativeQuery = true)
	Page<Tratament> findAllByToateTratamenteActiveMetodaTratament(@Param("txtSearch") String txtSearch, Pageable pageable);

	//dashboard-stapani.html
	//@@@@@@@@@DASHBOARD STAPANI@@@@@@@@@
	//@@@@@@@@@DASHBOARD STAPANI@@@@@@@@@
	//@@@@@@@@@DASHBOARD STAPANI@@@@@@@@@
	
	@Query(	value = 	 "SELECT t.*, a.*, s.* FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id INNER JOIN stapan AS s ON a.stapan_id = s.stapan_id WHERE t.datasfarsit > curdate() AND s.stapan_id = ?1", 
			countQuery = "SELECT count(*) FROM tratament as t INNER JOIN animal as a ON t.animal_id = a.animal_id INNER JOIN stapan AS s ON a.stapan_id = s.stapan_id WHERE t.datasfarsit > curdate() AND s.stapan_id = ?1",
			nativeQuery = true)
	List<Tratament> findAllTratamenteStapan(@Param("stapanId") Optional<Stapan> stapan);


	

}
