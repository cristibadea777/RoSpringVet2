package eu.badeacristian.RoSpringVet.controllers;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.badeacristian.RoSpringVet.models.Angajat;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Diagnostic;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.Tratament;
import eu.badeacristian.RoSpringVet.models.Vizita;
import eu.badeacristian.RoSpringVet.models.VizitaWrapper;
import eu.badeacristian.RoSpringVet.services.AngajatService;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import eu.badeacristian.RoSpringVet.services.DiagnosticService;
import eu.badeacristian.RoSpringVet.services.StapanService;
import eu.badeacristian.RoSpringVet.services.TratamentService;
import eu.badeacristian.RoSpringVet.services.VizitaService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j //simple logging facade
public class VizitaController {

	@Autowired
	private VizitaService vizitaService;
	
	@Autowired
	private AnimalService animalService;
	
	@Autowired
	private StapanService stapanService;
	
	@Autowired
	private DiagnosticService diagnosticService;
	
	@Autowired
	private TratamentService tratamentService;
	
	@Autowired
	private AngajatService angajatService;
	
	
	@RequestMapping("/vizite")
	public String getVizite(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedVizita(1, "date", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziVizite/page/{pageNo}")
	public String findPaginatedVizita(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		int pageSize = 5;
		Page<Vizita> page = null;
		List<Vizita> listaVizite = null;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		
		if(vizitaService.getAllVizita().isEmpty()) {
			redirectAttributes.addFlashAttribute("zeroInregistrari", 1);
			return "redirect:/";
		}
		
		
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		if(txtSearch.isEmpty()) {
			//gaseste paginat toate vizitele
			page = vizitaService.findPaginated(pageNo, pageSize, sortField, sortDir);
			listaVizite = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaVizite.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = vizitaService.findPaginated(pageNo, pageSize, sortField, sortDir);
				listaVizite = page.getContent();
			}
		}
		else {
			// gaseste paginat vizite al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = vizitaService.findPaginatedSearch(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
			//daca utilizatorul introduce in URL un nume de camp ce nu exista
			//atunci pagina o sa fie null, si o sa dea eroare.
			//deci il redirectionam inapoi pe prima pagina
			if(page == null) {
				log.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				log.warn("URL inexistent:");
				log.warn(request.getRequestURL().toString() + "?" + request.getQueryString());
				log.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				redirectAttributes.addFlashAttribute("campinexistent");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			
			//daca pagina este goala, adica nu am gasit nici un rezultat pentru cautarea noastra
			//atunci mergem inapoi pe pagina stapanilor si informam utilizatorul deschizand un modal
			if(page.isEmpty()) {
				//aici doar stilizez (ca sa apara frumos in modal, nu "numestapan")
				if(fieldSearch.equals("numestapan"))
					fieldSearch = "Nume Stapan";
				if(fieldSearch.equals("numeanimal"))
					fieldSearch = "Nume Animal";
				if(fieldSearch.equals("date"))
					fieldSearch = "Data";
				if(fieldSearch.equals("motiv"))
					fieldSearch = "Motiv";
				//redirect pt cautare fara rezultat
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				redirectAttributes.addFlashAttribute("cautareCamp", fieldSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		
		}
		listaVizite = page.getContent();
		
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		
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
		
		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "vizite";

	}

	
	@PostMapping("/salveazaVizita")
	public String saveVizita(
			@ModelAttribute("vizitawrapper") VizitaWrapper vizitawrapper, 
			@ModelAttribute("dropAngajat") Angajat angajatId, 
			@RequestParam("currentPage") int currentPage,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		long idModal = vizitawrapper.getVizita().getAnimalId().getAnimalId();
		redirectAttributes.addFlashAttribute("idModal", idModal);
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//daca veterinar e null
		if(angajatId.getAngajatId() == 1) {
			//"selecteaza angajat" din combo-box are valoarea 1
			sirAtribute.add("errviz");
			sirAtribute.add("errangajat");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		//daca data vizitei e null
		if(vizitawrapper.getVizita().getDate() == null ) {
			sirAtribute.add("errviz");
			sirAtribute.add("errvizdata");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		//daca diagnostic e null
		if(vizitawrapper.getDiagnostic().getDiagnostic().isEmpty()) {
			sirAtribute.add("errviz");
			sirAtribute.add("errdiagnostic");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		//daca data incepere tratament e null
		if(vizitawrapper.getTratament().getDatainceput() == null) {
			sirAtribute.add("errviz");
			sirAtribute.add("errtratinceput");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		//daca tratament e null
		if(vizitawrapper.getTratament().getMetodatratament().isEmpty()) {
			sirAtribute.add("errviz");
			sirAtribute.add("errtratament");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		//altfel:
		try {
			//Pentru ca obiectele nu erau inca salvate, cheile lor straine o sa fie nulle, de aia le setez aici dupa ce salvez pe fiecare 
			
			//salvare diagnostic in db
			vizitawrapper.getDiagnostic().setTratamentId(vizitawrapper.getTratament());
			diagnosticService.addDiagnostic(vizitawrapper.getDiagnostic());
			
			//salvare tratament in db
			vizitawrapper.getTratament().setDiagnosticId(vizitawrapper.getDiagnostic());
			tratamentService.addTratament(vizitawrapper.getTratament());
			
			//angajatul preluat din combobox
			Angajat angajat = angajatService.getAngajat(angajatId.getAngajatId()).get();
			String nume_angajat = angajat.getLastname() + " " + angajat.getFirstname();
			
			// salvare vizita in db - setare tratament si diagnostic
			vizitawrapper.getVizita().setDiagnosticId(vizitawrapper.getDiagnostic());
			vizitawrapper.getVizita().setTratamentId(vizitawrapper.getTratament());
			vizitawrapper.getVizita().setAngajatId(angajat);
			vizitawrapper.getVizita().setNumeangajat(nume_angajat);
			vizitaService.addVizita(vizitawrapper.getVizita());

		}catch(Exception e) {
			log.error(e.toString());
		}
		
		// redirect
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
	}
	
	
	@RequestMapping("/viziteAnimal/{id}")
	public String getViziteAnimal(Model model, @PathVariable(value = "id") long id){
		return findPaginatedViziteAnimal(1, id, "date", "asc", model); // returneaza prima pagina
	}
	
	@GetMapping("/veziViziteAnimal/{id}/page/{pageNo}")
	public String findPaginatedViziteAnimal(@PathVariable(value = "pageNo") int pageNo, @PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, @RequestParam("sortDir") String sortDir, Model model) {

		int pageSize = 5;
		Page<Vizita> page = vizitaService.findPaginatedAnimal(id, pageNo, pageSize, sortField, sortDir);
		List<Vizita> listaVizite = page.getContent();

		Animal animal = animalService.getAnimal(id).get();
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
		
		//Atribute pt redirectionare
		//Nume view
		model.addAttribute("numeView", "veziViziteAnimal");
		
		return "animal-vizite";

	}
	
	
	@RequestMapping("/viziteStapan/{id}")
	public String getViziteStapan(Model model, @PathVariable(value = "id") long id){
		return findPaginatedViziteStapan(1, id, "date", "asc", model); // returneaza prima pagina
	}
	
	@GetMapping("/veziViziteStapan/{id}/page/{pageNo}")
	public String findPaginatedViziteStapan(@PathVariable(value = "pageNo") int pageNo, @PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, @RequestParam("sortDir") String sortDir, Model model) {

		int pageSize = 5;
		Page<Vizita> page = vizitaService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
		List<Vizita> listaVizite = page.getContent();

		Stapan stapan = stapanService.getStapan(id).get();
		model.addAttribute("stapan", stapan);
		
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
		return "stapan-vizite";

	}
	
	
	@RequestMapping("/viziteAngajat/{id}")
	public String getViziteAngajat(Model model, @PathVariable(value = "id") long id){
		return findPaginatedViziteAngajat(1, id, "date", "asc", model); // returneaza prima pagina
	}

	@GetMapping("/veziViziteAngajat/{id}/page/{pageNo}")
	public String findPaginatedViziteAngajat(@PathVariable(value = "pageNo") int pageNo, @PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, @RequestParam("sortDir") String sortDir, Model model) {

		int pageSize = 5;
		Page<Vizita> page = vizitaService.findPaginatedAngajati(id, pageNo, pageSize, sortField, sortDir);
		List<Vizita> listaVizite = page.getContent();

		Angajat angajat = angajatService.getAngajat(id).get();
		model.addAttribute("angajat", angajat);
		
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
		return "angajat-vizite";

	}
	
	
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
