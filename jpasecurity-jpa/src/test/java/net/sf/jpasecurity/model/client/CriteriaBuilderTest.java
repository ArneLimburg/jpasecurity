package net.sf.jpasecurity.model.client;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import net.sf.jpasecurity.persistence.AbstractEntityTestCase;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.BeforeClass;
import org.junit.Test;

public class CriteriaBuilderTest extends AbstractEntityTestCase {

    private static final String EMAIL = "test@test.org";

    @BeforeClass
    public static void createEntityManagerFactory() throws SQLException {
        TestAuthenticationProvider.authenticate(EMAIL);
        createEntityManagerFactory("client");
        dropForeignKey("jdbc:hsqldb:mem:test", "sa", "", "CLIENTOPERATIONSTRACKING", "CLIENT");
    }

    @Test
    public void criteria() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<ClientEntityBrowserDto> c = cb.createQuery(ClientEntityBrowserDto.class);
        Root<Client> client = c.from(Client.class);

        Join<Client, ClientGroup> clientGroup = client.join("group", JoinType.LEFT);
        Join<Client, ClientStructure> clientStructure = client.join("structure", JoinType.LEFT);
        Join<ClientType, ClientTypeGroup> clientTypeGroup = client.join("type", JoinType.LEFT).join("clientTypeGroup",
            JoinType.LEFT);

        Selection<ClientEntityBrowserDto> selection = cb.construct(ClientEntityBrowserDto.class, client.<Integer>get("id"),
            client.<String>get("number"), client.<String>get("name"), clientTypeGroup.<String>get("name"),
            clientGroup.<String>get("name"), clientStructure.<String>get("name")

            );

        c.select(selection);
        c.distinct(true);
        TypedQuery<ClientEntityBrowserDto> query = getEntityManager().createQuery(c);
        assertTrue(query.getResultList().isEmpty());
    }
}
