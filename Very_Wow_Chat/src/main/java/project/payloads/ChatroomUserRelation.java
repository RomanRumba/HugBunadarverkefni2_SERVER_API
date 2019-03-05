package project.payloads;

public enum ChatroomUserRelation {
	// The user owns the chatroom
	OWNER,
	// the user is an admin of the chatroom
	ADMIN,
	// the user is a member of the chatroom
	MEMBER,
	// the user is not a member of the chatroom
	NOT_MEMBER;
}
