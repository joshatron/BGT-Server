BGT Server
==========

The BGT server is a RESTful api that allows people to play each other online.
It can keep track of friends and allow them to play games against each other.
It can also set up random games against random players.

A summary of the methods are described below:

| Call                                      | Type      | Description                                                               |
|-------------------------------------------|-----------|---------------------------------------------------------------------------|
| /account/register                         | POST      | Register a new user                                                       |
| /account/change-pass                      | POST      | Change your password                                                      |
| /account/change-name                      | POST      | Change your username                                                      |
| /account/authenticate                     | GET       | Check if the given credentials are valid                                  |
| /account/user                             | GET       | Get the username and ID from the username or ID                           |
| /social/request/create/{id}               | POST      | Create a friend request                                                   |
| /social/request/cancel/{id}               | DELETE    | Cancel a friend request                                                   |
| /social/request/respond/{id}              | POST      | Accept or deny a friend request                                           |
| /social/request/incoming                  | GET       | Get all your incoming friend requests                                     |
| /social/request/outgoing                  | GET       | Get all your outgoing friend requests                                     |
| /social/user/{id}/unfriend                | DELETE    | Unfriend a user                                                           |
| /social/user/{id}/block                   | POST      | Block a user                                                              |
| /social/user/{id}/unblock                 | DELETE    | Unblock a user                                                            |
| /social/user/{id}/blocked                 | GET       | Check if you are blocked by a user                                        |
| /social/user/friends                      | GET       | Get a list of your friends                                                |
| /social/user/blocking                     | GET       | Get a list for users you are blocking                                     |
| /social/message/send/{id}                 | POST      | Send a message to a user                                                  |
| /social/message/search                    | GET       | Search through your messages                                              |
| /social/notifications                     | GET       | Get the number of incoming friend requests and unread messages            |
| /games/request/create                     | POST      | Create a request for a game                                               |
| /games/request/cancel/{id}                | DELETE    | Delete a game request                                                     |
| /games/request/respond/{id}               | POST      | Accept or deny a game request                                             |
| /games/request/incoming                   | GET       | Get all your incoming game requests                                       |
| /games/request/outgoing                   | GET       | Get all your outgoing game requests                                       |
| /games/request/random/create/{size}       | POST      | Create a request for a random game                                        |
| /games/request/random/cancel              | DELETE    | Delete your request for a random game                                     |
| /games/request/random/outgoing            | GET       | Check the information on your random game request                         |
| /games/search                             | GET       | Search your games                                                         |
| /games/game/{id}                          | GET       | Get the information on a game                                             |
| /games/game/{id}/possible                 | GET       | Get all the next possible moves for a game                                |
| /games/game/{id}/play                     | POST      | Play a move on a game                                                     |
| /games/notifications                      | GET       | Get the number of incoming game requests and games where it is your turn  |
| /admin/initialize                         | POST      | Initializes admin with accound and password, specified in logs            |
| /admin/change-pass                        | POST      | Changes the password of the admin user                                    |
| /admin/user/{id}/reset                    | POST      | Resets the password for a user and return it                              |
| /admin/user/{id}/ban                      | POST      | Bans a user from logging in                                               |
| /admin/user/{id}/unban                    | POST      | Lifts the ban on a user                                                   |
| /admin/user/{id}/unlock                   | POST      | Unlocks user that got locked out from too many password attempts          |

