FROM fedora:24

# Install UTF8 locale support
RUN dnf install -y langpacks-en.noarch

# Install PostgreSQL
RUN dnf install -y postgresql-server postgresql-contrib

# Install PostGIS
RUN dnf install -y postgis

EXPOSE 5432

COPY docker-start.sh /start.sh
COPY script /install/script

CMD ["/start.sh"]
