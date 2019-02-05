package project.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.errors.BadRequestException;
import project.errors.NotFoundException;
import project.persistance.entities.Chatroom;
import project.persistance.entities.Tag;
import project.persistance.repositories.ChatroomRepository;
import project.persistance.repositories.TagRepository;

@Service
public class TagService {

	@Autowired
	private ChatroomRepository chatroomRepository;

	@Autowired
	private TagRepository tagRepository;

	/**
	 * remove the old tags and add new ones
	 * 
	 * @param chatroom
	 * @param tagNames
	 */
	@Transactional(readOnly = false)
	public void setTags(Chatroom chatroom, List<String> tagNames) {
		// remove the old tags
		this.removeAllTagsFromChatroom(chatroom);

		for (String tagName : tagNames) {
			// fetch the tag
			Tag tag = this.getTag(tagName);

			List<Tag> tags = chatroom.getTags();
			List<Chatroom> chatrooms = tag.getChatroomsWithTag();
			// add the tag to the chatroom
			tags.add(tag);
			chatrooms.add(chatroom);
		}
		// save the changes
		this.chatroomRepository.save(chatroom);
	}

	/**
	 * add a single tag to a chatroom
	 * 
	 * @param chatroom
	 * @param tagName
	 */
	@Transactional(readOnly = false)
	public void addTagtoChatroom(Chatroom chatroom, String tagName) throws BadRequestException {
		if (this.chatroomHasTag(chatroom, tagName)) {
			throw new BadRequestException("Chatroom already has this tag");
		}
		// fetch the tag
		Tag tag = this.getTag(tagName);

		List<Tag> tags = chatroom.getTags();
		List<Chatroom> chatrooms = tag.getChatroomsWithTag();

		// create the relation
		tags.add(tag);
		chatrooms.add(chatroom);

		this.chatroomRepository.save(chatroom);
	}

	/**
	 * Remove the tag from the chatroom
	 * 
	 * @param chatroom
	 * @param tagName
	 */
	@Transactional(readOnly = false)
	public void removeTagFromChatroom(Chatroom chatroom, String tagName) throws NotFoundException {
		if (this.chatroomHasTag(chatroom, tagName)) {
			throw new NotFoundException("Chatroom does not have this tag");
		}
		// fetch the tag
		Tag tag = this.getTag(tagName);

		List<Tag> tags = chatroom.getTags();
		List<Chatroom> chatrooms = tag.getChatroomsWithTag();
		// delete the relationship
		tags.remove(tag);
		chatrooms.remove(chatroom);
		// saving the changes
		this.chatroomRepository.save(chatroom);
	}

	/**
	 * check if the chatroom has the given tag
	 * 
	 * @param chatroom
	 * @param tagName
	 * @return true if the chatroom has the tag, else false
	 */
	public Boolean chatroomHasTag(Chatroom chatroom, String tagName) {
		// tag does not exist, i.e. tag is not in use altogether
		if (!this.tagExists(tagName)) {
			return false;
		}
		// fetch the tag
		Tag tag = this.getTag(tagName);

		List<Tag> tags = chatroom.getTags();
		List<Chatroom> chatrooms = tag.getChatroomsWithTag();

		// is there a relation between the chatroom and the tag?
		return tags.contains(tag) && chatrooms.contains(chatroom);
	}

	/**
	 * find all listed chatrom that have the given tag
	 * 
	 * @param tagName
	 * @return list of listed chatrooms wit the tag
	 */
	public List<Chatroom> findListedChatroomsWithTag(String tagName) {
		// get the names of chatrooms with the tag
		List<String> chatroomNames = this.chatroomRepository.findListedChatroomsWithTag(tagName);
		// fetch the chatrooms
		List<Chatroom> chatrooms = chatroomNames.stream().map(x -> this.chatroomRepository.findByChatroomName(x))
				.collect(Collectors.toList());

		return chatrooms;
	}

	/**
	 * Check if a tag exists with the given tagname
	 * 
	 * @param tagName
	 * @return
	 */
	protected Boolean tagExists(String tagName) {
		Tag tag = this.tagRepository.findByName(tagName);
		return tag != null;
	}

	/**
	 * fetch the tag if it exists, else create the tag ajnd return it
	 * 
	 * @param tagName
	 * @return
	 */
	@Transactional(readOnly = false)
	protected Tag getTag(String tagName) {
		// if the tag exists, return it
		if (this.tagExists(tagName)) {
			return this.tagRepository.findByName(tagName);
		}
		// else create a new tag and return it
		Tag tag = new Tag(tagName);
		return this.tagRepository.save(tag);
	}

	/**
	 * remove all the tags a chatroom has
	 * 
	 * @param chatroom
	 */
	@Transactional(readOnly = false)
	public void removeAllTagsFromChatroom(Chatroom chatroom) {
		List<Tag> tags = chatroom.getTags();
		// remove the chatroom from the tag's list of chatroom using it
		for (Tag tag : tags) {
			List<Chatroom> chatrooms = tag.getChatroomsWithTag();
			// tags.remove(tag);
			chatrooms.remove(chatroom);
		}
		// remove all the chatroom's tags
		tags.clear();
		// save the changes
		this.chatroomRepository.save(chatroom);
		// delete the tags that are no longer in use
		this.cleanTags();
	}

	/**
	 * delete tags that are not being used by a chatroom
	 */
	@Transactional(readOnly = false)
	protected void cleanTags() {
		this.tagRepository.deleteTagsWithNoRelations();
	}
}
