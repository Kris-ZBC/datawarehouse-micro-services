package local.sop.datawarehouse.personnotification.domain.ports.security;

public interface HmacSecretProvider {
    String hmacSecret();
}
