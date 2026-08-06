package local.sop.datawarehouse.person.domain.ports.security;

public interface HmacSecretProvider {
    String hmacSecret();
}
