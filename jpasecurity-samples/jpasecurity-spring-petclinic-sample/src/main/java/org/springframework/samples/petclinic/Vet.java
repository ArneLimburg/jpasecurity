package org.springframework.samples.petclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arne Limburg
 */
public class Vet extends Person {

	private Set<Specialty> specialties;
	private Set<Visit> visits;

	protected void setSpecialtiesInternal(Set<Specialty> specialties) {
		this.specialties = specialties;
	}

	protected Set<Specialty> getSpecialtiesInternal() {
		if (this.specialties == null) {
			this.specialties = new HashSet<Specialty>();
		}
		return this.specialties;
	}

	public List<Specialty> getSpecialties() {
		List<Specialty> sortedSpecs = new ArrayList<Specialty>(getSpecialtiesInternal());
		PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
		return Collections.unmodifiableList(sortedSpecs);
	}

	public int getNrOfSpecialties() {
		return getSpecialtiesInternal().size();
	}

	public void addSpecialty(Specialty specialty) {
		getSpecialtiesInternal().add(specialty);
	}


    protected void setVisitsInternal(Set<Visit> visits) {
        this.visits = visits;
    }

    protected Set<Visit> getVisitsInternal() {
        if (this.visits == null) {
            this.visits = new HashSet<Visit>();
        }
        return this.visits;
    }

    public List<Visit> getVisits() {
        List<Visit> sortedVisits = new ArrayList<Visit>(getVisitsInternal());
        PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", true, true));
        return Collections.unmodifiableList(sortedVisits);
    }

    public int getNrOfVisits() {
        return getVisitsInternal().size();
    }

    public void addVisit(Visit visit) {
        getVisitsInternal().add(visit);
    }
    
    public String toString() {
        StringBuilder specialties = new StringBuilder();
        for (Specialty specialty: getSpecialties()) {
            specialties.append(' ').append(specialty.getName()).append(',');
        }
        if (getNrOfSpecialties() == 0) {
            specialties.append("(none)");
        } else {
            specialties.setCharAt(0, '(');
            specialties.setCharAt(specialties.length() - 1, ')');
        }
        return super.toString() + " " + specialties.toString();
    }
}
