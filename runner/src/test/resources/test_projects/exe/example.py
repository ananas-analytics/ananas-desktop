import avro.schema
from avro.datafile import DataFileReader, DataFileWriter
from avro.io import DatumReader, DatumWriter
import sys
import base64
import StringIO
import io
import json

output_avsc = """{"namespace": "example.avro",
 "type": "record",
 "name": "Ouput",
 "fields": [
     {"name": "name", "type": "string"},
     {"name": "number",  "type": ["int", "null"]},
     {"name": "favorite_color", "type": ["string", "null"]}
 ]
}"""

#prepare avro data writer
output_schema = avro.schema.parse(output_avsc)
writer = DataFileWriter(sys.stdout, DatumWriter(), output_schema)

#prepare avro data reader
buf = io.BytesIO(sys.stdin.read())
reader = DataFileReader(buf, DatumReader())

i = 0
for user in reader:
    writer.append({"name": user['nname'], "number": i})
    i = i + 2

reader.close()
writer.close()
