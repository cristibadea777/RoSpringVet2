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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import eu.badeacristian.RoSpringVet.models.User;
import eu.badeacristian.RoSpringVet.models.UserRegistrationDTO;
import eu.badeacristian.RoSpringVet.models.Vizita;
import eu.badeacristian.RoSpringVet.models.VizitaWrapper;
import eu.badeacristian.RoSpringVet.repositories.UserRepository;
import eu.badeacristian.RoSpringVet.services.AngajatService;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import eu.badeacristian.RoSpringVet.services.EmailService;
import eu.badeacristian.RoSpringVet.services.StapanService;
import eu.badeacristian.RoSpringVet.services.UserService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class StapanController {

	@Autowired
	private StapanService stapanService;
	
	@Autowired
	private UserService userService;

	@Autowired
	UserRepository userRepository;

	
	@Autowired
	private AnimalService animalService;

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private AngajatService angajatService;
	
	@Value("${external.resources.path}")
    private String path; 
	
	@GetMapping("/stapani")
	private String actiuniStapani(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		
		return findPaginatedStapan(1, "firstname", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}

	@GetMapping("/veziStapani/page/{pageNo}")
	public String findPaginatedStapan(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
		
		int pageSize = 5;
		
		Page<Stapan> page = null;
		List<Stapan> listaStapani = null;
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		UserRegistrationDTO user = new UserRegistrationDTO();
		model.addAttribute("user", user);
		
		if(stapanService.getAllStapani().isEmpty()) {
			redirectAttributes.addFlashAttribute("zeroInregistrari", 1);
			return "stapani";
		}		
		
		//la lansarea aplicatiei prima oara, nu avem stapani
		//asa ca vom crea un stapan, RoSpringVetStapan, altfel ne da eroarea "Page index must not be less than zero!"
		
		if(txtSearch.isEmpty()) {
			//gaseste paginat toti stapanii
			page = stapanService.findPaginated(pageNo, pageSize, sortField, sortDir);
			listaStapani = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaStapani.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = stapanService.findPaginated(pageNo, pageSize, sortField, sortDir);
				listaStapani = page.getContent();
			}
			
		}
		else {
			// gaseste paginat stapanii al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = stapanService.findPaginatedSearch(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
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
				if(fieldSearch.equals("lastname"))
					fieldSearch = "Nume";
				if(fieldSearch.equals("firstname"))
					fieldSearch = "Prenume";
				if(fieldSearch.equals("nrtelefon"))
					fieldSearch = "Nr.telefon";			
				if(fieldSearch.equals("email"))
					fieldSearch = "Email";		
				//redirect pt cautare fara rezultat
				redirectAttributes.addFlashAttribute("cautareText", txtSearch);
				redirectAttributes.addFlashAttribute("cautareCamp", fieldSearch);
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
				
			}
			
			listaStapani = page.getContent();

			// daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			// atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			// altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if (listaStapani.size() == 0) {
				pageNo = page.getTotalPages(); // ultima pagina
				page = stapanService.findPaginatedSearch(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);				
				listaStapani = page.getContent();
			}
			
			
		}

		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());
		
		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);

		// tabel
		model.addAttribute("listaStapani", listaStapani);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		//pentru a adauga un stapan (USER) nou folosind modal-ul
		//punem un nou USER (Un Data Transfer Objec care sa tina datele pentru USER-ul nostru. Folosim clasa noastra UserRegistrationDTO) in model
		//cream un USER gol ca sa ne legam de datele din formularul modalului


		
		//pentru a adauga un animal  nou folosind modal-ul
		//cream un animal gol si il punem in model ca sa ne legam de datele din formularul modalului 
		Animal animal = new Animal();
		model.addAttribute("animal", animal);
				
		return "stapani";

	}
	
	
	@PostMapping("/adaugaAnimalStapan/{id}")
	public String saveAnimalStapan(
			@ModelAttribute("animal") @Valid Animal animal, 
			Errors errors, 
			@ModelAttribute("imageFile") MultipartFile imageFile,
			@PathVariable(value = "id") long id, 
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {

		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		redirectAttributes.addFlashAttribute("idModal", id); 

		//daca nume sau specie sunt goale, mergem inapoi pe pagina animalelor stapanului, si deschidem modalul  cu mesaju de eroare potrivit 
		//mesajele de eroare il iau din clasa Animal, pentru ca validez un obiect
		if (errors.hasFieldErrors("nume")) {	
			sirAtribute.add("errAddAnimal");
			sirAtribute.add("errnume");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		if (animal.getSpecie().isEmpty()) {
			sirAtribute.add("errAddAnimal");
			sirAtribute.add("errspecie");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		//salvare animal - ne trebuie salvat acum pt a lua ID-ul, pt numele pozei 
		animalService.addAnimal(animal);
		
		//daca userul a selectat o imagine - adica daca variabila imageFile nu este goala, atunci setam acea imagine
		//salvare poza animal in folder
		//parametrul de tip MultipartFile este poza, iar parametrul animalId ne trebuie pentru a seta poza animalului corespunzator		
		if(! imageFile.isEmpty()) {
			try {	
				//poza se salveaza in folder accesand metoda salvarePoza, si in acelas timp luam si numele pozei salvate, pt a o seta animalului in BD
				Stapan stapan = animal.getStapanId();
				String nume_poza = salvarePoza(stapan, animal, imageFile);
				//setare nume poza pt animal in baza de date
				animal.setImagine(nume_poza);	
			}catch(Exception e) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.warn("EROARE: " + e);
				sirAtribute.add("errAddAnimal");
				sirAtribute.add("errpoza");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}
		
		//salvare animal editat cu poza
		animalService.addAnimal(animal);
		
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes); //redirect inapoi 
		
	}
	
	
		
	@GetMapping("/veziAnimaleStapan/{id}")
	private String actiuniAnimalStapani(Model model, @PathVariable(value = "id") long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return findPaginatedAnimaleStapan(1, id, "nume", "asc", "", "", request, redirectAttributes, model); // returneaza prima pagina
	}

	@GetMapping("/veziAnimaleStapan/{id}/page/{pageNo}")
	public String findPaginatedAnimaleStapan(
			@PathVariable(value = "pageNo") int pageNo,
			@PathVariable(value = "id") long id,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model) {
				
		
		//stapan
		Stapan stapan = stapanService.getStapan(id).get();
		model.addAttribute("stapan", stapan);
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		int pageSize = 5;
	
		Page<Animal> page = null;
		List<Animal> listaAnimale = null;
		
		////FUNCTIE SEARCH ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		////FUNCTIE SEARCH ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if(txtSearch.isEmpty()) {
			//gaseste paginat toate animalele stapanului
			page = animalService.findPaginatedAnimalStapan(id, pageNo, pageSize, sortField, sortDir);
			listaAnimale = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaAnimale.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = animalService.findPaginatedAnimalStapan(id, pageNo, pageSize, sortField, sortDir);
				listaAnimale = page.getContent();
			}
			
		}
		else {
			// gaseste paginat stapanii al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = animalService.findPaginatedSearchAnimalStapan(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch, stapan.getStapanId());
			
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
				if(fieldSearch.equals("lastname"))
					fieldSearch = "Nume";
				if(fieldSearch.equals("firstname"))
					fieldSearch = "Prenume";
				if(fieldSearch.equals("nrtelefon"))
					fieldSearch = "Nr.telefon";			
				if(fieldSearch.equals("email"))
					fieldSearch = "Email";		

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

		
		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);

		// tabel
		model.addAttribute("listaAnimale", listaAnimale);
		
		// adaugam numele stapanului in model
		String nume_stapan = stapan.getFirstname() + " " + stapan.getLastname();
		model.addAttribute("nume_stapan", nume_stapan);
		
		//id stapan
		model.addAttribute("id_stapan", id);
		
		//nume view - pt redirectionare fie la veziAnimale fie la veziAnimaleStapan (in cazu asta e veziAnimaleStapan)
		model.addAttribute("numeView", "veziAnimaleStapan");

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		//pentru a adauga un animal nou folosind modal-ul
		//punem un nou animal in model
		//cream un animal gol ca sa ne legam de datele din formularul modalului

		Animal animal = new Animal();
		model.addAttribute("animal", animal);

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
		
		
		//Atribut pt calea catre pozele animalelor
		//Il punem in metoda @ModelAttribute
		
		return "stapan-animale-lista";
		
	}	
	
	@PostMapping("/editareStapan")
	public String editStapan(
			@ModelAttribute("stapan") @Valid Stapan stapan_nou,
			Errors errors, 
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			@ModelAttribute("imageFileStapan") MultipartFile imageFileStapan,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model)  {

		//ID-ul noului stapan nu se schimba, este acela cu al vechiului angajat. 
		//Luam informatiile vechiului angajat, ne folosim de ele si dupa le setam pe cele noi.
		Stapan stapan_vechi = stapanService.getStapan(stapan_nou.getStapanId()).get(); 
		String email_vechi = stapan_vechi.getEmail();
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		sirAtribute.add("erredit");
		
		//ID-ul modalului ce va aparea pe ecran, la fel cu al angajatului
		long idModal = stapan_nou.getStapanId();
		redirectAttributes.addFlashAttribute("idModal", idModal);
		
		//Validam obiectul "stapan" primit, pentru nume, prenume, campul lui de "email" etc.
		//Vom transmite ca parametru pentru URL "email" daca exista erori pentru campul de "email". Daca acest parametru exista (verificam asta utilizand Thymeleaf),
		//in modal-ul nostru va aparea un mesaj de eroare corespunzator pentru a atrage atentia utilizatorului ca email-ul trimis nu este valid.			
		//Id-ul modalului, directia de sortare, campul dupa care se sorteaza, pagina,vor fi trimise ca parametru si citite utilizand Thymeleaf.
		if(errors.hasErrors()) {
			
			if(errors.getFieldError("firstname") != null) 
				sirAtribute.add("prenume");
			if(errors.getFieldError("lastname") != null) 
				sirAtribute.add("nume");
			if(errors.getFieldError("email") != null) 
				sirAtribute.add("email");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			
		}
		//Am obervat ca pentru constrangerea "unique" nu o sa se inregistreze erori pentru obiectul "angajat",
		//deci acest if l-am scos din if-ul care verifica daca sunt erori in prima instanta 
		//email deja existent (care sa fie diferit de al vechiului angajat)
		//daca nu e asa, o sa fie informat utilizatorul print-un mesaj de eroare "Email deja existent"
		if(!stapan_nou.getEmail().equals(email_vechi) && stapanService.getStapanByEmail(stapan_nou.getEmail()) != null) {
			sirAtribute.add("emailExistent");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);

		}
		//Email-ul trebuie sa fie mai mic de 32 de caractere, ca asa am pus in constrangerea Unique
		if(stapan_nou.getEmail().length() > 32) {
			sirAtribute.add("emailLung");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		//dupa ce se verifica cu erorile pt prenume, nume, mail
		
		//setam parola vechiului stapan, ca asta nu s-a schimbat si nu am luat-o in form cu hidden field cum am luat ID-ul,
		//altfel primim un mesaj de eroare care ne spune ca parola nu poate fi nula.
		stapan_nou.setParola(stapan_vechi.getParola());
		
		//daca poza nu a fost modificata
		//incercam sa salvam stapan_nou daca nu are nicio eroare (nu vad ce ar putea sa aiba)
		if(imageFileStapan.isEmpty()) {
			try {
				stapan_nou.setImagine(stapan_vechi.getImagine());
				stapanService.updateStapan(stapan_nou);
			}catch(Exception e) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.error("EROARE: " + e.toString());
				sirAtribute.add("poza");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}
		
		//daca poza s-a modificat
		//incercam sa salvam poza si noul angajat
		if(! imageFileStapan.isEmpty()) {
			String nume_poza = "";
			try {	
				//poza se salveaza in folder accesand metoda salvarePoza
				//UserRegistrationDto nume_poza = salvarePoza(registrationDto, imageFilestapan);				
				//se seteaza cu regDTO, in UserService se pune si in BD in campul imagine string-ul cu numele pozei ca sa il accesam cu thymeleaf
				nume_poza = salvarePozaStapan(stapan_nou, imageFileStapan);
				//setam string-ul numelui pozei salvate in folder in metoda
				stapan_nou.setImagine(nume_poza);
				//update la obiect cu ajutorul serviciului
				stapanService.updateStapan(stapan_nou);
			}catch(Exception e) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.warn("EROARE: " + e);
				sirAtribute.add("poza");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}
		
		//si useru cu acelas email vechi sa se ia si sa se seteze cu nou mail, nume, prenume, parola
		User user = userRepository.findByEmail(email_vechi);
		user.setFirstname(stapan_nou.getFirstname());
		user.setLastname(stapan_nou.getLastname());
		user.setEmail(stapan_nou.getEmail());
		user.setPassword(stapan_nou.getParola());
		userService.updateUser(user);
	
		//succes
		sirAtribute.add("successEdit");
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		
	}
	
	
	@GetMapping("/stergereStapan")
	public String stergereStapan(
			@RequestParam("stapanId") long stapanId,
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			Model model) {
		
		
		String email = stapanService.getStapan(stapanId).get().getEmail();
		User user = userRepository.findByEmail(email);
		stapanService.deleteStapan(stapanId);
		userRepository.delete(user);
		
		return "redirect:/veziStapani/page/" + currentPage + "?sortField=" + sortField + "&sortDir=" + sortDir + "&txtSearch=" + txtSearch + "&fieldSearch=" + fieldSearch;
	}

	
	@PostMapping("/adaugareStapan")
	public String adaugareStapan(
			@ModelAttribute("user") @Valid UserRegistrationDTO registrationDto,
			Errors errors,
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			@ModelAttribute("imageFileStapan") MultipartFile imageFileStapan,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes
			) {
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		sirAtribute.add("erradd");
		
		// registrationDto este obiectul pe care il "POSTeaza" utilizatorul cand se
		// inregistreaza, dupa ce da submit la formular, de asta il validam		
		if (errors.hasErrors()) { // daca nu e format valid, daca este gol, etc
			if(errors.getFieldError("firstname") != null) 
				sirAtribute.add("errprenume");
			if(errors.getFieldError("lastname") != null) 
				sirAtribute.add("errnume");
			if(errors.getFieldError("email") != null) 
				sirAtribute.add("errmail1");
			if(errors.getFieldError("password") != null) 
				sirAtribute.add("errparola");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		// pentru ca n-am cum sa verific in DTO daca exista deja, folosesc repozitoriul
		// (daca as fi vrut asta ar fi trebuit ca in loc de DTO sa iau User ca obiect al formularului)
		// pentru DTO se fac celelalte verificari pentru ca le pot face (@Email - sa fie format valid, @NotNull, @Size, etc) - si sunt capturate de .hasErrors,
		// cu Thymeleaf le pun in View
				
		User user = userRepository.findByEmail(registrationDto.getEmail()); 
		//daca este null inseamna ca nu exista deja, altfel da eroare ca deja exista.
		//Utilizam adnotarea @Unique pentru campul de "email" in entitatea noastra de User, pentru a nu putea introduce mai multe mail-uri la fel in baza de date. 
		if (user == null && registrationDto.getEmail().length() < 32) {	
			//salvam userul daca mail-ul nu exista deja si nu este mai mare de 32 de caractere 
			userService.saveUser(registrationDto, "ROL_STAPAN");
			//salvare stapan in db
			userService.saveStapan(registrationDto, null, null, null);
			//preluare obiect angajat dupa email-ul regDto, ne folosim de el ca sa luam ID pt salvare poza in folder
			Stapan stapan = stapanService.getStapanByEmail(registrationDto.getEmail());
			
			if(! imageFileStapan.isEmpty()) {
				String nume_poza = "";
				try {	
					//poza se salveaza in folder accesand metoda salvarePoza
					//UserRegistrationDto nume_poza = salvarePoza(registrationDto, imageFileStapan);				
					//se seteaza cu regDTO, in UserService se pune si in BD in campul imagine string-ul cu numele pozei ca sa il accesam cu thymeleaf
					nume_poza = salvarePozaStapan(stapan, imageFileStapan);
					//setam string-ul numelui pozei salvate in folder in metoda
					stapan.setImagine(nume_poza);
					//update la obiect cu ajutorul serviciului
					stapanService.updateStapan(stapan);
				}catch(Exception e) {
					log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					log.warn("EROARE POZA: " + e);
					sirAtribute.add("errpoza");
				}
			}
						
			//trimitere notificare catre noul utilizator inregistrat
			User userDupaSalvare = userRepository.findByEmail(registrationDto.getEmail());
			try {
				emailService.sendEmailInregistrare(userDupaSalvare); 
			}catch(MailException ex) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.warn("EROARE MAIL: " + ex);
			} 
			
			sirAtribute.add("success");
		} 
		else { //daca mail exista deja sau daca este mai mare de 32 caractere
			sirAtribute.add("errmail2");
		}
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		
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
		
	
	
	/////////////////////////Pentru animalele stapanilor
	//Metoda salveaza poza cu numele pozei pe care il calculeaza, si returneaza inapoi doar numele pozei, ca sa fie salvata si in BD pt animal 
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
	
	
/////////////////////////Pentru stapani
	//salvare poza stapan in folder
	//Metoda salveaza poza cu numele pozei pe care il calculeaza, returneaza si numele pozei salvate, o salvam dupa ce se creaza stapan, facem update
	//numele pozei pt stapan se seteaza dupa ce se salveaza stapanul, apoi se face update cu serviciul
	public String salvarePozaStapan(Stapan stapan, MultipartFile imageFile) throws IOException{
		String nume_poza = "";
		//setare nume poza cu "stapan_" + IDstapan
		String extensie = imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf("."));
		nume_poza = "stapan_" + stapan.getStapanId() + extensie ;
		//salvare poza in folderul poze_stapani
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		byte[] bytes = imageFile.getBytes();
		Path final_image_path = Paths.get(path + File.separator + "poze_stapani" + File.separator + nume_poza);
		Files.write(final_image_path, bytes);
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		return nume_poza;
	}
	
	
	
	
}