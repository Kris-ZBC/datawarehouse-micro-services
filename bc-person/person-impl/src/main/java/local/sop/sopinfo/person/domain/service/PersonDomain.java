package local.sop.sopinfo.person.domain.service;

import java.util.List;

import local.sop.sopinfo.person.domain.model.Person;
import local.sop.sopinfo.person.domain.model.PhoneNumberDraft;
import local.sop.sopinfo.person.domain.model.valueobjects.Email;
import local.sop.sopinfo.person.domain.model.valueobjects.FirstName;
import local.sop.sopinfo.person.domain.model.valueobjects.LastName;
import local.sop.sopinfo.person.domain.model.valueobjects.OrganizationRef;

public interface PersonDomain {

    Person create(
            FirstName firstName,
            LastName lastName,
            Email email,
            OrganizationRef organizationRef,
            List<PhoneNumberDraft> phoneNumbers
    );
}