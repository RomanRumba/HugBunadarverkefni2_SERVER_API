package project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import project.persistance.entities.Chatroom;
import project.persistance.entities.User;
import project.services.ChatroomService;
import project.services.CryptographyService;
import project.services.TagService;
import project.services.UserService;

/**
 * Controller for test purposes; creating dummy data. 
 * This ensures we are always working with the same data
 * when testing and debugging. Also ensures that all testers
 *  have as similar environments as possible.
 * 
 * @author Vilhelml
 */
@RestController
public class TestController {

	@Autowired
	private ChatroomService chatroomService;

	@Autowired
	private UserService userService;

	@Autowired
	private TagService tagservice;

	/**
	 * Temporary test method to fill the database with test data
	 * 
	 * @deprecated
	 */
	@RequestMapping(value = "/createdata", method = RequestMethod.GET, headers = "Accept=application/json")
	public void createMockUserRelations() {
		BCryptPasswordEncoder privateInfoEncoder = new BCryptPasswordEncoder();

		try {
			User vilhelm = new User("vilhelml", privateInfoEncoder.encode("Test$1234"), "Vilhelm",
					CryptographyService.getCiphertext("fskdnfsf@fdsfds.com"));
			this.userService.createUser(vilhelm);
			User roman = new User("ror9", privateInfoEncoder.encode("Test$1234"), "Roman",
					CryptographyService.getCiphertext("fskdnfsf@fdsfds.com"));
			this.userService.createUser(roman);
			User david = new User("dah38", privateInfoEncoder.encode("Test$1234"), "Davíð",
					CryptographyService.getCiphertext("fskdnfsf@fdsfds.com"));
			this.userService.createUser(david);

			vilhelm = this.userService.findByUsername("vilhelml");
			roman = this.userService.findByUsername("ror9");
			david = this.userService.findByUsername("dah38");

			this.userService.addFriend(vilhelm, roman);
			this.userService.addFriend(roman, vilhelm);
			this.userService.addFriend(vilhelm, david);
			this.userService.addFriend(david, roman);

			Chatroom c1 = new Chatroom("c1", "disp1", "desc1", true, true);
			c1 = this.chatroomService.createChatroom(vilhelm, c1);
			Chatroom c2 = new Chatroom("c2", "disp2", "desc2", true, true);
			c2 = this.chatroomService.createChatroom(roman, c2);
			Chatroom c3 = new Chatroom("c3", "disp3", "desc3", false, true);
			c3 = this.chatroomService.createChatroom(david, c3);
			Chatroom c4 = new Chatroom("c4", "disp4", "desc4", true, false);
			c4 = this.chatroomService.createChatroom(roman, c4);
			Chatroom c5 = new Chatroom("c5", "disp5", "desc5", true, true);
			c5 = this.chatroomService.createChatroom(roman, c5);
			Chatroom c6 = new Chatroom("c6", "disp6", "desc6", true, true);
			c6 = this.chatroomService.createChatroom(david, c6);

			this.tagservice.addTagtoChatroom(c1, "music");
			this.tagservice.addTagtoChatroom(c2, "music");
			this.tagservice.addTagtoChatroom(c1, "jazz");
			this.tagservice.addTagtoChatroom(c4, "music");
			this.tagservice.addTagtoChatroom(c5, "rock");

			this.chatroomService.joinChatroom(vilhelm, c4);
			this.chatroomService.sendMemberInvitation(vilhelm, c3);
			this.chatroomService.sendAdminInvitation(vilhelm, c5);
			this.chatroomService.sendAdminInvitation(vilhelm, c6);

			this.chatroomService.joinChatroom(vilhelm, c5);
			this.chatroomService.acceptAdminInvite(vilhelm, c6);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
