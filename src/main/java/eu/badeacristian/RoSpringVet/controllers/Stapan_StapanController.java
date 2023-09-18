
 package eu.badeacristian.RoSpringVet.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import eu.badeacristian.RoSpringVet.models.Angajat;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Diagnostic;
import eu.badeacristian.RoSpringVet.models.Programare;
import eu.badeacristian.RoSpringVet.models.Role;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.Tratament;
import eu.badeacristian.RoSpringVet.models.Vizita;
import eu.badeacristian.RoSpringVet.models.VizitaWrapper;
import eu.badeacristian.RoSpringVet.services.AngajatService;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import eu.badeacristian.RoSpringVet.services.ProgramareService;
import eu.badeacristian.RoSpringVet.services.StapanService;
import eu.badeacristian.RoSpringVet.services.TratamentService;
import eu.badeacristian.RoSpringVet.services.UserService;
import eu.badeacristian.RoSpringVet.services.VizitaService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class Stapan_StapanController {
	
	@Autowired
	StapanService stapanService;
	
	@Autowired
	private AngajatService angajatService;
	
	@Autowired
	private TratamentService tratamentService;
	
	@Autowired
	private ProgramareService programareService;

	@Autowired
	private AnimalService animalService;
	
	@Autowired
	private VizitaService vizitaService;
	
	@Autowired
	private UserService userService;
	
	
	private Stapan getStapan() {
		
		String username = "wut";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
		  username = ((UserDetails)principal).getUsername();
		} else {
		  username = principal.toString();
		}		
		
		log.warn("LOGGED USER - USERNAME:" 	+ username);
		log.warn("LOGGED USER - ROLES");
		Collection<Role> roluri = userService.getUser(username).getRoles();
		for (Role rol : roluri) 
			log.warn("~~~~~~~~~~~~~~~~~~~ " + rol.getName());
		
		Stapan stapan = stapanService.getStapanByEmail(username);
		
		return stapan;
	}
	
	
////------------------------------------- ANIMALE-------------------------------------
////------------------------------------- ANIMALE-------------------------------------
	@GetMapping("/veziAnimaleleTale")
	private String actiuniAnimalStapani(Model model) {
		return findPaginatedAnimaleStapan(1, "nume", "asc", model); // returneaza prima pagina
	}

	@GetMapping("/veziAnimaleleTale/page/{pageNo}")
	public String findPaginatedAnimaleStapan(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			Model model) {

		Stapan stapan = getStapan();
		
		// adaugam stapanul in model
		model.addAttribute("stapan", stapan);
		
		long id_stapan = stapan.getStapanId();
		int pageSize = 5;
		Page<Animal> page = animalService.findPaginatedAnimalStapan(id_stapan, pageNo, pageSize, sortField, sortDir);
		List<Animal> listaAnimale = page.getContent();
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaAnimale", listaAnimale);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		//Atribute pentru adaugarea unei programari
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		
		//Atribute pentru adaugarea unui animal nou
		Animal animal = new Animal();
		model.addAttribute("animal", animal);
		
		return "templates-stapan/eu-animale.html";

	}
	
	
