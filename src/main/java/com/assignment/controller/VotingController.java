package com.assignment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.assignment.model.LoggedUser;
import com.assignment.model.User;
import com.assignment.model.VoteCount;
import com.assignment.model.Voting;
import com.assignment.service.VotingService;



@Controller
public class VotingController {
	@Autowired
	private VotingService votingService;
	
	@GetMapping(value = {"/" , "/login"})
	public String home(Model model) {
		model.addAttribute("loggeduser",new LoggedUser());
		return "login";										
	}
	
	
	@GetMapping("/adminLogin")
	public String adminLogin(Model model) {
		model.addAttribute("loggeduser",new LoggedUser());
		return "adminLogin";	
	}
	
	
	@GetMapping("/logout")
	public String logout(@ModelAttribute LoggedUser loggedUser, Model model) {
		this.votingService.logout(loggedUser);
		return "redirect:/";
	}
	
	@GetMapping("/delete")
		public String deleteVote(@RequestParam Iterable<? extends Voting> candidateName) {
		this.votingService.deleteVote(candidateName);
		return "adminpage";
		
	}
	

	@PostMapping("/login")
	public String login(@ModelAttribute LoggedUser loggedUser, Model model) {
		if(loggedUser.getUsername().equals("admin") 
				&& loggedUser.getPassword().equals("admin"))
		{
			long count = votingService.getTotalUsers();
			long totalVotes = votingService.getTotalVotes();
			List<VoteCount> votes = this.votingService.findAllVote();
			model.addAttribute("votes",votes);
			model.addAttribute("usersCount" , count);
			model.addAttribute("totalVotes" , totalVotes);
			return "adminpage";
		}
		else {
			User foundUser = this.votingService.login(loggedUser);		
			if(foundUser == null) {
				model.addAttribute("error","Username or Password is invalid");
				model.addAttribute("loggeduser",new LoggedUser());
				return "login";
			}
			else {
				Voting voter = new Voting();
				String voterName = foundUser.getUsername();
		     	voter.setUserId(foundUser.getId()); 
				model.addAttribute("voter",voter);
				model.addAttribute("voterName" ,voterName);
				return "votingpage";
			}
		}
	}
	
	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("user",new User());
		return "register";
	}
	
	@PostMapping("/register")
	public String addUser(@ModelAttribute User user, Model model) {
		boolean result = this.votingService.addUser(user);
		if(result) {
			model.addAttribute("RegisterScucess"," Dear " + user.getUsername()+" Your Registration is Successfull !");
			return "login";
		 }
		else {
			model.addAttribute("error","User Already Exists");
			
			model.addAttribute("user",new User());
			return "register";
		}
	}
	
	@PostMapping("/voted/{userId}")
	public String voted(@ModelAttribute User user, Voting voting, Model model, @PathVariable Integer userId) {
		Voting findVote = this.votingService.findByUserId(userId);
		  
		if(findVote != null) {	
			model.addAttribute("loggeduser",new LoggedUser());
			model.addAttribute("error","You have alreday voted once to " + findVote.getCandidateName());
			Voting voter = new Voting();
	     	voter.setUserId(userId); 
			model.addAttribute("voter",voter);
			return "votingpage";			
		}
		else {
			this.votingService.addVote(voting);		
			model.addAttribute("errorSucess","You have  Voted Successfully to " + voting.getCandidateName());
			Voting voter = new Voting();
	     	voter.setUserId(userId); 
			model.addAttribute("voter",voter);

			return "votingpage";
		}
	}

}
