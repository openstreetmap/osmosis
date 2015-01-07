FROM fedora:21

# Install PostgreSQL
RUN yum install -y postgresql-server postgresql-contrib

# Install PostGIS
RUN yum install -y postgis

EXPOSE 5432

COPY docker-start.sh /start.sh

CMD ["/start.sh"]
