#!/bin/sh
set -eu

validate_secret() {
  name=$1
  value=$2
  if [ "${#value}" -lt 32 ]; then
    echo "$name must be replaced with a unique random secret of at least 32 characters" >&2
    exit 1
  fi
  case "$value" in
    *replace-with*)
      echo "$name still contains the example placeholder" >&2
      exit 1
      ;;
  esac
}

validate_secret POSTGRES_ADMIN_PASSWORD "$POSTGRES_PASSWORD"
validate_secret GATEWAY_DB_PASSWORD "$GATEWAY_DB_PASSWORD"
validate_secret SINHVIEN_DB_PASSWORD "$SINHVIEN_DB_PASSWORD"
validate_secret DETAI_DB_PASSWORD "$DETAI_DB_PASSWORD"
validate_secret DANGKY_DB_PASSWORD "$DANGKY_DB_PASSWORD"

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -v gateway_password="$GATEWAY_DB_PASSWORD" \
  -v sinhvien_password="$SINHVIEN_DB_PASSWORD" \
  -v detai_password="$DETAI_DB_PASSWORD" \
  -v dangky_password="$DANGKY_DB_PASSWORD" <<'SQL'
CREATE ROLE gateway_user LOGIN PASSWORD :'gateway_password';
CREATE ROLE sinhvien_user LOGIN PASSWORD :'sinhvien_password';
CREATE ROLE detai_user LOGIN PASSWORD :'detai_password';
CREATE ROLE dangky_user LOGIN PASSWORD :'dangky_password';

CREATE DATABASE gateway OWNER gateway_user;
CREATE DATABASE sinhvien OWNER sinhvien_user;
CREATE DATABASE detai OWNER detai_user;
CREATE DATABASE dangky OWNER dangky_user;

REVOKE CONNECT ON DATABASE gateway FROM PUBLIC;
REVOKE CONNECT ON DATABASE sinhvien FROM PUBLIC;
REVOKE CONNECT ON DATABASE detai FROM PUBLIC;
REVOKE CONNECT ON DATABASE dangky FROM PUBLIC;
SQL
