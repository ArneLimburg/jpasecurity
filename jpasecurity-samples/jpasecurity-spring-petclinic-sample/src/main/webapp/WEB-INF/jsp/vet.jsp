<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Vet Information</h2>

  <table>
    <tr>
      <th>Name</th>
      <td><b>${vet.firstName} ${vet.lastName}</b></td>
    </tr>
    <tr>
      <th>Specialities</th>
      <td>
	    <c:forEach var="specialty" items="${vet.specialties}">
          ${specialty.name}
        </c:forEach>
        <c:if test="${vet.nrOfSpecialties == 0}">none</c:if>
      </td>
    </tr>
  </table>

  <h2>Visits</h2>

  <c:forEach var="visit" items="${visits}">
    <table width="94%">
      <tr>
        <th>Date</th>
        <td><fmt:formatDate value="${visit.date}" pattern="yyyy-MM-dd"/></td>
      </tr>
      <tr>
        <th>Pet</th>
        <td>${visit.pet.name}</td>
      </tr>
      <tr>
        <th>Type</th>
        <td>${visit.pet.type.name}</td>
      </tr>
      <tr>
        <th>Owner</th>
        <td><a href="owner.do?ownerId=${visit.pet.owner.id}">${visit.pet.owner.firstName} ${visit.pet.owner.lastName}</a></td>
      </tr>
      <tr>
        <th>Description</th>
        <td>${visit.description}</td>
      </tr>
    </table>
    <access:updating entity="visit">
      <table class="table-buttons">
        <tr>
          <td>
            <form method="GET" action="<c:url value="/editVisit.do"/>" name="formEditVisit${visit.id}">
              <input type="hidden" name="visitId" value="${visit.id}"/>
              <p class="submit"><input type="submit" value="Edit Visit"/></p>
            </form>
          </td>
        </tr>
      </table>
    </access:updating>
  </c:forEach>
  
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
