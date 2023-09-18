package eu.badeacristian.RoSpringVet.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.badeacristian.RoSpringVet.models.Angajat;
import eu.badeacristian.RoSpringVet.models.Programare;
import eu.badeacristian.RoSpringVet.models.Stapan;
import eu.badeacristian.RoSpringVet.models.UserRegistrationDTO;
import eu.badeacristian.RoSpringVet.repositories.UserRepository;
import eu.badeacristian.RoSpringVet.services.AngajatService;
import eu.badeacristian.RoSpringVet.services.AnimalService;
import eu.badeacristian.RoSpringVet.services.ProgramareService;
import eu.badeacristian.RoSpringVet.services.StapanService;
import eu.badeacristian.RoSpringVet.services.TratamentService;

@Controller
public class MainController {
	
	@Autowired
	StapanService stapanService;
	@Autowired
	AngajatService angajatService;
	@Autowired
	AnimalService animalService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ProgramareService programareService;
	@Autowired
	TratamentService tratamentService;
	
	@GetMapping("/login")
	public String login(Model model) {
		//pentru a adauga un stapan (USER) nou folosind modal-ul
		//punem un nou USER (Un Data Transfer Objec care sa tina datele pentru USER-ul nostru. Folosim clasa noastra UserRegistrationDTO) in model
		//cream un USER gol ca sa ne legam de datele din formularul modalului
		UserRegistrationDTO user = new UserRegistrationDTO();
		model.addAttribute("user", user);
		return "login";
	}
	
	
	@GetMapping("/")
	public ModelAndView home(Model model) {
		String username = "wut";
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
		  username = ((UserDetails)principal).getUsername();
		} else {
		  username = principal.toString();
		}		
		
		//model and view
		ModelAndView modelAndView = new ModelAndView();
		
		Stapan stapan = stapanService.getStapanByEmail(username);

		if(stapan != null) {
			modelAndView.addObject("stapan", stapan);
			modelAndView.addObject("nume_stapan", stapan.getFirstname() + ' ' + stapan.getLastname());
			
			//programari azi stapan
			String dataAzi    = LocalDate.now().toString();
			int programariAzi = programareService.getAllProgramariDataStapan(dataAzi, stapan.getStapanId()).size();
			modelAndView.addObject("programariAzi", programariAzi);
			//programari maine stapan
			String dataMaine  = LocalDate.now().plusDays(1).toString();
			int programariMaine = programareService.getAllProgramariDataStapan(dataMaine, stapan.getStapanId()).size();
			modelAndView.addObject("programariMaine", programariMaine);
			
			//tratamente active
			int tratamenteActive = tratamentService.getTotalTratamenteStapan(stapan.getStapanId());
			modelAndView.addObject("tratamenteActive", tratamenteActive);
			
			modelAndView.setViewName("dashboard-stapani");
		}
		else {
			Angajat angajat = angajatService.getAngajatByEmail(username);
			if(angajat != null) {
				modelAndView.addObject("angajat", angajat);
				modelAndView.addObject("nume_angajat", angajat.getFirstname() + ' ' + angajat.getLastname());
				String dataAzi    = LocalDate.now().toString();
				String dataMaine  = LocalDate.now().plusDays(1).toString();
				int programariAzi = programareService.getAllProgramariData(dataAzi).size();
				int programariMaine = programareService.getAllProgramariData(dataMaine).size();
				int totalAngajati = angajatService.getAllAngajati().size();
				int totalAnimale  = animalService.getAllAnimale().size();
				int totalStapani  = stapanService.getAllStapani().size();
				modelAndView.addObject("programariAzi", programariAzi);
				modelAndView.addObject("programariMaine", programariMaine);
				modelAndView.addObject("totalAngajati", totalAngajati);
				modelAndView.addObject("totalAnimale", totalAnimale);
				modelAndView.addObject("totalStapani", totalStapani);
				modelAndView.setViewName("dashboard-angajati");
			}
			else {
				UserRegistrationDTO user = new UserRegistrationDTO();
				model.addAttribute("user", user);
				modelAndView.setViewName("login");
			}
		}
		
		
		return modelAndView;
		
	}
	
	@GetMapping("/desprenoi")
	public ModelAndView about(Model model) {
		//model and view
		ModelAndView modelAndView = new ModelAndView();
		//angajati
		List<Angajat> listaAngajati = angajatService.getAllAngajati();
		modelAndView.addObject("listaAngajati", listaAngajati);
		
		modelAndView.setViewName("despre-noi");
		
		return modelAndView;
		
	}
	
	
	
	
	
	
}
