package local.sop.datawarehouse.person.interfaceadapters.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Id;
import jakarta.persistence.Version;
import local.sop.common.libs.sharedkernel.enums.PhoneUserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "phone_numbers")
public class PhoneNumberEntity {
	@Id
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "person_id", nullable = false, columnDefinition = "BINARY(16)")
	private PersonEntity person;

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	@Enumerated(EnumType.STRING)
	@Column(name = "phone_user_type", nullable = false)
	private PhoneUserType phoneUserType;

	@Column(name = "phone_number", nullable = false, length = 255)
	private String phoneNumber;

	protected PhoneNumberEntity() {}

	public PhoneNumberEntity(UUID id, PhoneUserType phoneUserType, String phoneNumber) {
		this.id = id;
		this.phoneUserType = phoneUserType;
		this.phoneNumber = phoneNumber;
	}

	public UUID getId() { return id; }
	public PersonEntity getPerson() { return person; }
	public long getVersion() { return version; }
	public PhoneUserType getType() { return phoneUserType; }
	public String getPhoneNumber() { return phoneNumber; }

    public void setId(UUID id) {this.id = id;}
    public void setPerson(PersonEntity person) {this.person = person;}
    public void setType(PhoneUserType phoneUserType) {this.phoneUserType = phoneUserType;}
    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
}
