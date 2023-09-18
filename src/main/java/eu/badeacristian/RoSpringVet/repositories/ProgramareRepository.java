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
import eu.badeacristian.RoSpringVet.models.Programare;
import eu.badeacristian.RoSpringVet.models.Stapan;

@Repository
public interface ProgramareRepository extends JpaRepository<Programare, Long> {

	//toate prog. confirmate
	@Query(value = "SELECT * FROM programare WHERE stare='confirmata'", nativeQuery = true)
	Page<Programare> findAll(Pageable pageable);
	//toate prog. neconfirmate
	@Query(value = "SELECT * FROM programare WHERE stare='neconfirmata'", nativeQuery = true)
	Page<Programare> findAllNeconfirmate(Pageable pageable);

	//confirmate toti stapanii, cautare dupa data 	--- pentru dashboard
	@Query(value = "SELECT * FROM programare WHERE date LIKE %?1% AND stare='confirmata'", nativeQuery = true)
	Page<Programare> findAllByDate(@Param ("date") String date, Pageable pageable);
	//neconfirmate toti stapanii, cautare data 		--- pentru dashboard
	@Query(value = "SELECT * FROM programare WHERE date LIKE %?1% AND stare='neconfirmata'", nativeQuery = true)
	Page<Programare> findAllByDateNeconfirmate(@Param ("date") String date, Pageable pageable);
	
	//stapan neconfirmate
	@Query(value = "SELECT * FROM programare WHERE stapan_id = ?1 AND stare='neconfirmata'", nativeQuery = true)
	Page<Programare> findAllByStapanIdNeconfirmata(@Param("stapanId") Optional<Stapan> stapan, Pageable pageable);	
	//stapan confirmate
	@Query(value = "SELECT * FROM programare WHERE stapan_id = ?1 AND stare='confirmata'", nativeQuery = true)
	Page<Programare> findAllByStapanId(@Param("stapanId") Optional<Stapan> stapan, Pageable pageable);
	
	//animal confirmate
	@Query(value = "SELECT * FROM programare WHERE animal_id = ?1 AND stare='confirmata'", nativeQuery = true)
	Page<Programare> findAllByAnimalId(@Param("animalId") Optional<Animal> animal, Pageable pageable);
	//animal neconfirmate
	@Query(value = "SELECT * FROM programare WHERE animal_id = ?1 AND stare='neconfirmata'", nativeQuery = true)
	Page<Programare> findAllByAnimalIdNeconfirmate(@Param("animalId") Optional<Animal> animal, Pageable pageable);

	//toate confirmate din data de -fara pageable
	@Query(value = "SELECT * FROM programare WHERE date LIKE %?1% AND stare='confirmata'", nativeQuery = true)
	List<Programare> findAllByDate(@Param ("date") String date);
	//toate neconfirmate din data de -fara pageable
	@Query(value = "SELECT * FROM programare WHERE date LIKE %?1% AND stare='confirmata'", nativeQuery = true)
	List<Programare> findAllByDateNeconfirmate(@Param ("date") String date);

	//cautare dupa nume animal - confirmate
	@Query(value = "SELECT * FROM programare WHERE numeanimal LIKE %?1% AND stare='confirmata'", nativeQuery = true)
	Page<Programare> findAllByNumeanimalContaining(String text, Pageable pageable);
	//cautare dupa nume animal - neconfirmate
	@Query(value = "SELECT * FROM programare WHERE numeanimal LIKE %?1% AND stare='neconfirmata'", nativeQuery = true)
	Page<Programare> findAllByNumeanimalNeconfirmateContaining(String text, Pageable pageable);

	@Query(value = "SELECT * FROM programare WHERE numestapan LIKE %?1% AND stare='confirmata'", nativeQuery = true)
	Page<Programare> findByNumestapanContaining(String text, Pageable pageable);
	//cautare dupa nume stapan - confirmate
	@Query(value = "SELECT * FROM programare WHERE numestapan LIKE %?1% AND stare='neconfirmata'", nativeQuery = true)
	Page<Programare> findByNumestapanNeconfirmateContaining(String text, Pageable pageable);
	
	//toate confirmate pt Stapan din data de -fara pageable
	@Query(value = "SELECT * FROM programare WHERE date LIKE %?1% AND stare='confirmata' AND stapan_id = ?2", nativeQuery = true)
	List<Programare> findAllByDateStapan(String data, @Param("stapanId") Optional<Stapan> stapan);
	
	
	//SELECT DISTINCT date FROM appvet1.programare 
	//WHERE Month(date) = Month('2021-08-01')
	//AND Day(date) = dayofmonth('2021-08-24')
	//ORDER BY date;

	
}
