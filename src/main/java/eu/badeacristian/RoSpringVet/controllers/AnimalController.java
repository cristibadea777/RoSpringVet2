package eu.badeacristian.RoSpringVet.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import eu.badeacristian.RoSpringVet.models.Angajat;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Diagnostic;
import eu.badeacristian.RoSpringVet.models.Programare;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.Tratament;
import eu.badeacristian.RoSpringVet.models.Vizita;
import eu.badeacristian.RoSpringVet.models.VizitaWrapper;
import eu.badeacristian.RoSpringVet.services.AngajatService;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j

public class AnimalController {

	@Autowired
	private AnimalService animalService;
	
	@Autowired
	private AngajatService angajatService;
	
	@Value("${external.resources.path}")
    private String path; 

	
	@GetMapping("/animale")
	private String actiuniAnimalStapani(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return findPaginatedAnimale(1, "nume", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}

	@GetMapping("/veziAnimale/page/{pageNo}")
	public String findPaginatedAnimale(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		int pageSize = 5;		
		Page<Animal> page = null;
		List<Animal> listaAnimale = null;
		
		if(animalService.getAllAnimale().isEmpty()) {
			redirectAttributes.addFlashAttribute("zeroInregistrari", 1);
			return "redirect:/";
		}		
		
		////FUNCTIE SEARCH ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		////FUNCTIE SEARCH ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if(txtSearch.isEmpty()) {
			//gaseste paginat toate animalele
			page = animalService.findPaginatedAnimal(pageNo, pageSize, sortField, sortDir);
			listaAnimale = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaAnimale.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = animalService.findPaginatedAnimal(pageNo, pageSize, sortField, sortDir);
				listaAnimale = page.getContent();
			}
			
		}
		else {
			//gaseste paginat animalele ale caror <<fieldSearch>> incep cu <<txtSearch>>
			page = animalService.findPaginatedSearchAnimale(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
					
			//daca utilizatorul introduce in URL un nume de camp ce nu exista
			//atunci pagina o sa fie null, si o sa dea eroare.
			//deci il redirectionam inapoi pe prima pagina
			if(page == null) {
				log.warn("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				log.warn("URL inexistent:");
				log.warn(request.getRequestURL().toString() + "?" + request.getQueryString());
				sirAtribute.add("campinexistent");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			
			
			//daca pagina este goala, adica nu am gasit nici un rezultat pentru cautarea noastra
			//atunci mergem inapoi pe pagina stapanilor si informam utilizatorul deschizand un modal
			if(page.isEmpty()) {
				//aici doar traduc in Română 
				if(fieldSearch.equals("nume"))
					fieldSearch = "Nume";
				if(fieldSearch.equals("rasa"))
					fieldSearch = "Rasa";
				if(fieldSearch.equals("specie"))
					fieldSearch = "Specie";			

				//redirect pt cautare fara rezultat
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				redirectAttributes.addFlashAttribute("cautareCamp", fieldSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
						
		}
		////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ END FUNCTIE SEARCH
		////~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ END FUNCTIE SEARCH
		
		listaAnimale = page.getContent();
		
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
		
		//nume view - pt redirectionare fie la veziAnimale fie la veziAnimaleStapan (in cazu asta e veziAnimale)
		model.addAttribute("numeView", "veziAnimale");
		
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
		
		//Atribute pentru adaugarea unei programari
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
				
		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "animale";
	

	}
	
	@PostMapping("/editareAnimal")
	public String editareAnimal(
			@ModelAttribute("animal") @Valid Animal animal,
			Errors errors, 
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@ModelAttribute("numeView") String numeView,
			@ModelAttribute("imageFile") MultipartFile imageFile,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model){
		
		//pt erori
		ArrayList<String> sirAtribute = new ArrayList<String>();
		sirAtribute.add("erredit");
		
		Animal animal_vechi = animalService.getAnimal(animal.getAnimalId()).get();
		Stapan stapan = animal_vechi.getStapanId();
		
		//ID-ul modalului ce va aparea pe ecran, la fel cu al animalului
		long idModal = animal_vechi.getAnimalId();
		redirectAttributes.addFlashAttribute("idModal", idModal);

		if(errors.hasErrors()) {	
			if(errors.getFieldError("nume") != null)
				sirAtribute.add("errnume");
			if(errors.getFieldError("specie") != null) 
				sirAtribute.add("errspecie");
			if(errors.getFieldError("imagine") != null) 
				sirAtribute.add("errpoza");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}		
		
		String nume_poza =  animal_vechi.getImagine();		
		
		//daca userul a selectat o imagine noua pentru animal - adica daca variabila imageFile nu este goala, atunci setam acea imagine
		//pt poza animalului, salvare poza
		if(! imageFile.isEmpty()) {
			try {
				nume_poza = salvarePoza(stapan, animal, imageFile);
			}catch(Exception e) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.warn("EROARE: " + e);
				sirAtribute.add("errpoza");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}
		
		//setare nume poza pt animal in baza de date 
		//daca nu a fost selectat nimic, ramane cea veche, pt ca am initializat variabila nume_poza deja cu valoarea animalului vechi
		//daca a fost selectata alta poza, atunci nume_poza va lua valoarea calculata de metoda salvarePoza
		animal.setImagine(nume_poza);
		//nu luam id-ul stapanului in formular, asa ca il setam in controller utilizandu-ne de vechiul animal
		animal.setStapanId(animal_vechi.getStapanId());
		//salvare animal editat 
		animalService.addAnimal(animal); //daca obiectul animal nu are erori atunci il salvam
		
		//daca totu e bine ne intoarcem pe pagina tabelului cu aceeasi directie de sortare si camp dupa care sortam
		sirAtribute.add("successEdit");
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		
	}
	
	@GetMapping("/stergereAnimal")
	public String stergereAnimal(
			@ModelAttribute("animalId") long animalId,
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("numeView") String numeView) {
		
		long id = animalService.getAnimal(animalId).get().getStapanId().getStapanId(); //inainte de a sterge animalu, luam ID-ul stapanului pentru redirectionare
		//stergem animalul
		animalService.deleteAnimal(animalId);
		//redirect
				
		if(numeView.equals("veziAnimale"))
			return  "redirect:/" + "veziAnimale" + "/page/" + currentPage + "?sortField=" + sortField + "&sortDir=" + sortDir;
		
		if(numeView.equals("veziAnimaleleTale"))
			return "redirect:/" + numeView + "/page/" + currentPage + "?sortField=" + sortField + "&sortDir=" + sortDir; //veziAnimaleleTale nu are ID, e al stapanului si e luat din controller
		
		return "redirect:" + numeView + "/" + id + "/page/" + currentPage + "?sortField=" + sortField + "&sortDir=" + sortDir;	 //daca totu e bine ne intoarcem pe pagina tabelului cu aceeasi directie de sortare si camp dupa care sortam
	}
	
		
	//poza se salveaza in folderul extern si va returna si numele pozei pt a se salva SI in BD	
	//variabila path e declarat la inceput, valoarea o luam cu @Value din application.properties
	//calea finala catre poza salvata (final_image_path) va fi din
	//calea catre folderul extern + 
	//		+ separator (/) +
	// 		+ imagini_animale (folderul din resources in care punem pozele animalelor) +
	// 		+ numele imaginii 
	//pe care o generam noi din id-ul animalului numele animalului, nume stapan, extensia pozei
	public String salvarePoza(Stapan stapan, Animal animal, MultipartFile imageFile) throws IOException{
			String nume_poza = "";
			//setare nume poza cu numele (nou) si id-ul animalului si numele stapanului
			String extensie = imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf("."));
			nume_poza = "stapan_" + stapan.getStapanId() + "_animal_" + animal.getAnimalId() + extensie;
							
			//salvare poza in folderul poze_animale
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
			byte[] bytes = imageFile.getBytes();
			Path final_image_path = Paths.get(path + File.separator + "poze_animale" + File.separator + nume_poza);
			Files.write(final_image_path, bytes);
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			return nume_poza;
		
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
