# Prerequisites
#
# * brew install jq
#


# Register `username1`
curl -d '{
    "userName": "username1",
    "displayName": "displayname1",
    "password": "A$ecurePassword5",
    "passwordReap": "A$ecurePassword5",
    "email": "username1@domain.com"
}' -H "Content-Type: application/json" -X POST http://localhost:9090/register

# Complete registration of `username1`
curl -H "Content-Type: application/json" -X PUT http://localhost:9090/validation/username1

# Login of `username1`
curl -d '{"userName": "username1", "password": "A$ecurePassword5"}' -H "Content-Type: application/json" -X POST http://localhost:9090/login

# Extract JWT from login response
curl -d '{"userName": "username1", "password": "A$ecurePassword5"}' -H "Content-Type: application/json" -X POST http://localhost:9090/login | jq '.GoodResp[0].token'

# Extract JWT from login response and insert into file
curl -d '{"userName": "username1", "password": "A$ecurePassword5"}' -H "Content-Type: application/json" -X POST http://localhost:9090/login | jq -r '.GoodResp[0].token' > jwt.txt

# Create chat room `chatroomName`
curl -d '{
    "chatroomName": "chatroomName",
    "displayName": "displayName",
    "description": "description",
    "listed": false,
    "invited_only": false,
    "ownerUsername": "ownerUsername",
    "created": 15,
    "lastMessageReceived": 16,
    "tags": ["a", "b", "c", "d"]
}' -H "Content-Type:application/json" -H "Authorization:"$(cat jwt.txt) -X POST http://localhost:9090/auth/chatroom/



