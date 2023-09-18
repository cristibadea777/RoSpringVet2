package eu.badeacristian.RoSpringVet.controllers;


import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import eu.badeacristian.RoSpringVet.models.Animal;
import eu.badeacristian.RoSpringVet.models.Programare;
import eu.badeacristian.RoSpringVet.models.Role;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.User;
import eu.badeacristian.RoSpringVet.repositories.UserRepository;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import eu.badeacristian.RoSpringVet.services.EmailService;
import eu.badeacristian.RoSpringVet.services.ProgramareService;
import eu.badeacristian.RoSpringVet.services.StapanService;
import eu.badeacristian.RoSpringVet.services.UserService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ProgramareController {

	@Autowired
	private ProgramareService programareService;
	
	@Autowired
	private StapanService stapanService;
	
	@Autowired
	private AnimalService animalService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	

	
	private boolean userEsteAngajat() { 
		//~~~~~~~~~~~~~~~~~pentru editare programare
		//editeaza angajatul => stare programare = confirmata
		//editeaza stapanul=> stare programare = neconfirmara
		
		String username = "wut";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
		  username = ((UserDetails)principal).getUsername();
		} 
		else 
		  username = principal.toString();
		
		log.warn("LOGGED USER - USERNAME:" 	+ username);
		log.warn("LOGGED USER - ROLES");
		Collection<Role> roluri = userService.getUser(username).getRoles();
		
		for (Role rol : roluri) {
			log.warn("~~~~~~~~~~~~~~~~~~~ " + rol.getName());
			if(rol.getName().equals("ROL_ANGAJAT"))
				return true;
		}
		return false;
	}
	
	
	
	@PostMapping("/adaugaProgramare")
	public String adaugaProgramare(
			@ModelAttribute("programare") Programare programare,
			@RequestParam("currentPage") int currentPage,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@ModelAttribute("txtSearch") String txtSearch,
			@ModelAttribute("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		long idModal = programare.getAnimalId().getAnimalId();
		redirectAttributes.addFlashAttribute("idModal", idModal);
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		
		if(programare.getMotiv().isEmpty()) {
			sirAtribute.add("errAddProgramare");
			sirAtribute.add("errMotivProgramare");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		if(programare.getDate() == null) {
			sirAtribute.add("errAddProgramare");
			sirAtribute.add("errDataProgramare");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		LocalDate currentDate = LocalDate.now();
	    Date progData = programare.getDate();
	    LocalDate data = progData.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		if(data.isBefore(currentDate)) {
			sirAtribute.add("errAddProgramare");
			sirAtribute.add("errDataProgramareTrecut");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		//~~~ salvare programare in db ~~~		
		//Daca o adauga un angajat, atunci e CONFIRMATA 
		//Daca o adauga un stapan, atunci e NECONFIRMATA
		String username = "wut";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
		  username = ((UserDetails)principal).getUsername();
		} else {
		  username = principal.toString();
		}		
		Stapan stapan = stapanService.getStapanByEmail(username);		
		if(stapan == null)  
			programare.setStare("confirmata");//daca mail-ul nu se gaseste in repozitoriul stapani atunci e clar ca angajatul face programarea, atunci o setam ca fiind confirmata
		else 
			programare.setStare("neconfirmata");
		
		programareService.addProgramare(programare);
				
		//email catre stapan programare schimbata (programare confirmata)
		User user = userRepository.findByEmail(programare.getStapanId().getEmail());
		String nume = programare.getAnimalId().getNume();
		//email Confirmata daca o face angajatul
		//email Neconfirmata daca o face stapanul
		if(programare.getStare().equals("confirmata"))
			emailService.sendEmailProgramareConfirmata(user, programare.getDate(), nume);
		else
			emailService.sendEmailProgramareNeconfirmata(user, programare.getDate(), nume);
		
		//redirect
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
	}
	
	@PostMapping("/editareProgramare")
	public String editeazaProgramare(
			@ModelAttribute("programare") Programare programare,
			@RequestParam("currentPage") int currentPage,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {		
		
		Optional<Programare> programare_veche = programareService.getProgramare(programare.getProgramareId());
		long idModal = programare_veche.get().getProgramareId();
		redirectAttributes.addFlashAttribute("idModal", idModal);
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		if(programare.getMotiv().isEmpty()) {
			sirAtribute.add("errUppProgramare");
			sirAtribute.add("errUppMotivProgramare");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		if(programare.getDate() == null) {
			sirAtribute.add("errUppProgramare");
			sirAtribute.add("errUppDataProgramare");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		//schimbare stare 
		if(userEsteAngajat() == true) {
			programare.setStare("confirmata");
			log.warn("############################################# TRUE");
		}
		else {
			log.warn("############################################# FALSE");
			programare.setStare("neconfirmata");
		}
			
		//salvare programare in db
		programareService.updateProgramare(programare);
		
		//email catre stapan programare schimbata (programare confirmata)
		User stapan = userRepository.findByEmail(programare.getStapanId().getEmail());
		String nume = programare.getAnimalId().getNume();
		if(programare.getStare().equals("confirmata"))
			emailService.sendEmailProgramareConfirmata(stapan, programare.getDate(), nume);
		else
			emailService.sendEmailProgramareNeconfirmata(stapan, programare.getDate(), nume);
		
		
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		
	}
	
	
	@GetMapping("/programariAzi")
	private String arataProgramariAzi(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String txtSearch = (LocalDate.now()).toString();
		String fieldSearch = "date";
		return findPaginatedProgramari(1, "date", "asc", txtSearch, fieldSearch, request, redirectAttributes, model); // returneaza prima pagina
	}
	@GetMapping("/programariMaine")
	private String arataProgramariMaine(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String txtSearch = (LocalDate.now().plusDays(1)).toString();
		String fieldSearch = "date";
		return findPaginatedProgramari(1, "date", "asc", txtSearch, fieldSearch, request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/programari")
	private String arataProgramari(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return findPaginatedProgramari(1, "date", "asc", "", "", request,redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziProgramari/page/{pageNo}")
	public String findPaginatedProgramari(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		stergereProgramariVechi();
		
		// atribute pentru a edita o programare
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		// atribute pentru numele vederii
		int pageSize = 5;
		Page<Programare> page = null;
		List<Programare> listaProgramari = null;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//daca exista programari
		//in caz ca nu exista deloc, dam doar template-ul
		if(programareService.getAllProgramari().isEmpty())
			return "programari";
		
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		if(txtSearch.isEmpty()) {
			//gaseste paginat toate programarile
			page = programareService.findPaginatedProgramare(pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
		else {			
			//gaseste paginat toate programarile in functie de camp si text
			page = programareService.findPaginatedSearch(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
			//daca cautarea nu are rezultate, atunci ne intoarcem la programari pe prima pagina 
			//daca pagina este goala, adica nu am gasit nici un rezultat pentru cautarea noastra
			//atunci mergem inapoi
			if(page.isEmpty()) {
				//aici doar stilizez (ca sa apara frumos in modal, nu "numestapan")
				if(fieldSearch.equals("numestapan"))
					fieldSearch = "Nume Stapan";
				if(fieldSearch.equals("numeanimal"))
					fieldSearch = "Nume Animal";
				if(fieldSearch.equals("date"))
					fieldSearch = "Data";		
				redirectAttributes.addFlashAttribute("cautareCamp", fieldSearch);
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}		
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		
		listaProgramari = page.getContent();
		
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

		//data pentru cautare 
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "programari";

	}
	
	
	public void stergereProgramariVechi() {
		List<Programare> programari = programareService.getAllProgramari();
		//~~~~~~~~~~~~stergere programari vechi~~~~~~~~~~~~
		for(Programare programare : programari) {	
			LocalDate currentDate = LocalDate.now();
		    Date progData = programare.getDate();
		    LocalDate data = progData.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if(data.isBefore(currentDate)) {
				try {
					programareService.deleteProgramare(programare);
				}catch(Exception ex){
					log.warn(ex.toString());
				}
				log.warn("@@@@@@@ 1 PROGRAMARE VECHE STEARSA @@@@@@@@");
			}
		}
	}
	
	
	
	@PostMapping("/anulareProgramare")
	public String stergeProgramare(
			@ModelAttribute("programareId") long programareId,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model){
		ArrayList<String> sirAtribute = new ArrayList<String>();
		Programare programare = programareService.getProgramare(programareId).get();
		User user = userRepository.findByEmail(programare.getStapanId().getEmail());
		String nume = programare.getAnimalId().getNume();
		
		//daca era confirmata si o sterge angajatul, atunci mail Programare Anulata
		//altfel, dara era neconfirmata, atunci e Refuzata
		if(programare.getStare().equals("confirmata"))
			emailService.sendEmailProgramareAnulata(user, programare.getDate(), nume);
		else
			emailService.sendEmailProgramareRefuzata(user, programare.getDate(), nume);
		//email programare amanata
		
		programareService.deleteProgramare(programareId);
		
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
	}
	
	
	
	@RequestMapping("/programariStapan/{id}")
	public String getProgramariStapan(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariStapan(1, id, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziProgramariStapan/{id}/page/{pageNo}")
	public String findPaginatedProgramariStapan(
			@PathVariable(value = "pageNo") int pageNo, 
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
				
		
		//log.warn(String.valueOf(model.getAttribute("fararezultatTratamente"))); 
		//valoarea faraRezultatTratamente in model in Controler-ul Tratamente cu flash attributes pt redirect se pastreaza
		//acum ne putem folosi de ea in template cu Thymeleaf
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		int pageSize = 5;
		Page<Programare> page = programareService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
		List<Programare> listaProgramari = page.getContent();
		
		Stapan stapan = stapanService.getStapan(id).get();
		String nume_stapan = stapan.getLastname() + " " + stapan.getFirstname();
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu stapanului
			if(pageNo==0) {
				sirAtribute.add("fararezultatStapan");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_stapan", nume_stapan);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			page = programareService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
		
		model.addAttribute("stapan", stapan);
		model.addAttribute("nume_stapan", nume_stapan);
		
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
		
		//data pentru cautare 
		//model.addAttribute("data", data);
		
		return "stapan-programari";

	}
	
	
	@RequestMapping("/programariAnimal/{id}")
	public String getProgramariAnimal(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariAnimal(1, id, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziProgramariAnimal/{id}/page/{pageNo}")
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
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		Animal animal = animalService.getAnimal(id).get();
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu stapanului
			if(pageNo==0) {
				sirAtribute.add("fararezultatAnimal");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			page = programareService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}

		model.addAttribute("animal", animal);
		
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
		
		return "animal-programari";

	}
	
	
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Pt programari neconfirmate ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	//~~~~~~~~~~~~~~~~~ CONFIRMA PROGRAMARE
	
	//Cand confirmam programarea, doar ii schimbam starea din neconfirmata in confirmata.
	
	@PostMapping("/confirmareProgramare")
	private String confirmaProgramarea(
			@ModelAttribute("programareId") long programareId,
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("data") String data,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
				
		Programare programare = programareService.getProgramare(programareId).get();
		programare.setStare("confirmata");
		programareService.addProgramare(programare);
		
		User user = userRepository.findByEmail(programare.getStapanId().getEmail());
		String nume = user.getFirstname() + " " + user.getLastname();
		
		if(programare.getStare().equals("confirmata"))
			emailService.sendEmailProgramareConfirmata(user, programare.getDate(), nume);
		
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		
	}
	
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE AZI SI TOTAL
	
	@GetMapping("/programariNeconfirmateAzi")
	private String arataProgramariNeconfirmateAzi(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String txtSearch = LocalDate.now().toString();
		String fieldSearch = "date";
		return findPaginatedProgramariNeconfirmate(1, "date", "asc", txtSearch, fieldSearch, request, redirectAttributes, model); // returneaza prima pagina
	}
	@GetMapping("/programariNeconfirmateMaine")
	private String arataProgramariNeconfirmateMaine(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String txtSearch = (LocalDate.now().plusDays(1)).toString();
		String fieldSearch = "date";
		return findPaginatedProgramariNeconfirmate(1, "date", "asc", txtSearch, fieldSearch, request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/programariNeconfirmate")
	private String arataProgramariNeconfirmate(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return findPaginatedProgramariNeconfirmate(1, "date", "asc", "", "", request,redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziProgramariNeconfirmate/page/{pageNo}")
	public String findPaginatedProgramariNeconfirmate(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		stergereProgramariVechi();
		
		// atribute pentru a edita o programare
		Programare programare = new Programare();
		model.addAttribute("programare", programare);
		// atribute pentru numele vederii
		int pageSize = 5;
		Page<Programare> page = null;
		List<Programare> listaProgramari = null;
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		if(txtSearch.isEmpty()) {
			//gaseste paginat toate programarile
			page = programareService.findPaginatedProgramareNeconfirmata(pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
		else {
			//gaseste paginat toate programarile in functie de camp si text
			page = programareService.findPaginatedSearchNeconfirmate(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
		
			//daca utilizatorul introduce in URL un nume de camp ce nu exista
			//atunci pagina o sa fie null, si o sa dea eroare.
			//deci il redirectionam inapoi 
			if(page == null) { 
				sirAtribute.add("fararezultat");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			//daca cautarea nu are rezultate, atunci ne intoarcem la programari pe prima pagina (asta la if-ul mai jos)
			//asta daca exista programari
			//in caz ca nu exista deloc, dam doar template-ul
			if(programareService.getAllProgramari().isEmpty())//aici neconf
				return "programari";
			
			//daca pagina este goala, adica nu am gasit nici un rezultat pentru cautarea noastra
			//atunci mergem inapoi
			if(page.isEmpty()) {
				//aici doar stilizez (ca sa apara frumos in modal, nu "numestapan")
				if(fieldSearch.equals("numestapan"))
					fieldSearch = "Nume Stapan";
				if(fieldSearch.equals("numeanimal"))
					fieldSearch = "Nume Animal";
				if(fieldSearch.equals("date"))
					fieldSearch = "Data";		
				redirectAttributes.addFlashAttribute("cautareCamp", fieldSearch);
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}		
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		
		listaProgramari = page.getContent();
		
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
		
		//data pentru cautare 
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "programari-neconfirmate";

	}
	
	
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE STAPAN
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE STAPAN
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE STAPAN	
	@RequestMapping("/programariStapanNeconfirmate/{id}")
	public String getProgramariStapanNeconfirmate(
			@PathVariable(value = "id") long id,
			Model model){
		return findPaginatedProgramariStapanNeconfirmate(id, 1, "date", "asc", model); // returneaza prima pagina
	}
	
	@GetMapping("/veziProgramariStapanNeconfirmate/{id}/page/{pageNo}")
	public String findPaginatedProgramariStapanNeconfirmate(
			@PathVariable(value = "id") long id,
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			Model model) {
		int pageSize = 5;
		Page<Programare> page = programareService.findPaginatedStapanNeconfirmata(id, pageNo, pageSize, sortField, sortDir);
		List<Programare> listaProgramari = page.getContent();

		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			if(pageNo == 0)
				return "redirect:/profilStapan/"+id;
			page = programareService.findPaginatedStapanNeconfirmata(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}
		
		Stapan stapan = stapanService.getStapan(id).get();
		model.addAttribute("stapan", stapan);
		
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

		return "stapan-programari-neconfirmate";

	}

	
	
	
	
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE ANIMAL
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE ANIMAL
	//~~~~~~~~~~~~~~~~~ PROGRAMARARI NECONFIRMATE ANIMAL		
	@RequestMapping("/programariAnimalNeconfirmate/{id}")
	public String getProgramariAnimalNeconfirmate(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes){
		return findPaginatedProgramariAnimalNeconfirmate(1, id, "date", "asc", request, redirectAttributes, model); // returneaza prima pagina
	}
	
	@GetMapping("/veziProgramariAnimalNeconfirmate/{id}/page/{pageNo}")
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
		
		//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
		//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
		//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
		if(listaProgramari.size() == 0) {
			pageNo = page.getTotalPages(); //ultima pagina
			//daca pagina e 0 adica nu exista inregistrari atunci mergem inapoi la profilu stapanului
			if(pageNo==0) {
				sirAtribute.add("fararezultatAnimal");
				redirectAttributes.addFlashAttribute("idModal", id);
				redirectAttributes.addFlashAttribute("nume_animal", animal.getNume());
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
			page = programareService.findPaginatedStapan(id, pageNo, pageSize, sortField, sortDir);
			listaProgramari = page.getContent();
		}

		model.addAttribute("animal", animal);
		
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
		
		
		return "animal-programari-neconfirmate";

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
