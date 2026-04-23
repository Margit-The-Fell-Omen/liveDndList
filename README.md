# Live D&D List
## Overview
This is a Spring Boot application for creating and managing D&D character lists, built with REST architecture. This app provides user-friendly interface for creating D&D characters, managing and (in future) sharing them.
## Development state
Currently in active development. 
Latest release version - [Live D&D List 0.0.1](http://live-dnd-list.duckdns.org/)
## Tech stack
- JDK 21
- JWT 0.12.3
- Spring Boot 4.0.2
- PostgreSQL:18-alpine
- Redis (latest)
## Building and running
To run this app locally you should install Docker and Docker Compose.
If you want to run this app locally use this commands:
    'cd <your project root folder>
    git clone https://github.com/Margit-The-Fell-Omen/liveDndList.git
    docker compose up --build'
Then frontend will run on port 80 and backend will run on post 8080.
**IMPORTANT:** if you experience infinite startup loop of docker services - remove 'restart: always' lines from all containers in 'dacker-compose.yml' file.
## Contributing
'CONTRIBUTING.md' will be created in the future.
## Versioning scheme
Will be decided in the future.
## Sonar Cloud
Project is checked by Sonar Cloud free [Link to Sonar Cloud](https://sonarcloud.io/summary/overall?id=Margit-The-Fell-Omen_liveDndList&branch=master)
## License
This project is released into public domain.

See UNLICENSE.md for details.
