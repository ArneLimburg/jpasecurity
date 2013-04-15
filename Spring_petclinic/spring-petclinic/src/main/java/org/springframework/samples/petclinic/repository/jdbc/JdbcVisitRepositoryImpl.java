/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Repository;

/**
 * A simple JDBC-based implementation of the {@link VisitRepository} interface.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 * @author Thomas Risberg
 * @author Mark Fisher
 * @author Michael Isvy
 */
@Repository
public class JdbcVisitRepositoryImpl implements VisitRepository {
	
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert insertVisit;
    
    private VetRepository vetRepository;

    private PetRepository petRepository;

    @Autowired
    public JdbcVisitRepositoryImpl(DataSource dataSource, VetRepository vetRepository, PetRepository petRepository) {
    	this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        this.insertVisit = new SimpleJdbcInsert(dataSource)
                .withTableName("visits")
                .usingGeneratedKeyColumns("id");
        this.vetRepository = vetRepository;
        this.petRepository = petRepository;
    }


    @Override
    public void save(Visit visit) throws DataAccessException {
        if (visit.isNew()) {
            Number newKey = this.insertVisit.executeAndReturnKey(
                    createVisitParameterSource(visit));
            visit.setId(newKey.intValue());
        } else {
        	this.namedParameterJdbcTemplate.update(
                    "UPDATE visits SET id=:id, visit_date=:visit_date, description=:description, " +
                            "pet_id=:pet_id, vet_id=:vet_id WHERE id=:id",
                            createVisitParameterSource(visit));
        }
    }

    public void deletePet(int id) throws DataAccessException {
        this.jdbcTemplate.update("DELETE FROM pets WHERE id=?", id);
    }


    /**
     * Creates a {@link MapSqlParameterSource} based on data values from the supplied {@link Visit} instance.
     */
    private MapSqlParameterSource createVisitParameterSource(Visit visit) {
        return new MapSqlParameterSource()
                .addValue("id", visit.getId())
                .addValue("visit_date", visit.getDate().toDate())
                .addValue("description", visit.getDescription())
                .addValue("pet_id", visit.getPet().getId())
        		.addValue("vet_id", visit.getVet().getId());
    }

    @Override
    public List<Visit> findByPetId(Integer petId) {
    	List<Visit> visits = new LinkedList<Visit>();
        final List<JdbcVisit> jdbcVisits = this.jdbcTemplate.query(
                "SELECT id, visit_date, description, vet_id FROM visits WHERE pet_id=?",
                new ParameterizedRowMapper<JdbcVisit>() {
                    @Override
                    public JdbcVisit mapRow(ResultSet rs, int row) throws SQLException {
                    	JdbcVisit visit = new JdbcVisit();
                        visit.setId(rs.getInt("id"));
                        Date visitDate = rs.getDate("visit_date");
                        visit.setDate(new DateTime(visitDate));
                        visit.setDescription(rs.getString("description"));
                        visit.setVetId(rs.getInt("vet_id"));
                        return visit;
                    }
                },
                petId);
        for (JdbcVisit visit : jdbcVisits) {
        	Vet vet = this.vetRepository.findById(visit.getVetId());
            visit.setVet(vet);
            visits.add(visit);
        }
        return visits;
    }

    @Override
    public List<Visit> findByVet(Vet vet) {
    	List<Visit> visits = new LinkedList<Visit>();
        final List<JdbcVisit> jdbcVisits = this.jdbcTemplate.query(
                "SELECT id, visit_date, description, pet_id FROM visits WHERE vet_id=?",
                new ParameterizedRowMapper<JdbcVisit>() {
                    @Override
                    public JdbcVisit mapRow(ResultSet rs, int row) throws SQLException {
                    	JdbcVisit visit = new JdbcVisit();
                        visit.setId(rs.getInt("id"));
                        Date visitDate = rs.getDate("visit_date");
                        visit.setDate(new DateTime(visitDate));
                        visit.setDescription(rs.getString("description"));
                        visit.setPetId(rs.getInt("pet_id"));
                        return visit;
                    }
                },
                vet.getId());
        for (JdbcVisit visit : jdbcVisits) {
            visit.setVet(vet);
            Pet pet = this.petRepository.findById(visit.getPetId());
            pet.addVisit(visit);
            visits.add(visit);
        }
        return visits;
    }

    @Override
    public Visit findById(int id) throws DataAccessException {
        JdbcVisit visit;
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);
            visit = this.namedParameterJdbcTemplate.queryForObject(
                    "SELECT id, description, visit_date, pet_id, vet_id FROM visits WHERE id=:id",
                    params,
                    new JdbcVisitRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Visit.class, new Integer(id));
        }
        Pet pet = this.petRepository.findById(visit.getPetId());
        pet.addVisit(visit);
        Vet vet = this.vetRepository.findById(visit.getVetId());
        visit.setVet(vet);
        return visit;
    }
}
