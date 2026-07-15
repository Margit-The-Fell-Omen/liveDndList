{
  description = "Live D&D List - Spring Boot + React Dev Environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    nixpkgs,
    flake-utils,
    ...
  }:
    flake-utils.lib.eachDefaultSystem (
      system: let
        pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            # Backend
            jdk21
            maven

            # Frontend
            nodejs_26

            # Database
            postgresql_16

            # Optional
            docker-compose
          ];

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk21}"
            export PGDATA="$PWD/postgres-data"

            echo "══════════════════════════════════════════"
            echo "  Live D&D List Dev Environment"
            echo "══════════════════════════════════════════"
            echo "Java: $(java -version 2>&1 | head -n 1)"
            echo "Node: $(node --version)"
            echo "PostgreSQL: $(postgres --version)"
            echo ""
            echo "Quick Start:"
            echo "  1. Start DB:      pg_ctl start"
            echo "  2. Backend:       ./mvnw spring-boot:run"
            echo "  3. Frontend:      cd frontend && npm run dev"
            echo "══════════════════════════════════════════"
          '';
        };
      }
    );
}
