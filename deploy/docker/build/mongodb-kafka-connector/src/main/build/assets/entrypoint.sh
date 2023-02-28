#!/bin/bash
[[ -n $DEBUG ]] && set -x
set -eu

########################################################################################################################
my_kafka_bootstrap_servers="${KAFKA_BOOTSTRAP_SERVERS:-kafka:9091}"
my_kafka_key_converter="${KAFKA_KEY_CONVERTER:-org.apache.kafka.connect.storage.StringConverter}"
my_kafka_key_converter_schemas_enable="${KAFKA_KEY_CONVERTER_SCHEMA_ENABLED:-false}"
my_kafka_value_converter="${KAFKA_VALUE_CONVERTER:-org.apache.kafka.connect.json.JsonConverter}"
my_kafka_value_converter_schemas_enable="${KAFKA_VALUE_CONVERTER_SCHEMA_ENABLED:-false}"
my_kafka_offset_flush_interval_ms="${KAFKA_OFFSET_FLUSH_INTERVAL_MS:-10000}"

my_kafka_topics="${KAFKA_TOPICS}"
my_mongodb_connection_string="${MONGODB_CONNECTION_STRING}"
my_mongodb_database="${MONGODB_DATABASE}"
my_mongodb_collection="${MONGODB_COLLECTION}"
my_mongodb_document_id_strategy="${MONGODB_DOCUMENT_ID_STRATEGY:-com.mongodb.kafka.connect.sink.processor.id.strategy.UuidProvidedInValueStrategy}"

### Pre checks ##########################################################################################################

if [[ -z $my_kafka_topics ]]; then
  echo "No kafka topics are set!"
  exit 1
fi

if [[ -z $my_mongodb_connection_string ]]; then
  echo "No connection string is set!"
  exit 1
fi

if [[ -z $my_mongodb_database ]]; then
  echo "No database is set!"
  exit 1
fi

if [[ -z $my_mongodb_collection ]]; then
  echo "No collection is set!"
  exit 1
fi

########################################################################################################################
mongoConf="/opt/bitnami/kafka/config/mongo.properties"
connectConf="/opt/bitnami/kafka/config/connect-standalone.properties"

sed -ir "s|^bootstrap.servers=.*|bootstrap.servers=${my_kafka_bootstrap_servers}|" "$connectConf"
sed -ir "s|^key.converter=.*|key.converter=${my_kafka_key_converter}|" "$connectConf"
sed -ir "s|^key.converter.schemas.enable=.*|key.converter.schemas.enable=${my_kafka_key_converter_schemas_enable}|" "$connectConf"
sed -ir "s|^value.converter=.*|value.converter=${my_kafka_value_converter}|" "$connectConf"
sed -ir "s|^value.converter.schemas.enable=.*|value.converter.schemas.enable=${my_kafka_value_converter_schemas_enable}|" "$connectConf"
sed -ir "s|^offset.flush.interval.ms=.*|offset.flush.interval.ms=${my_kafka_offset_flush_interval_ms}|" "$connectConf"

sed -ir "s|^topics=.*|topics=${my_kafka_topics}|" "$mongoConf"
sed -ir "s|^key.converter=.*|key.converter=${my_kafka_key_converter}|" "$mongoConf"
sed -ir "s|^key.converter.schemas.enable=.*|key.converter.schemas.enable=${my_kafka_key_converter_schemas_enable}|" "$mongoConf"
sed -ir "s|^value.converter=.*|value.converter=${my_kafka_value_converter}|" "$mongoConf"
sed -ir "s|^value.converter.schemas.enable=.*|value.converter.schemas.enable=${my_kafka_value_converter_schemas_enable}|" "$mongoConf"
sed -ir "s|^connection.uri=.*|connection.uri=${my_mongodb_connection_string}|" "$mongoConf"
sed -ir "s|^database=.*|database=${my_mongodb_database}|" "$mongoConf"
sed -ir "s|^collection=.*|collection=${my_mongodb_collection}|" "$mongoConf"
sed -ir "s|^document.id.strategy=.*|document.id.strategy=${my_mongodb_document_id_strategy}|" "$mongoConf"

exec "$@"
