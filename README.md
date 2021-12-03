# JavaTorrent
## There are two Spring applications:
1. Centralized cerver which controls all the information about file parts on all clients.
2. And a client app (It actually is a Tomcat server too) which splits file to packs and sends this information to the center

<br>
When client app recieves a get file request, it gets the information about packs of this file from the center and donwloads each pack from the client that ows this pack. Then, all pack are being gathered together and form the desired file.

## There is a Docker compose file which will run two client and one center containers within the specified directory.
