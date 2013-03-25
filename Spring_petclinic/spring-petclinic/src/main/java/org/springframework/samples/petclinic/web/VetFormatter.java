package org.springframework.samples.petclinic.web;


import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;

import org.hibernate.engine.jdbc.internal.Formatter;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.ClinicService;


public class VetFormatter implements Formatter<Vet> {

    private final ClinicService clinicService;


    @Autowired
    public VetFormatter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public PetType parse(String text, Locale locale) throws ParseException {
        Collection<PetType> findVets = this.clinicService.findVets();
        for (Vet vet : findVets) {
            if (vet.getName().equals(text)) {
                return vet;
            }
        }
        throw new ParseException("vet not found: " + text, 0);
    }

}