////-------------------------------------VIZITE-------------------------------------
////-------------------------------------VIZITE-------------------------------------	
	@RequestMapping("/euVizite")
	public String getViziteStapan(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedViziteStapan(1, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euVizite/page/{pageNo}")
	public String findPaginatedViziteStapan(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		//luam stapanul conectat cu metoda ce utilizeaza spring security, si dupa ii luam id-ul pt query-uri in bd
		Stapan stapan = getStapan();
		model.addAttribute("stapan", stapan);
		long id = stapan.getStapanId();
		
		int pageSize = 5;
		Page<Vizita> page = vizitaService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
		List<Vizita> listaVizite = page.getContent();		
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaVizite", listaVizite);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		return "templates-stapan/eu-vizite";

	}	
	
	
////-------------------------------------VIZITE ANIMAL-------------------------------------
////-------------------------------------VIZITE ANIMAL-------------------------------------
	@RequestMapping("/euViziteAnimal/{id}")
	public String getViziteAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedViziteAnimal(1, id, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euViziteAnimal/{id}/page/{pageNo}")
	public String findPaginatedViziteAnimal(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request, 
			RedirectAttributes redirectAttributes,
			Model model) {

		
		//animalul cu id-ul...din url
		Animal animal = animalService.getAnimal(id).get();
		
		//luam stapanul CONECTAT
		//luam stapanu utilizand spring security. dupa ii luam id-ul
		Stapan stapan = getStapan();
				
		//acum DACA id-ul stapanului animalului nu coincide cu al stapanului conectat 
		//atunci nu o sa mearga, redirectionare catre /veziAnimaleleTale
		if(animal.getStapanId().getStapanId() != stapan.getStapanId())
			return "redirect:/veziAnimaleleTale";
		
		
		int pageSize = 5;
		Page<Vizita> page = vizitaService.findPaginatedAnimal(id, pageNo, pageSize, sortField, sortDir);
		List<Vizita> listaVizite = page.getContent();
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaVizite.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu stapanului
			if(pageNo==0) {
				sirAtribute.add("fararezultatAnimal");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			page = vizitaService.findPaginatedAnimal(id, pageNo, pageSize, sortField, sortDir);
			listaVizite = page.getContent();
		}

		model.addAttribute("animal", animal);
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaVizite", listaVizite);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		// Atribute pentru adaugarea unei vizite noi cu modalul
		// Vizita
		Vizita vizita = new Vizita();
		// Diagnostic
		Diagnostic diagnostic = new Diagnostic();
		model.addAttribute("diagnostic", diagnostic);
		long diagnostic_id = diagnostic.getDiagnosticId();
		model.addAttribute("id_diagnostic", diagnostic_id);
		// Tratament
		Tratament tratament = new Tratament();
		model.addAttribute("tratament", tratament);
		long tratament_id = tratament.getTratamentId();
		model.addAttribute("id_tratament", tratament_id);
		// Angajat
		Angajat angajat = new Angajat();
		List<Angajat> lista_angajati = angajatService.getAllAngajati();
		model.addAttribute("lista_angajati", lista_angajati);
		model.addAttribute("angajat", angajat);
		VizitaWrapper vizitawrapper = new VizitaWrapper();
		// impachetam toate obiectele pe care vrem sa le salvam in obiectul
		// vizitawrapper, care e trimis de formular, acolo luam obiectele cu get() si le salvam
		vizitawrapper.setVizita(vizita);
		vizitawrapper.setDiagnostic(diagnostic);
		vizitawrapper.setTratament(tratament);
		model.addAttribute("vizitawrapper", vizitawrapper);		

		return "templates-stapan/eu-vizite-animal";

	}
	
////-------------------------------------PROGRAMARI-------------------------------------
////-------------------------------------PROGRAMARI-------------------------------------
	
	@RequestMapping("/euProgramari")
	public String getProgramariStapan(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariStapan(1, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euProgramari/page/{pageNo}")
	public String findPaginatedProgramariStapan(
			@PathVariable(value = "pageNo") int pageNo, 
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
						
		Stapan stapan = getStapan();
		long id = stapan.getStapanId();
		String nume_stapan = stapan.getLastname() + " " + stapan.getFirstname();
		model.addAttribute("stapan", stapan);
		model.addAttribute("nume_stapan", nume_stapan);
		
		int pageSize = 5;
		Page<Programare> page = programareService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
		List<Programare> listaProgramari = page.getContent();
				
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu stapanului
			if(pageNo==0) {
				return "templates-stapan/eu-programari";
			}
			page = programareService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
		
		//atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		//tabel
		model.addAttribute("listaProgramari", listaProgramari);
		
		//atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		// atribute pentru a edita o programare
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		

		return "templates-stapan/eu-programari";

	}
	
////-------------------------------------PROGRAMARI NECONFIRMATE-------------------------------------
////-------------------------------------PROGRAMARI NECONFIRMATE-------------------------------------
	
	@RequestMapping("/euProgramariNeconfirmate")
	public String getProgramariStapanNeconfirmate(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariStapanNeconfirmate(1, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euProgramariNeconfirmate/page/{pageNo}")
	public String findPaginatedProgramariStapanNeconfirmate(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		//luam stapanu utilizand spring security. dupa ii luam id-ul
		Stapan stapan = getStapan();
		long id = stapan.getStapanId();

		int pageSize = 5;
		Page<Programare> page = programareService.findPaginatedStapanNeconfirmata(id, pageNo, pageSize, sortField, sortDir);
		List<Programare> listaProgramari = page.getContent();
		
		model.addAttribute("stapan", stapan);
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			
			//daca nu exista inregistrari
			if(page == null || page.getTotalPages() <= 0 ) {
				return "templates-stapan/eu-programari-neconfirmate";
			}
			
			page = programareService.findPaginatedStapanNeconfirmata(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
	
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaProgramari", listaProgramari);
		
		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		//atribute editare si adaugare programare
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		
		
		return "templates-stapan/eu-programari-neconfirmate";

	}
	
////-------------------------------------PROGRAMARI ANIMAL-------------------------------------
////-------------------------------------PROGRAMARI ANIMAL-------------------------------------
	
	@RequestMapping("/euProgramariAnimal/{id}")
	public String getProgramariAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariAnimal(1, id, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euProgramariAnimal/{id}/page/{pageNo}")
	public String findPaginatedProgramariAnimal(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request, 
			RedirectAttributes redirectAttributes, 
			Model model) {

		int pageSize = 5;
		Page<Programare> page = programareService.findPaginatedAnimal(id, pageNo, pageSize, sortField, sortDir);
		List<Programare> listaProgramari = page.getContent();
		
		Animal animal = animalService.getAnimal(id).get();
		model.addAttribute("animal", animal);
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu animalului
			//daca nu exista inregistrari
			if(page == null || page.getTotalPages() <= 0 ) {
				sirAtribute.add("fararezultatAnimal");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			page = programareService.findPaginatedAnimal(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaProgramari", listaProgramari);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		//atribute editare si adaugare programare
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		
		
		return "templates-stapan/eu-programari-animal";

	}
	
	@RequestMapping("/euProgramariAnimalNeconfirmate/{id}")
	public String getProgramariAnimalNeconfirmate(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariAnimalNeconfirmate(1, id, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euProgramariAnimalNeconfirmate/{id}/page/{pageNo}")
	public String findPaginatedProgramariAnimalNeconfirmate(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request, 
			RedirectAttributes redirectAttributes, 
			Model model) {

		int pageSize = 5;
		Page<Programare> page = programareService.findPaginatedAnimalNeconfirmate(id, pageNo, pageSize, sortField, sortDir);
		List<Programare> listaProgramari = page.getContent();
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
	
		Animal animal = animalService.getAnimal(id).get();
		model.addAttribute("animal", animal);
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu animalului
			//daca nu exista inregistrari
			if(page == null || page.getTotalPages() <= 0 ) {
				sirAtribute.add("fararezultatAnimal");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			page = programareService.findPaginatedAnimalNeconfirmate(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}

		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaProgramari", listaProgramari);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		//atribute editare si adaugare programare
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		
		
		return "templates-stapan/eu-programari-animal-neconfirmate";

	}
		
		
////-------------------------------------TRATAMENTE-------------------------------------
////-------------------------------------TRATAMENTE-------------------------------------
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//Toate tratamente active pentru stapanul conectat
	@RequestMapping("/euTratamenteActive")
	public String getTratamenteStapan(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteActiveStapan(1, "datainceput", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euTratamenteActive/page/{pageNo}")
	public String findPaginatedTratamenteActiveStapan(
			@PathVariable(value = "pageNo") int pageNo, 
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		
		Stapan stapan = getStapan();
		model.addAttribute("stapan", stapan);
		long id = stapan.getStapanId();
		model.addAttribute("stapanId", id);
		
		
		
		Page<Tratament> page = null;
		List<Tratament> listaTratamente = null;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();

		if(txtSearch.isEmpty()) {
			//gaseste paginat toate tratamentele active stapan
			page = tratamentService.findPaginatedTratamenteActiveStapan(id, pageNo, pageSize, sortField, sortDir);
			
			//daca nu exista inregistrari
			if(page == null || page.getTotalPages() <= 0 ) {
				sirAtribute.add("fararezultatStapan");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			
			listaTratamente = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaTratamente.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = tratamentService.findPaginated(pageNo, pageSize, sortField, sortDir);
				listaTratamente = page.getContent();
			}
			
			
			listaTratamente = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaTratamente.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = tratamentService.findPaginatedTratamenteActiveStapan(id, pageNo, pageSize, sortField, sortDir);
				listaTratamente = page.getContent();
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		else {
			// gaseste paginat vizite al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = tratamentService.findPaginatedSearchTratamenteActiveStapan(id, pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
			//daca pagina este goala, adica nu am gasit nici un rezultat pentru cautarea noastra
			//atunci mergem inapoi pe pagina stapanilor si informam utilizatorul deschizand un modal
			if(page == null || page.isEmpty()) {
				//aici doar stilizez (ca sa apara frumos in modal, nu "numeanimal")
				if(fieldSearch.equals("numestapan"))
					fieldSearch = "Nume Stapan";
				if(fieldSearch.equals("numeanimal"))
					fieldSearch = "Nume Animal";
				if(fieldSearch.equals("datainceput"))
					fieldSearch = "Data Inceput";		
				if(fieldSearch.equals("datasfarsit"))
					fieldSearch = "Data Sfarsit";
				if(fieldSearch.equals("metodatratament"))
					fieldSearch = "Metoda Tratament";	
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				redirectAttributes.addFlashAttribute("fieldSearch", fieldSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		
		}
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		
		listaTratamente = page.getContent();


		// daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		// atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		// altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		
		if(page == null || pageNo <= 0) {
			sirAtribute.add("fararezultat");
			redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		if (listaTratamente.size() == 0) {
			pageNo = page.getTotalPages(); // ultima pagina
			page = tratamentService.findPaginatedTratamenteActiveStapan(id, pageNo, pageSize, sortField, sortDir);			
			listaTratamente = page.getContent();
		}
			
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaTratamente", listaTratamente);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "templates-stapan/eu-tratamente-active";

	}
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//Toate tratamente active pentru stapanul cu ID-ul...
	@RequestMapping("/euTratamenteVechi")
	public String getTratamenteVechiStapan(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteVechiStapan(1, "datainceput", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euTratamenteVechi/page/{pageNo}")
	public String findPaginatedTratamenteVechiStapan(
			@PathVariable(value = "pageNo") int pageNo, 
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		
		Stapan stapan = getStapan();
		model.addAttribute("stapan", stapan);
		long id = stapan.getStapanId();
		model.addAttribute("stapanId", id);
		
		Page<Tratament> page = tratamentService.findPaginatedSearchTratamenteVechiStapan(id, pageNo, pageSize, sortField, sortDir);
		List<Tratament> listaTratamente = page.getContent();
		listaTratamente = page.getContent();

		// daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		// atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		// altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		
		if (listaTratamente.size() == 0) {
			pageNo = page.getTotalPages(); // ultima pagina
			
			if(pageNo==0) {
				sirAtribute.add("fararezultatStapan");
				redirectAttributes.addFlashAttribute("nume_stapan", stapan.getFirstname() + " " + stapan.getLastname());
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}

			page = tratamentService.findPaginatedTratamenteActiveStapan(id, pageNo, pageSize, sortField, sortDir);			
			listaTratamente = page.getContent();
		
		}
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaTratamente", listaTratamente);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "templates-stapan/eu-tratamente-vechi";

	}
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//Toate tratamente active pentru animalul cu ID-ul...
	@RequestMapping("/euTratamenteActiveAnimal/{id}")
	public String getTratamenteAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteAnimal(1, id, "datainceput", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euTratamenteActiveAnimal/{id}/page/{pageNo}")
	public String findPaginatedTratamenteAnimal(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//animalul cu id-ul...din url
		Animal animal = animalService.getAnimal(id).get();
		model.addAttribute("animal", animal);
		
		//luam stapanul CONECTAT
		//luam stapanu utilizand spring security. dupa ii luam id-ul
		Stapan stapan = getStapan();
		model.addAttribute("stapan", stapan);
		
		//acum DACA id-ul stapanului animalului nu coincide cu al stapanului conectat 
		//atunci nu o sa mearga, redirectionare catre /veziAnimaleleTale
		if(animal.getStapanId().getStapanId() != stapan.getStapanId())
			return "redirect:/veziAnimaleleTale";
		
				
		Page<Tratament> page = tratamentService.findPaginatedTratamenteActiveAnimal(id, pageNo, pageSize, sortField, sortDir);
		List<Tratament> listaTratamente = page.getContent();
				
		//daca nu exista inregistrari
		if(page == null || page.getTotalPages() <= 0 ) {
			sirAtribute.add("fararezultatAnimal");
			redirectAttributes.addFlashAttribute("idModal", id);
			redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		listaTratamente = page.getContent();
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaTratamente.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			page = tratamentService.findPaginated(pageNo, pageSize, sortField, sortDir);
			listaTratamente = page.getContent();
		}
		
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaTratamente", listaTratamente);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "templates-stapan/eu-tratamente-active-animal";

	}
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//Toate tratamente vechi pentru animalul cu ID-ul...
	@RequestMapping("/euTratamenteVechiAnimal/{id}")
	public String getTratamenteVechiAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteVechiAnimal(1, id, "datainceput", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/euTratamenteVechiAnimal/{id}/page/{pageNo}")
	public String findPaginatedTratamenteVechiAnimal(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//animalul cu id-ul...din url
		Animal animal = animalService.getAnimal(id).get();
		model.addAttribute("animal", animal);
		
		//luam stapanul CONECTAT
		//luam stapanu utilizand spring security. dupa ii luam id-ul
		Stapan stapan = getStapan();
		model.addAttribute("stapan", stapan);
		
		//acum DACA id-ul stapanului animalului nu coincide cu al stapanului conectat 
		//atunci nu o sa mearga, redirectionare catre /veziAnimaleleTale
		if(animal.getStapanId().getStapanId() != stapan.getStapanId())
			return "redirect:/veziAnimaleleTale";
		
				
		Page<Tratament> page = tratamentService.findPaginatedTratamenteVechiAnimal(id, pageNo, pageSize, sortField, sortDir);
		List<Tratament> listaTratamente = page.getContent();
				
		//daca nu exista inregistrari
		if(page == null || page.getTotalPages() <= 0 ) {
			sirAtribute.add("fararezultatAnimal");
			redirectAttributes.addFlashAttribute("idModal", id);
			redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		listaTratamente = page.getContent();
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaTratamente.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			page = tratamentService.findPaginated(pageNo, pageSize, sortField, sortDir);
			listaTratamente = page.getContent();
		}
		
			
		
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaTratamente", listaTratamente);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		return "templates-stapan/eu-tratamente-vechi-animal";

	}
	
	
////-------------------------------------REDIRECT-------------------------------------
////-------------------------------------REDIRECT-------------------------------------
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~ REDIRECT SI ADAUGARE ATRIBUTE ERORI IN MODAL (ERORI ADAUGARE, ERORI EDITARE, CAUTARE FARA REZULTAT) ~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//redirect
	public String functieRedirect(HttpServletRequest request) {
		String refererURI = "";
		try {refererURI = new URI(request.getHeader("referer")).getPath();} catch (URISyntaxException e) {}
		String refererURL = request.getHeader("referer").toString();
		String parts[] = refererURL.split("\\?");
		String refererParameters = "";
		if(parts.length > 1) {
		
			refererParameters = parts[1];
			return "redirect:" + refererURI + "?" + refererParameters;
		}
		else
			return "redirect:" + refererURI;
	}
	
	//adaugare flash attributes in modal si returnare cale redirect cu functieRedirect
	public String redirectCuAtributeErori(ArrayList<String> sirAtribute, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		for(String atribut : sirAtribute) {
			redirectAttributes.addFlashAttribute(atribut, 1);
		}
		return functieRedirect(request);
	}
	//END ~~~~~~~~~~~~~~~~~~~~~~~ REDIRECT SI ADAUGARE ATRIBUTE ERORI IN MODAL (ERORI ADAUGARE, ERORI EDITARE, CAUTARE FARA REZULTAT) ~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
		
	

	
}

