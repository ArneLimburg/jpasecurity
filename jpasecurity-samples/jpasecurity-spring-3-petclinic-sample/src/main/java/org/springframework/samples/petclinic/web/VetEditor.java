package org.springframework.samples.petclinic.web;

import java.beans.PropertyEditorSupport;

import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Vet;

public class VetEditor extends PropertyEditorSupport {

    private final Clinic clinic;

    public VetEditor(Clinic clinic) {
        this.clinic = clinic;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        for (Vet vet: this.clinic.getVets()) {
            if (vet.toString().equals(text)) {
                setValue(vet);
            }
        }
    }
}
