package local.sop.sopinfo.person.interfaceadapters.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name="person")
public class PersonEntity {
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "email" , nullable = false, length = 255)
	private String email;

	@Column(name = "organization_ref", nullable = false)
	private UUID organizationRef;

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	@OneToMany(mappedBy = "person", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
	private List<PhoneNumberEntity> phoneNumbers = new ArrayList<>();

	protected PersonEntity() {} //JPA requires a default constructor

	public PersonEntity(UUID id, String firstName, String lastName, String email, UUID organizationRef) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.organizationRef = organizationRef;
	}

	public UUID getId() { return id;}
	public String getFirstName() {return firstName;}
	public String getLastName() { return lastName;}
	public String getEmail() {return email;}
	public UUID getOrganizationRef() { return organizationRef;}
	public long getVersion() {return version;}
	public List<PhoneNumberEntity> getPhoneNumbers() {return phoneNumbers;}

	public void setId(UUID id) {this.id = id;}
	public void setFirstName(String firstName) {this.firstName = firstName;}
	public void setLastName(String lastName) { this.lastName = lastName;}
	public void setEmail(String email) {this.email = email; }
	public void setOrganizationRef(UUID organizationRef) {this.organizationRef = organizationRef;}

	public void replacePhoneNumbers(List<PhoneNumberEntity> phoneNumbers) {
		this.phoneNumbers.clear();

		if (phoneNumbers == null) {
			return;
		}

		for (PhoneNumberEntity phoneNumber : phoneNumbers) {
			addPhoneNumber(phoneNumber);
		}
	}
	
	public void addPhoneNumber(PhoneNumberEntity phoneNumber) {
		phoneNumbers.add(phoneNumber);
		phoneNumber.setPerson(this);
	}
}
