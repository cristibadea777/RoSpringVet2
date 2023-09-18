package eu.badeacristian.RoSpringVet.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import eu.badeacristian.RoSpringVet.models.Role;
import eu.badeacristian.RoSpringVet.models.User;
import eu.badeacristian.RoSpringVet.models.UserRegistrationDTO;
import eu.badeacristian.RoSpringVet.repositories.UserRepository;
import eu.badeacristian.RoSpringVet.services.AngajatService;
import eu.badeacristian.RoSpringVet.services.EmailService;
import eu.badeacristian.RoSpringVet.services.UserService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j //simple logging facade

public class AngajatController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AngajatService angajatService;
	
	@Autowired
	private EmailService emailService;

	@Value("${external.resources.path}")
    private String path; 
	

	private Angajat getAngajat() {
		
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
		
		Angajat angajat = angajatService.getAngajatByEmail(username);
		
		return angajat;
	}
	
	
	
	@GetMapping("/angajati")
	private String actiuniAngajati(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return findPaginatedAngajat(1, "firstname", "asc", "", "", model, request, redirectAttributes); // returneaza prima pagina
	}
	
	@ModelAttribute("angajat")
	public void modelAngajat(Model model) {
		Angajat angajat = new Angajat();
		model.addAttribute("angajat", angajat);
	}

	@GetMapping("/veziAngajati/page/{pageNo}")
	public String findPaginatedAngajat(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			Model model,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) 
	{
		int pageSize = 5;
		Page<Angajat> page = null;
		List<Angajat> listaAngajati = null;
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		if(txtSearch.isEmpty()) {
			//gaseste paginat toti angajatii
			page = angajatService.findPaginated(pageNo, pageSize, sortField, sortDir);
			listaAngajati = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaAngajati.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = angajatService.findPaginated(pageNo, pageSize, sortField, sortDir);
				listaAngajati = page.getContent();
			}
		}
		else {
			// gaseste paginat toti angajatii al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = angajatService.findPaginatedSearch(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
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
			//atunci mergem inapoi pe pagina angajatilor si informam utilizatorul deschizand un modal
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
			
			listaAngajati = page.getContent();
		}
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
				
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaAngajati", listaAngajati);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		UserRegistrationDTO registrationDto = new UserRegistrationDTO();
		model.addAttribute("user", registrationDto);

		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "angajati";

	}

	
	
	////PLECATI
	@GetMapping("/veziPlecati")
	private String actiuniAngajatiPlecati(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return findPaginatedAngajatPlecati(1, "firstname", "asc", "", "", model, request, redirectAttributes); // returneaza prima pagina
	}
	
	
	@GetMapping("/veziPlecati/page/{pageNo}")
	public String findPaginatedAngajatPlecati(
			@PathVariable(value = "pageNo") int pageNo,
			@RequestParam("sortField") String sortField, 
			@RequestParam("sortDir") String sortDir, 
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			Model model,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) 
	{
		int pageSize = 5;
		Page<Angajat> page = null;
		List<Angajat> listaAngajati = null;
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		//~~~~~~~~~~~~~~~~~~~~~~FUNCTIA DE SEARCH
		if(txtSearch.isEmpty()) {
			//gaseste paginat toti angajatii
			page = angajatService.findPaginatedPlecati(pageNo, pageSize, sortField, sortDir);
			if(page.getTotalPages() <= 0) {
				redirectAttributes.addFlashAttribute("zeroInregistrari", 1);
				return "redirect:/angajati";		  
			}
			listaAngajati = page.getContent();
			
			//daca pagina ramane fara inregistrari (toate sunt sterse) sau in URL se introduce o pagina inexistenta
			//atunci luam ultima pagina (page.getTotalPages()) si populam tabelul
			//altfel utilizatorul se impotmoleste, va vedea un tabel gol fara sa poata naviga inapoi
			if(listaAngajati.size() == 0) {
				pageNo = page.getTotalPages(); //ultima pagina
				page = angajatService.findPaginatedPlecati(pageNo, pageSize, sortField, sortDir);
				if(page.getTotalPages() <= 0) {
					redirectAttributes.addFlashAttribute("zeroInregistrari");
					return "redirect:/angajati";
				}
				listaAngajati = page.getContent();
			}
		}
		else {
			// gaseste paginat toti angajatii al carui <<fieldSearch>> incep cu <<txtSearch>>
			page = angajatService.findPaginatedSearchPlecati(pageNo, pageSize, sortField, sortDir, fieldSearch, txtSearch);
			
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
			//atunci mergem inapoi pe pagina angajatilor si informam utilizatorul deschizand un modal
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
			
			listaAngajati = page.getContent();
		}
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
		//END FUNCTIA DE SEARCH~~~~~~~~~~~~~~~~~~~~~~
				
		// atribute paginare
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// tabel
		model.addAttribute("listaAngajati", listaAngajati);

		// atribute sortare
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		
		UserRegistrationDTO registrationDto = new UserRegistrationDTO();
		model.addAttribute("user", registrationDto);

		//atribute cautare
		model.addAttribute("txtSearch", txtSearch);
		model.addAttribute("fieldSearch", fieldSearch);
		
		return "angajati-plecati";

	}
	
	@GetMapping("/reangajare")
	public String reangajareAngajat(
			@RequestParam("angajatId") long id,
			Model model) {
		
		Angajat angajat = angajatService.getAngajat(id).get();
		
		angajat.setFunctie(" ");
		
		angajatService.updateAngajat(angajat);
		
		return "redirect:/angajati";
	}
	
	////PLECATI
	

	@PostMapping("/salveazaAngajat")
	public String saveAngajat(
			@ModelAttribute("user") @Valid UserRegistrationDTO registrationDto,
			Errors errors,
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			@RequestParam("functie") String functie,
			@ModelAttribute("imageFileAngajat") MultipartFile imageFileAngajat,
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
			userService.saveUser(registrationDto, "ROL_ANGAJAT");
			//salvare angajat in db
			userService.saveAngajat(registrationDto, functie, null);
			//preluare obiect angajat dupa email-ul regDto, ne folosim de el ca sa luam ID pt salvare poza in folder
			Angajat angajat = angajatService.getAngajatByEmail(registrationDto.getEmail());
			
			if(! imageFileAngajat.isEmpty()) {
				String nume_poza = "";
				try {	
					//poza se salveaza in folder accesand metoda salvarePoza
					//UserRegistrationDto nume_poza = salvarePoza(registrationDto, imageFileAngajat);				
					//se seteaza cu regDTO, in UserService se pune si in BD in campul imagine string-ul cu numele pozei ca sa il accesam cu thymeleaf
					nume_poza = salvarePozaAngajat(angajat, imageFileAngajat);
					//setam string-ul numelui pozei salvate in folder in metoda
					angajat.setImagine(nume_poza);
					//update la obiect cu ajutorul serviciului
					angajatService.updateAngajat(angajat);
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
	

	@GetMapping("/stergere")
	public String deleteAngajat(
			@RequestParam("angajatId") long id, 
			HttpServletRequest request,
			RedirectAttributes redirectAttributes, 
			Model model) {
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		
		Angajat angajat = angajatService.getAngajat(id).get();
		
		if(angajat.getEmail().equals(getAngajat().getEmail())) {
			sirAtribute.add("imposibil_de_sters");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		// delete angajat -- se pune functia angajatului ca fiind 'plecat'
		angajat.setFunctie("PLECAT");
		angajatService.updateAngajat(angajat);
		
		return "redirect:/angajati";
	}
	
	
	@PostMapping("/editareAngajat")
	public String editAngajat(
			@ModelAttribute("angajat") @Valid Angajat angajat_nou,
			Errors errors, 
			@RequestParam("currentPage") String currentPage,
			@RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir,
			@RequestParam("txtSearch") String txtSearch,
			@RequestParam("fieldSearch") String fieldSearch,
			@ModelAttribute("imageFileAngajat") MultipartFile imageFileAngajat,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes,
			Model model)  {

		//ID-ul noului stapan nu se schimba, este acela cu al vechiului angajat. 
		//Luam informatiile vechiului angajat, ne folosim de ele si dupa le setam pe cele noi.
		Angajat angajat_vechi = angajatService.getAngajat(angajat_nou.getAngajatId()).get(); 
		String email_vechi = angajat_vechi.getEmail();
		
		ArrayList<String> sirAtribute = new ArrayList<String>();
		sirAtribute.add("erredit");
		
		//ID-ul modalului ce va aparea pe ecran, la fel cu al angajatului
		long idModal = angajat_nou.getAngajatId();
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
		if(!angajat_nou.getEmail().equals(email_vechi) && angajatService.getAngajatByEmail(angajat_nou.getEmail()) != null) {
			sirAtribute.add("emailExistent");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);

		}
		//Email-ul trebuie sa fie mai mic de 32 de caractere, ca asa am pus in constrangerea Unique
		if(angajat_nou.getEmail().length() > 32) {
			sirAtribute.add("emailLung");
			return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		}
		
		//dupa ce se verifica cu erorile pt prenume, nume, mail
		
		//setam parola vechiului stapan, ca asta nu s-a schimbat si nu am luat-o in form cu hidden field cum am luat ID-ul,
		//altfel primim un mesaj de eroare care ne spune ca parola nu poate fi nula.
		angajat_nou.setParola(angajat_vechi.getParola());
		
		//daca poza nu a fost modificata
		//incercam sa salvam angajat_nou daca nu are nicio eroare (nu vad ce ar putea sa aiba)
		if(imageFileAngajat.isEmpty()) {
			try {
				angajat_nou.setImagine(angajat_vechi.getImagine());
				angajatService.updateAngajat(angajat_nou);
			}catch(Exception e) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.error("EROARE: " + e.toString());
				sirAtribute.add("poza");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}
		
		//daca poza s-a modificat
		//incercam sa salvam poza si noul angajat
		if(! imageFileAngajat.isEmpty()) {
			String nume_poza = "";
			try {	
				//poza se salveaza in folder accesand metoda salvarePoza
				//UserRegistrationDto nume_poza = salvarePoza(registrationDto, imageFileAngajat);				
				//se seteaza cu regDTO, in UserService se pune si in BD in campul imagine string-ul cu numele pozei ca sa il accesam cu thymeleaf
				nume_poza = salvarePozaAngajat(angajat_nou, imageFileAngajat);
				//setam string-ul numelui pozei salvate in folder in metoda
				angajat_nou.setImagine(nume_poza);
				//update la obiect cu ajutorul serviciului
				angajatService.updateAngajat(angajat_nou);
			}catch(Exception e) {
				log.warn("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				log.warn("EROARE: " + e);
				sirAtribute.add("poza");
				return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
			}
		}
		
		//si useru cu acelas email vechi sa se ia si sa se seteze cu nou mail, nume, prenume, parola
		User user = userRepository.findByEmail(email_vechi);
		user.setFirstname(angajat_nou.getFirstname());
		user.setLastname(angajat_nou.getLastname());
		user.setEmail(angajat_nou.getEmail());
		user.setPassword(angajat_nou.getParola());
		userService.updateUser(user);
	
		//succes
		sirAtribute.add("successEdit");
		return redirectCuAtributeErori(sirAtribute, request, redirectAttributes);
		
	}
	
	
	//salvare poza angajat in folder
	//Metoda salveaza poza cu numele pozei pe care il calculeaza, returneaza si numele pozei salvate, o salvam dupa ce se creaza angajat, facem update
	//numele pozei pt angajat se seteaza dupa ce se salveaza angajatul, apoi se face update cu serviciul
	public String salvarePozaAngajat(Angajat angajat, MultipartFile imageFile) throws IOException{
		String nume_poza = "";
		//setare nume poza cu "angajat_" + IDangajat
		String extensie = imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf("."));
		nume_poza = "angajat_" + angajat.getAngajatId() + extensie ;
		//salvare poza in folderul poze_angajati
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		byte[] bytes = imageFile.getBytes();
		Path final_image_path = Paths.get(path + File.separator + "poze_angajati" + File.separator + nume_poza);
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
