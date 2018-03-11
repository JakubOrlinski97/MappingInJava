echo -n "ssljake" | argon2 somesalt -id -t 10 -m 16 -p 3 -e | cut -c33-88 > hash.txt
keytool -genkey -alias MappingInJava -keystore jake.store

keytool -export -alias MappingInJava -keystore jake.store -file mykey.cert
keytool -import -file mykey.cert -alias MappingInJava -keystore jakeTrust.store