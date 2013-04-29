package org.springframework.samples.petclinic.web;


import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.service.ClinicService;


public class VetFormatter implements Formatter<Vet> {

    private final ClinicService clinicService;


    @Autowired
    public VetFormatter(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @Override
    public String print(Vet vet, Locale locale) {
        return vet.toString();
    }

    @Override
    public Vet parse(String text, Locale locale) throws ParseException {
        Collection<Vet> findVets = this.clinicService.findVets();
        for (Vet vet : findVets) {
            if (vet.toString().equals(text)) {
                return vet;
            }
        }
        throw new ParseException("vet not found: " + text, 0);
    }
}
