package eu.badeacristian.RoSpringVet.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.Tratament;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import eu.badeacristian.RoSpringVet.services.StapanService;
import eu.badeacristian.RoSpringVet.services.TratamentService;

@Controller
public class TratamentController {

	@Autowired
	private TratamentService tratamentService;
	
	@Autowired
	private AnimalService animalService;
	
	
	@Autowired
	private StapanService stapanService;
	
	
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Toate Tratamentele@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Toate Tratamentele@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Toate Tratamentele@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	@GetMapping("/tratamente")
	private String actiuniTratamente(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		
		return findPaginatedTratament(1, "datasfarsit", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}

	@GetMapping("/veziTratamente/page/{pageNo}")
	public String findPaginatedTratament(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		int pageSize = 5;
		
		Page<Tratament> page = null;
		List<Tratament> listaTratamente = null;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		if(txtSearch.isEmpty()) {
			
			//gaseste paginat toate tratamentele
			page = tratamentService.findPaginated(pageNo, pageSize, sortField, sortDir);
			
			//daca nu exista inregistrari
			if(page.getTotalPages() <= 0 ) {
				redirectAttributes.addFlashAttribute("zeroInregistrari", 1);
				return "redirect:/";
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
			
		}
		else {
			// gaseste paginat tratamente al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = tratamentService.findPaginatedSearchToateTratamenteActive(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
			//daca pagina este goala, adica nu am gasit nici un rezultat pentru cautarea noastra
			//atunci mergem inapoi pe pagina stapanilor si informam utilizatorul deschizand un modal
			if(page.isEmpty()) {
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
				//redirect pt cautare fara rezultat
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				redirectAttributes.addFlashAttribute("cautareCamp", fieldSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}

		}
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		
		listaTratamente = page.getContent();
		// daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		// atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		// altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if (listaTratamente.size() == 0) {
			pageNo = page.getTotalPages(); // ultima pagina
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
		//tratament - pt edit
		Tratament tratament = new Tratament();
		model.addAttribute("tratament", tratament);
		//nume view 
		String numeView = "veziTratamente";
		model.addAttribute("numeView", numeView);
		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "tratamente";

	}
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Editare Tratament@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Editare Tratament@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Editare Tratament@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	@PostMapping("/editareTratament")
	public String editareTratament(
			@ModelAttribute("tratament") @Valid Tratament tratament,
			@RequestParam("currentPage") int currentPage,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Errors errors,
			Model model){
		
		long idModal = tratament.getTratamentId(); //id modal care sa fie deschis daca are erori
		redirectAttributes.addFlashAttribute("idModal", idModal);
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		
		if(tratament.getMetodatratament().isEmpty()) {
			sirAtribute.add("errtratament");
			sirAtribute.add("errmetodatratament");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
			
		if(tratament.getDatasfarsit() == null) {
			sirAtribute.add("errtratament");
			sirAtribute.add("errdatasfarsit");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		//setare data inceput de la vechiul tratament (nu merge luata din template cu input field ascunse asa cum fac pt restu, nu ma mai complic sa vad de ce)
		Tratament tratament_vechi = tratamentService.getTratament(tratament.getTratamentId()).get();
		tratament.setDatainceput(tratament_vechi.getDatainceput());
		//salvare
		tratamentService.updateTratament(tratament);
		
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
	}
	
	
	
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//Toate tratamente active pentru animalul cu ID-ul...
	@RequestMapping("/tratamenteActive/{id}")
	public String getTratamenteAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteAnimal(1, id, "datainceput", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziTratamenteAnimal/{id}/page/{pageNo}")
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
		
		Animal animal = animalService.getAnimal(id).get();
		model.addAttribute("animal", animal);
		
		Stapan stapan = stapanService.getStapan(animal.getStapanId().getStapanId()).get();
		model.addAttribute("stapan", stapan);
				
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
		
		return "animal-tratamente-active";

	}

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Animal@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//Toate tratamente vechi pentru animalul cu ID-ul...
	@RequestMapping("/tratamenteVechiAnimal/{id}")
	public String getTratamenteVechiAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteVechiAnimal(1, id, "datainceput", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziTratamenteVechiAnimal/{id}/page/{pageNo}")
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
		
		Animal animal = animalService.getAnimal(id).get();
		model.addAttribute("animal", animal);
		
		Stapan stapan = stapanService.getStapan(animal.getStapanId().getStapanId()).get();
		model.addAttribute("stapan", stapan);
				
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
		
		return "animal-tratamente-vechi";

	}
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Active Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//Toate tratamente active pentru stapanul cu ID-ul...
	@RequestMapping("/tratamenteActiveStapan/{id}")
	public String getTratamenteStapan(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteActiveStapan(1, id, "datainceput", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziTratamenteActiveStapan/{id}/page/{pageNo}")
	public String findPaginatedTratamenteActiveStapan(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		
		Stapan stapan = stapanService.getStapan(id).get();
		model.addAttribute("stapan", stapan);
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
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_stapan", stapan.getFirstname() + " " + stapan.getLastname());
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
		
		return "stapan-tratamente-active";

	}
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@Tratamente Vechi Stapan@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	
	//Toate tratamente active pentru stapanul cu ID-ul...
	@RequestMapping("/tratamenteVechiStapan/{id}")
	public String getTratamenteVechiStapan(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedTratamenteVechiStapan(1, id, "datainceput", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziTratamenteVechiStapan/{id}/page/{pageNo}")
	public String findPaginatedTratamenteVechiStapan(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		
		Stapan stapan = stapanService.getStapan(id).get();
		model.addAttribute("stapan", stapan);
		model.addAttribute("stapanId", id);
		
		Page<Tratament> page = tratamentService.findPaginatedSearchTratamenteVechiStapan(id, pageNo, pageSize, sortField, sortDir);
		List<Tratament> listaTratamente = page.getContent();
		listaTratamente = page.getContent();

		// daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		// atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		// altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		
		if (listaTratamente.size() == 0) {
			pageNo = page.getTotalPages(); // ultima pagina
			
			//daca nu exista inregistrari
			if(page == null || page.getTotalPages() <= 0 ) {
				sirAtribute.add("fararezultatStapan");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_stapan", stapan.getFirstname() + " " + stapan.getLastname());
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
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
		
		return "stapan-tratamente-vechi";

	}
	
	
	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@FUNCTIE REDIRECT@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@FUNCTIE REDIRECT@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@	
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@FUNCTIE REDIRECT@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	private String functieRedirect(HttpServletRequest request) {
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
